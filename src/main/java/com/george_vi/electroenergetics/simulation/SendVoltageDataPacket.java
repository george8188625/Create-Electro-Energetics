package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.SendCatenaryPacket;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.Node;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;

public record SendVoltageDataPacket(List<Pair<Node, Double>> voltages) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendVoltageDataPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Pair.streamCodec(Node.STREAM_CODEC, ByteBufCodecs.DOUBLE)), SendVoltageDataPacket::voltages,
            SendVoltageDataPacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        for (Pair<Node, Double> e : voltages) {
            WireRenderer.addVoltageData(e.getFirst(), e.getSecond());
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_VOLTAGE_DATA;
    }
}
