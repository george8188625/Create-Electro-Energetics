package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class WireEffects {

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused())
            return;

        for (NodeConnection connection : WireRenderer.getAllConnections()) {
            Vec3 pos1 = null;
            Vec3 pos2 = null;

            BlockState state1 = mc.level.getBlockState(connection.node1().sourcePos());
            BlockState state2 = mc.level.getBlockState(connection.node2().sourcePos());

            if (state1.getBlock() instanceof DeviceBlock db)
                pos1 = db.getNodePosition(mc.level, connection.node1().sourcePos(), state1, connection.node1().id());
            if (state2.getBlock() instanceof DeviceBlock db)
                pos2 = db.getNodePosition(mc.level, connection.node2().sourcePos(), state2, connection.node2().id());

            if (pos1 == null || pos2 == null)
                continue;

            BlockPos devicePos1 = connection.node1().sourcePos();
            BlockPos devicePos2 = connection.node2().sourcePos();
            pos1 = pos1.add(devicePos1.getX(), devicePos1.getY(), devicePos1.getZ());
            pos2 = pos2.add(devicePos2.getX(), devicePos2.getY(), devicePos2.getZ());

            List<Vec3> points = WireRenderer.cablePoints(pos1, pos2, 1);

            spawnDrippingWater(points);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnDrippingWater(List<Vec3> points) {
        Minecraft mc = Minecraft.getInstance();

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = points.get(i + 1);
            if (point.distanceTo(mc.player.getEyePosition()) > 80)
                continue;

            boolean bubbles = false;
            if (mc.level.random.nextFloat() > 0.02) {
                if (mc.level.random.nextFloat() > 0.03)
                    continue;
                bubbles = true;
            }

            Vec3 pos = VecHelper.lerp(mc.level.random.nextFloat(), point, nextPoint);
            if (mc.level.isRainingAt(BlockPos.containing(pos))) {
                if (bubbles)
                    mc.level.addParticle(ParticleTypes.SPLASH, pos.x(), pos.y(), pos.z(), 0, 0, 0);
                else
                    mc.level.addParticle(ParticleTypes.DRIPPING_WATER, pos.x(), pos.y() - 0.1, pos.z(), 0, 0, 0);
            }
        }
    }
}
