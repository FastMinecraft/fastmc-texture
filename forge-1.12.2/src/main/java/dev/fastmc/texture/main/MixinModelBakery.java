package dev.fastmc.texture.main;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ModelBakery.class, remap = false)
public class MixinModelBakery {
    @Mutable
    @Shadow @Final private Map<Item, List<String>> variantNames;

    @Mutable
    @Shadow @Final private Map<ResourceLocation, ModelBlockDefinition> blockDefinitions;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void Inject$init$RETURN(CallbackInfo ci) {
        variantNames = new Reference2ObjectOpenHashMap<>();
        blockDefinitions = new ConcurrentHashMap<>();
    }
}
