package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.CEEHoldInteractionHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), remap = false)
    private void electroenergetics$turnPlayer(LocalPlayer instance, double y, double x) {
        if (CEEHoldInteractionHandler.isInteracting())
            CEEHoldInteractionHandler.onPlayerMove(x, y);
        else
            instance.turn(y, x);
    }
}
