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

import java.util.ArrayList;

public record ClearWireConnectionsPacket(BlockPos pos1, int id1, BlockPos pos2, int id2, boolean all) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, ClearWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClearWireConnectionsPacket::pos1,
            ByteBufCodecs.INT, ClearWireConnectionsPacket::id1,
            BlockPos.STREAM_CODEC, ClearWireConnectionsPacket::pos2,
            ByteBufCodecs.INT, ClearWireConnectionsPacket::id2,
            ByteBufCodecs.BOOL, ClearWireConnectionsPacket::all,
            ClearWireConnectionsPacket::new
    );

    public static ClearWireConnectionsPacket clearAll() {
        return new ClearWireConnectionsPacket(BlockPos.ZERO, 0, BlockPos.ZERO, 0, true);
    }

    @Override
    public void handle(LocalPlayer player) {
        if (all) {
            WireRenderer.WIRE_CONNECTIONS = new ArrayList<>();
            return;
        }

        NodeConnection toRemove = new NodeConnection(new Node(id1, pos1), new Node(id2, pos2));
        WireRenderer.removeConnections(toRemove);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CLEAR_WIRE_CONNECTIONS;
    }
}
