package com.george_vi.electroenergetics.content.wire.interaction;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.WireData;
import com.george_vi.electroenergetics.simulation.WireType;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
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

        ItemStack stackInHand = mc.player.getMainHandItem();

        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stackInHand))
                .findFirst().orElse(null);
        if (behaviour == null)
            return;

        mc.getProfiler().push("rayCastWire");
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
        WireType bestWireType = null;

        for (Pair<NodeConnection, WireData> wire : WireRenderer.getAllConnections()) {
            NodeConnection connection = wire.getFirst();

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

            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wire.getSecond().wireType().getSag(), 1f);

            double miny = pos1.y;
            for (Vec3 point : points)
                miny = Math.min(miny, point.y());

            AABB wireBB = new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z).setMinY(miny).inflate(0.1);
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

                bestProgress = (float) Mth.lerp(t, i - 1, i + 1) / (points.size() - 1);
                bestPosition = VecHelper.lerp((float) t, prevPoint, nextPoint);
                bestWireType = wire.getSecond().wireType();
            }
        }

        mc.getProfiler().pop();

        if (bestPosition == null) {
            targetedPoint = null;
            return;
        }

        targetedPoint = new NodeConnectionPoint(bestConnection.node1(), bestConnection.node2(), bestProgress);

        if (behaviour.getWireDisplayType(targetedPoint, mc.level, mc.player, stackInHand) == WireInteractionBehaviour.DisplayType.DOT) {
            Outliner.getInstance().chaseAABB("cee_wire_interaction_point", AABB.ofSize(bestPosition, 0.01, 0.01, 0.01))
                    .lineWidth(0.15f)
                    .colored(behaviour.getWireDisplayColor(targetedPoint, mc.level, mc.player, stackInHand))
                    .disableLineNormals();
        } else {
            Vec3 pos1 = null;
            Vec3 pos2 = null;

            BlockState state1 = mc.level.getBlockState(targetedPoint.node1().sourcePos());
            BlockState state2 = mc.level.getBlockState(targetedPoint.node2().sourcePos());

            if (state1.getBlock() instanceof DeviceBlock db)
                pos1 = db.getNodePosition(mc.level, targetedPoint.node1().sourcePos(), state1, targetedPoint.node1().id());
            if (state2.getBlock() instanceof DeviceBlock db)
                pos2 = db.getNodePosition(mc.level, targetedPoint.node2().sourcePos(), state2, targetedPoint.node2().id());

            if (pos1 == null || pos2 == null)
                return;

            pos1 = targetedPoint.node1().toGlobalPos(pos1);
            pos2 = targetedPoint.node2().toGlobalPos(pos2);

            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, bestWireType.getSag(), 1f);
            points.add(pos2);

            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 point = points.get(i);
                Vec3 nextPoint = points.get(i + 1);

                Outliner.getInstance().showLine("cee_wire_interaction_line_" + i, point, nextPoint)
                        .lineWidth(0.07f)
                        .colored(behaviour.getWireDisplayColor(targetedPoint, mc.level, mc.player, stackInHand))
                        .disableLineNormals();
            }
        }

    }
}
