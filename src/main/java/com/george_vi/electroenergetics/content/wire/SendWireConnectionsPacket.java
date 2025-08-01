package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.WireData;
import com.george_vi.electroenergetics.simulation.simulator.DirectionSensitiveNodeConnection;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SendWireConnectionsPacket(List<Pair<DirectionSensitiveNodeConnection, WireData>> connections) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Pair.streamCodec(DirectionSensitiveNodeConnection.STREAM_CODEC, WireData.STREAM_CODEC)), SendWireConnectionsPacket::connections,
            SendWireConnectionsPacket::new
    );

    public static SendWireConnectionsPacket connectWire(Node node1, Node node2, WireData data) {
        return new SendWireConnectionsPacket(List.of(Pair.of(new DirectionSensitiveNodeConnection(node1, node2), data)));
    }

    public static SendWireConnectionsPacket connectWire(NodeConnection connection, WireData data) {
        return connectWire(connection.node1(), connection.node2(), data);
    }

    @Override
    public void handle(LocalPlayer player) {
        for (Pair<DirectionSensitiveNodeConnection, WireData> connection : connections())
            WireRenderer.addConnection(new NodeConnection(connection.getFirst().node1(), connection.getFirst().node2()), connection.getSecond());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_WIRE_CONNECTIONS;
    }
}
