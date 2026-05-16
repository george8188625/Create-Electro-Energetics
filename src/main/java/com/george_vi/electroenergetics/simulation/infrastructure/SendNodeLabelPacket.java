package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record SendNodeLabelPacket(InWorldNode node, Optional<String> label) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, SendNodeLabelPacket> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, SendNodeLabelPacket::node,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), SendNodeLabelPacket::label,
            SendNodeLabelPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        WireRenderer.setNodeLabel(node, label.orElse(null));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_NODE_LABEL;
    }
}
