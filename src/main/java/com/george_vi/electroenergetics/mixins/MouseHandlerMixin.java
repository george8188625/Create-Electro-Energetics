package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "turnPlayer",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"),
            cancellable = true, remap = false)
    private void electroenergetics$turnPlayer(final double d, final CallbackInfo ci,
                                      @Local(ordinal = 4) final double j, @Local(ordinal = 5) final double k,
                                      @Local(ordinal = 0) final int l) {
        if (CEEHoldInteractionHandler.isInteracting()) {
            CEEHoldInteractionHandler.onPlayerMove(j, k * l);
            ci.cancel();
        }
    }
}
