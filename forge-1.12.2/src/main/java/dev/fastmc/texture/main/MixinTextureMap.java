package dev.fastmc.texture.main;

import dev.fastmc.texture.IPatchedTextureMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap implements IPatchedTextureMap {
    @Shadow
    private int mipmapLevels;

    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapUploadedSprites;

    @Shadow
    @Final
    private List<TextureAtlasSprite> listAnimatedSprites;

    @Mutable
    @Shadow(remap = false)
    @Final
    private Set<ResourceLocation> loadedSprites;

    @Mutable
    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Shadow(remap = false)
    protected abstract void finishLoading(Stitcher stitcher, ProgressBar bar, int j, int k);

    @Shadow
    protected abstract ResourceLocation getResourceLocation(TextureAtlasSprite p_184396_1_);

    @Shadow
    public abstract TextureAtlasSprite registerSprite(ResourceLocation location);

    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract boolean generateMipmaps(IResourceManager resourceManager, TextureAtlasSprite texture);

    private static final ThreadLocal<ArrayDeque<ResourceLocation>> loadingSpritesOverride = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/renderer/texture/ITextureMapPopulator;Z)V", at = @At("RETURN"))
    private void Inject$init$RETURN(CallbackInfo ci) {
        mapRegisteredSprites = new ConcurrentHashMap<>();
        loadedSprites = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /**
     * @author Luna
     * @reason Parallel loading
     */
    @Overwrite
    public void loadTextureAtlas(IResourceManager resourceManager) {
        int maxTextureSize = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(maxTextureSize, maxTextureSize, 0, this.mipmapLevels);
        this.mapUploadedSprites.clear();
        this.listAnimatedSprites.clear();
        loadedSprites.clear();
        FMLLog.log.info("Max texture size: {}", maxTextureSize);

        ProgressBar bar = ProgressManager.push("Texture stitching", this.mapRegisteredSprites.size());
        int mip = 1 << this.mipmapLevels;

        int count = loadTextureParallel(resourceManager, stitcher, bar, mip);
        finishLoading(stitcher, bar, count, mip);
    }

    /**
     * @author Luna
     * @reason Parallel loading
     */
    @Overwrite(remap = false)
    private int loadTexture(
        Stitcher stitcher,
        IResourceManager resourceManager,
        ResourceLocation location,
        TextureAtlasSprite textureatlassprite,
        ProgressManager.ProgressBar bar,
        int j,
        int k
    ) {
        if (loadedSprites.contains(location)) {
            return j;
        }
        ResourceLocation resourcelocation = this.getResourceLocation(textureatlassprite);
        IResource resource = null;
        ArrayDeque<ResourceLocation> loadingSprites = loadingSpritesOverride.get();

        for (ResourceLocation loading : loadingSprites) {
            if (location.equals(loading)) {
                final String error = "circular texture dependencies, stack: [" + com.google.common.base.Joiner.on(", ").join(
                    loadingSprites) + "]";
                net.minecraftforge.fml.client.FMLClientHandler.instance().trackBrokenTexture(resourcelocation, error);
                return j;
            }
        }
        loadingSprites.addLast(location);

        try {
            for (ResourceLocation dependency : textureatlassprite.getDependencies()) {
                if (!mapRegisteredSprites.containsKey(dependency.toString())) {
                    registerSprite(dependency);
                }
                TextureAtlasSprite depSprite = mapRegisteredSprites.get(dependency.toString());
                j = loadTexture(stitcher, resourceManager, dependency, depSprite, bar, j, k);
            }
            try {
                if (textureatlassprite.hasCustomLoader(resourceManager, resourcelocation)) {
                    if (textureatlassprite.load(
                        resourceManager,
                        resourcelocation,
                        l -> mapRegisteredSprites.get(l.toString())
                    )) {
                        return j;
                    }
                } else {
                    PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(resourceManager.getResource(resourcelocation));
                    resource = resourceManager.getResource(resourcelocation);
                    boolean flag = resource.getMetadata("animation") != null;
                    textureatlassprite.loadSprite(pngsizeinfo, flag);
                }
            } catch (RuntimeException runtimeexception) {
                net.minecraftforge.fml.client.FMLClientHandler.instance().trackBrokenTexture(
                    resourcelocation,
                    runtimeexception.getMessage()
                );
                return j;
            } catch (IOException e) {
                net.minecraftforge.fml.client.FMLClientHandler.instance().trackMissingTexture(resourcelocation);
                return j;
            } finally {
                IOUtils.closeQuietly(resource);
            }

            j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
            int j1 = Math.min(
                Integer.lowestOneBit(textureatlassprite.getIconWidth()),
                Integer.lowestOneBit(textureatlassprite.getIconHeight())
            );

            if (j1 < k) {
                // FORGE: do not lower the mipmap level, just log the problematic textures
                LOGGER.warn(
                    "Texture {} with size {}x{} will have visual artifacts at mip level {}, it can only support level {}. Please report to the mod author that the texture should be some multiple of 16x16.",
                    resourcelocation,
                    textureatlassprite.getIconWidth(),
                    textureatlassprite.getIconHeight(),
                    MathHelper.log2(k),
                    MathHelper.log2(j1)
                );
            }

            if (generateMipmaps(resourceManager, textureatlassprite)) {
                stitcher.addSprite(textureatlassprite);
            }
            return j;
        } finally {
            loadingSprites.removeLast();
            loadedSprites.add(location);
        }
    }
}
