package dev.fastmc.texture.main;

import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Stitcher.class)
public class MixinStitcher {
    @Mutable
    @Shadow
    @Final
    private Set<Stitcher.Holder> setStitchHolders;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void Inject$init$RETURN(CallbackInfo ci) {
        setStitchHolders = Collections.newSetFromMap(new ConcurrentHashMap<>(256));
    }
}
