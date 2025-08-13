package com.george_vi.electroenergetics.content.wire.interaction;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record InteractWirePacket(NodeConnectionPoint point) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, InteractWirePacket> STREAM_CODEC = StreamCodec.composite(
            NodeConnectionPoint.STREAM_CODEC, InteractWirePacket::point,
            InteractWirePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        ItemStack stackInHand = player.getMainHandItem();

        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stackInHand))
                .findFirst().orElse(null);

        if (behaviour != null)
            behaviour.interactWire(point(), player.level(), player, stackInHand);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.INTERACT_WIRE;
    }
}
