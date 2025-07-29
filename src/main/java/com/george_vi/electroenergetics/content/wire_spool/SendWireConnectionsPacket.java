package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.Node;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SendWireConnectionsPacket(List<Pair<Node, Node>> connections) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Pair.streamCodec(Node.STREAM_CODEC, Node.STREAM_CODEC)), SendWireConnectionsPacket::connections,
            SendWireConnectionsPacket::new
    );

    public static SendWireConnectionsPacket connectWire(Node node1, Node node2) {
        return new SendWireConnectionsPacket(List.of(Pair.of(node1, node2)));
    }

    public static SendWireConnectionsPacket connectWire(NodeConnection connection) {
        return connectWire(connection.node1(), connection.node2());
    }

    @Override
    public void handle(LocalPlayer player) {
        for (Pair<Node, Node> connection : connections())
            WireRenderer.addConnection(new NodeConnection(connection.getFirst(), connection.getSecond()));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_WIRE_CONNECTIONS;
    }
}
