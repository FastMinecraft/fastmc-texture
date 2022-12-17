package dev.fastmc.texture.accessor;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundList;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SoundHandler.class)
public interface AccessorSoundHandler {
    @Accessor("LOGGER")
    static Logger getLogger() {throw new UnsupportedOperationException();}

    @Invoker
    void callLoadSoundResource(ResourceLocation location, SoundList sounds);
}
