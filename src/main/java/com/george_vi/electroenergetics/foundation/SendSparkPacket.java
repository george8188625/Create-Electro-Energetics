package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.CEEPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record SendSparkPacket(Vec3 pos, SparkSize size) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendSparkPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecs.VEC3, SendSparkPacket::pos,
            CatnipStreamCodecBuilders.ofEnum(SparkSize.class), SendSparkPacket::size,
            SendSparkPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_SPARK;
    }

    enum SparkSize {
        SMALL,
        MEDIUM,
        BIG
    }
}
