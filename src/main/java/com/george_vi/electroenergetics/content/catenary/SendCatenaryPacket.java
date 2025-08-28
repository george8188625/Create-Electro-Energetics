package com.george_vi.electroenergetics.content.catenary;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.WireData;
import com.george_vi.electroenergetics.simulation.simulator.DirectionSensitiveNodeConnection;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SendCatenaryPacket(List<Couple<BlockPos>> connections) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendCatenaryPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Couple.streamCodec(BlockPos.STREAM_CODEC)), SendCatenaryPacket::connections,
            SendCatenaryPacket::new
    );

    public static SendCatenaryPacket connectWire(Couple<BlockPos> connection) {
        return new SendCatenaryPacket(List.of(connection));
    }

    @Override
    public void handle(LocalPlayer player) {
        for (Couple<BlockPos> connection : connections())
            WireRenderer.addCatenary(connection);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_CATENARY;
    }
}
