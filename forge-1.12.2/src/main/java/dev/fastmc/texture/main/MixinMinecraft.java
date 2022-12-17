package dev.fastmc.texture.main;

import dev.fastmc.texture.FastMcTextureMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method ="init", at = @At("HEAD"))
    private void Inject$init$HEAD(CallbackInfo info) {
        FastMcTextureMod.init();
    }
}
