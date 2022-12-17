package dev.fastmc.texture.accessor;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("unused")
@Mixin(value = ModelLoaderRegistry.class, remap = false)
public interface AccessorModelLoaderRegistry {
    @Invoker
    static void callAddAlias(ResourceLocation from, ResourceLocation to) {throw new UnsupportedOperationException();}

    @Invoker
    static IModel callGetMissingModel(ResourceLocation location, Throwable cause) {throw new UnsupportedOperationException();}
}
