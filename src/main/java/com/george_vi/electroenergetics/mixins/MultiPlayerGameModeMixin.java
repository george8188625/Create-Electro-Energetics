package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    
    @Redirect(method = "startPrediction", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), remap = false)
    private static void electroEnergetics$sendUseOn(ClientPacketListener instance, Packet<?> packet) {
        if (WireInteractionHandler.preventUseOnBlockPacket) {
            if (packet instanceof ServerboundUseItemOnPacket) {
                WireInteractionHandler.preventUseOnBlockPacket = false;
                return;
            }
        }

        instance.send(packet);
    }

}
