package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record SendNodeDataPacket(InWorldNode node, Optional<String> label, Optional<Vec3> detachedPosition, boolean remove) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, SendNodeDataPacket> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, SendNodeDataPacket::node,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), SendNodeDataPacket::label,
            ByteBufCodecs.optional(CatnipStreamCodecs.VEC3), SendNodeDataPacket::detachedPosition,
            ByteBufCodecs.BOOL, SendNodeDataPacket::remove,
            SendNodeDataPacket::new
    );

    public static SendNodeDataPacket remove(InWorldNode node) {
        return new SendNodeDataPacket(node, Optional.empty(), Optional.empty(), true);
    }

    public static SendNodeDataPacket update(InWorldNodeData nodeData) {
        InWorldNode node = nodeData.node;
        return new SendNodeDataPacket(node, Optional.ofNullable(nodeData.label),
                Optional.ofNullable(nodeData.detachedNodeType == null ? null : nodeData.getGlobalPos()), false);
    }

    @Override
    public void handle(LocalPlayer player) {
        if (remove)
            WireRenderer.removeNodeData(node);
        else
            WireRenderer.setNodeData(node, label.orElse(null), detachedPosition.orElse(null));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_NODE_DATA;
    }
}
