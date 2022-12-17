package dev.fastmc.texture.main;

import dev.fastmc.texture.IPatchedSoundHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.audio.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Mixin(SoundHandler.class)
public abstract class MixinSoundHandler implements IPatchedSoundHandler {
    @Shadow
    @Final
    private SoundRegistry soundRegistry;

    @Shadow
    @Nullable
    protected abstract Map<String, SoundList> getSoundMap(InputStream stream);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private SoundManager sndManager;

    /**
     * @author Luna
     * @reason Parallel loading
     */
    @Overwrite
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.soundRegistry.clearMap();

        List<Tuple<ResourceLocation, SoundList>> resources = new ObjectArrayList<>();
        for (String s : resourceManager.getResourceDomains()) {
            try {
                for (IResource resource : resourceManager.getAllResources(new ResourceLocation(s, "sounds.json"))) {
                    try {
                        Map<String, SoundList> map = this.getSoundMap(resource.getInputStream());

                        assert map != null;
                        for (Map.Entry<String, SoundList> entry : map.entrySet()) {
                            resources.add(new net.minecraft.util.Tuple<>(
                                new ResourceLocation(s, entry.getKey()),
                                entry.getValue()
                            ));
                        }
                    } catch (RuntimeException runtimeexception) {
                        LOGGER.warn("Invalid sounds.json", runtimeexception);
                    }
                }
            } catch (IOException e) {
                // NO-OP
            }
        }

        loadSoundsParallel(resources);

        for (ResourceLocation location : this.soundRegistry.getKeys()) {
            SoundEventAccessor accessor = this.soundRegistry.getObject(location);

            assert accessor != null;
            if (accessor.getSubtitle() instanceof TextComponentTranslation) {
                String s1 = ((TextComponentTranslation) accessor.getSubtitle()).getKey();

                if (!I18n.hasKey(s1)) {
                    LOGGER.debug("Missing subtitle {} for event: {}", s1, location);
                }
            }

            if (SoundEvent.REGISTRY.getObject(location) == null) {
                LOGGER.debug("Not having sound event for: {}", location);
            }
        }

        this.sndManager.reloadSoundSystem();
    }
}
