package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.Node;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SendWireConnectionsPacket(BlockPos pos1, int id1, BlockPos pos2, int id2) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SendWireConnectionsPacket::pos1,
            ByteBufCodecs.INT, SendWireConnectionsPacket::id1,
            BlockPos.STREAM_CODEC, SendWireConnectionsPacket::pos2,
            ByteBufCodecs.INT, SendWireConnectionsPacket::id2,
            SendWireConnectionsPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        NodeConnection newConnection = new NodeConnection(new Node(id1, pos1), new Node(id2, pos2));
        WireRenderer.addConnection(newConnection);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_WIRE_CONNECTIONS;
    }
}
