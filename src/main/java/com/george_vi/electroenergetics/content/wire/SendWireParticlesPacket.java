package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SendWireParticlesPacket(Node node1, Node node2, ParticleOptions options) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SendWireParticlesPacket> STREAM_CODEC = StreamCodec.composite(
            Node.STREAM_CODEC, SendWireParticlesPacket::node1,
            Node.STREAM_CODEC, SendWireParticlesPacket::node2,
            ParticleTypes.STREAM_CODEC, SendWireParticlesPacket::options,
            SendWireParticlesPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        BlockState state1 = player.level().getBlockState(node1.sourcePos());
        BlockState state2 = player.level().getBlockState(node2.sourcePos());
        Vec3 pos1;
        if (state1.getBlock() instanceof DeviceBlock db)
            pos1 = node1.toGlobalPos(db.getNodePosition(player.level(), node1.sourcePos(), state1, node1.id()));
        else
            pos1 = node1.sourcePos().getCenter();

        Vec3 pos2;
        if (state2.getBlock() instanceof DeviceBlock db)
            pos2 = node2.toGlobalPos(db.getNodePosition(player.level(), node2.sourcePos(), state2, node2.id()));
        else
            pos2 = node2.sourcePos().getCenter();

        List<Vec3> cablePoints = QuadraticWireHelper.cablePoints(pos1, pos2, 1);

        for (int i = 0; i < cablePoints.size() - 1; i++) {
            Vec3 point = cablePoints.get(i);
            Vec3 nextPoint = cablePoints.get(i + 1);
            if (player.level().random.nextFloat() > 0.8) {
                point = VecHelper.lerp(player.level().random.nextFloat(), point, nextPoint);
                player.level().addParticle(options, point.x, point.y, point.z, 0, 0, 0);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_WIRE_PARTICLE;
    }
}
