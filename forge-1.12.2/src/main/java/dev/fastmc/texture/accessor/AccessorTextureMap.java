package dev.fastmc.texture.accessor;

import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TextureMap.class)
public interface AccessorTextureMap {
    @Accessor
    Map<String, TextureAtlasSprite> getMapRegisteredSprites();

    @Invoker(remap = false)
    int callLoadTexture(Stitcher stitcher, IResourceManager resourceManager, ResourceLocation location, TextureAtlasSprite textureatlassprite, ProgressManager.ProgressBar bar, int j, int k);
}
