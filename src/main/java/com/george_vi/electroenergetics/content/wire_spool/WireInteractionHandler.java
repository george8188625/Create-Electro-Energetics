package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.wire_spool.WireRenderer;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.simulator.DirectionSensitiveNodeConnection;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

public class WireInteractionHandler {

    public static NodeConnectionPoint targetedPoint = null;

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("rayCastWire");

        ItemStack stackInHand = mc.player.getMainHandItem();
        if (!stackInHand.is(AllTags.optionalTag(BuiltInRegistries.ITEM, CreateElecrtoEnergetics.rl("target_wire"))))
            return;
        double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        Vec3 from = RaycastHelper.getTraceOrigin(mc.player);
        Vec3 to = RaycastHelper.getTraceTarget(mc.player, range, from);
        HitResult hit = mc.hitResult;

        double bestDist = range;
        if (hit != null)
            bestDist = hit.getLocation().distanceTo(from);

        NodeConnection bestConnection = null;
        float bestProgress = 0;
        Vec3 bestPosition = null;

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

            pos1 = connection.node1().toGlobalPos(pos1);
            pos2 = connection.node2().toGlobalPos(pos2);

            List<Vec3> points = WireRenderer.cablePoints(pos1, pos2, 1, 1f);

            double miny = pos1.y;
            for (Vec3 point : points)
                miny = Math.min(miny, point.y());

            AABB wireBB = new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z).setMinY(miny);
            if (!wireBB.contains(from) && wireBB.clip(from, to).isEmpty())
                continue;

            for (int i = 1; i < points.size() - 1; i++) {
                Vec3 prevPoint = points.get(i - 1);
                Vec3 point = points.get(i);
                Vec3 nextPoint = points.get(i + 1);

                double distance = point.distanceTo(from);
                if (distance >= bestDist)
                    continue;
                Optional<Vec3> intersection = AABB.ofSize(point, 0.5, 0.5, 0.5).clip(from, to);
                if (intersection.isEmpty())
                    continue;

                bestConnection = connection;
                bestDist = distance;

                Vec3 ap = intersection.get().subtract(prevPoint);
                Vec3 ab = nextPoint.subtract(prevPoint);

                double ab_length_squared = ab.lengthSqr();
                double t = ab_length_squared == 0 ? 0.0 : ap.dot(ab) / ab_length_squared;

                bestProgress = (float) Mth.lerp(t, (double) (i - 1) / points.size(), (double) (i + 1) / points.size());
                bestPosition = VecHelper.lerp((float) t, prevPoint, nextPoint);

            }
        }

        mc.getProfiler().pop();

        if (bestPosition == null) {
            targetedPoint = null;
            return;
        }

        targetedPoint = new NodeConnectionPoint(bestConnection.node1(), bestConnection.node2(), bestProgress);

        Outliner.getInstance().chaseAABB("cee_wire_interaction_point", AABB.ofSize(bestPosition, 0.01, 0.01, 0.01))
                .lineWidth(0.1f)
                .disableLineNormals();

    }
}
