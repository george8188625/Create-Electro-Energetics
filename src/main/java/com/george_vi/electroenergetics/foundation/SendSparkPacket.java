package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.CEEPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SendSparkPacket(Vec3 pos, SparkSize size) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendSparkPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecs.VEC3, SendSparkPacket::pos,
            CatnipStreamCodecBuilders.ofEnum(SparkSize.class), SendSparkPacket::size,
            SendSparkPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        Level level = player.level();
        if (size == SparkSize.SMALL) {
            for (int i = 0; i < 15; i++) {
                Vec3 vel = Vec3.ZERO.offsetRandom(level.random, 0.06f);
                level.addParticle(ParticleTypes.BUBBLE_POP, pos.x, pos.y, pos.z, vel.z, vel.y, vel.z);
            }

            level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 0, 0, 0);

        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_SPARK;
    }

    public enum SparkSize {
        SMALL,
        MEDIUM,
        BIG
    }
}
