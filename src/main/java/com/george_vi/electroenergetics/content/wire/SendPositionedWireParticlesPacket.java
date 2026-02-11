package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SendPositionedWireParticlesPacket(Vec3 pos1, Vec3 pos2, ParticleOptions options, Float sag, float chance) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SendPositionedWireParticlesPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecs.VEC3, SendPositionedWireParticlesPacket::pos1,
            CatnipStreamCodecs.VEC3, SendPositionedWireParticlesPacket::pos2,
            ParticleTypes.STREAM_CODEC, SendPositionedWireParticlesPacket::options,
            ByteBufCodecs.FLOAT, SendPositionedWireParticlesPacket::sag,
            ByteBufCodecs.FLOAT, SendPositionedWireParticlesPacket::chance,
            SendPositionedWireParticlesPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {

        List<Vec3> cablePoints = QuadraticWireHelper.cablePoints(pos1, pos2, sag);
        cablePoints.add(pos2);
        for (int i = 0; i < cablePoints.size() - 1; i++) {
            Vec3 point = cablePoints.get(i);
            Vec3 nextPoint = cablePoints.get(i + 1);
            for (int j = 0; j < chance; j++) {
                if (player.level().random.nextFloat() > 1 - chance) {
                    point = VecHelper.lerp(player.level().random.nextFloat(), point, nextPoint);
                    player.level().addParticle(options, point.x, point.y, point.z, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_POSITIONED_WIRE_PARTICLE;
    }
}
