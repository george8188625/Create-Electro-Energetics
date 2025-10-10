package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ClearWireConnectionsPacket(List<Pair<InWorldNode, InWorldNode>> connections, boolean all) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, ClearWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Pair.streamCodec(InWorldNode.STREAM_CODEC, InWorldNode.STREAM_CODEC)), ClearWireConnectionsPacket::connections,
            ByteBufCodecs.BOOL, ClearWireConnectionsPacket::all,
            ClearWireConnectionsPacket::new
    );


    public static ClearWireConnectionsPacket clearAll() {
        return new ClearWireConnectionsPacket(Collections.emptyList(), true);
    }

    public static ClearWireConnectionsPacket clearWire(InWorldNode node1, InWorldNode node2) {
        return new ClearWireConnectionsPacket(List.of(Pair.of(node1, node2)), false);
    }

    public static ClearWireConnectionsPacket clearWire(NodeConnection connection) {
        return clearWire(connection.node1(), connection.node2());
    }

    @Override
    public void handle(LocalPlayer player) {
        if (all) {
            WireRenderer.WIRE_CONNECTIONS = new ArrayList<>();
            return;
        }

        for (Pair<InWorldNode, InWorldNode> connection : connections()) {
            WireRenderer.removeConnections(new NodeConnection(connection.getFirst(), connection.getSecond()));
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CLEAR_WIRE_CONNECTIONS;
    }
}
