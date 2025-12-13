package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ClearCatenaryPacket(List<Couple<BlockPos>> connections, boolean all) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, ClearCatenaryPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Couple.streamCodec(BlockPos.STREAM_CODEC)), ClearCatenaryPacket::connections,
            ByteBufCodecs.BOOL, ClearCatenaryPacket::all,
            ClearCatenaryPacket::new
    );


    public static ClearCatenaryPacket clearAll() {
        return new ClearCatenaryPacket(Collections.emptyList(), true);
    }

    public static ClearCatenaryPacket clearWire(Couple<BlockPos> connection) {
        return new ClearCatenaryPacket(List.of(connection), false);
    }

    @Override
    public void handle(LocalPlayer player) {
        if (all) {
            WireRenderer.CATENARY = new ArrayList<>();
            return;
        }

        for (Couple<BlockPos> connection : connections()) {
            WireRenderer.removeCatenary(connection);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CLEAR_CATENARY;
    }
}
