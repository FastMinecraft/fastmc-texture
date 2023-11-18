package dev.fastmc.texture.main;

import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(SoundRegistry.class)
public class MixinSoundRegistry {
    @Shadow
    private Map<ResourceLocation, SoundEventAccessor> soundRegistry;

    @Inject(method = "createUnderlyingMap", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", shift = At.Shift.AFTER, remap = false), remap = false)
    private void Inject$createUnderlyingMap$INVOKE$Maps$newHashMap(CallbackInfoReturnable<HashMap<?, ?>> cir) {
        soundRegistry = new ConcurrentHashMap<>();
    }
}
