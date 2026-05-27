package com.george_vi.electroenergetics.content.wire.interaction;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

public class WireInteractionHandler {

    public static NodeConnectionPoint targetedPoint = null;
    public static Vec3 targetedPos = Vec3.ZERO;

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null)
            return;

        ItemStack stackInHand = mc.player.getMainHandItem();

        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stackInHand, mc.player))
                .findFirst().orElse(null);
        if (behaviour == null) {
            targetedPoint = null;
            return;
        }

        mc.getProfiler().push("rayCastWire");
        double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        Vec3 from = getTraceOrigin(mc.player);
        Vec3 to = getTraceTarget(mc.player, range, from);
        HitResult hit = mc.hitResult;

        double bestDist = range;
        if (hit != null)
            bestDist = hit.getLocation().distanceTo(from);

        InWorldNodeConnection bestConnection = null;
        float bestProgress = 0;
        Vec3 bestPosition = null;
        WireData bestWireData = null;
        double bestWirePointDistance = 0;

        for (Pair<InWorldNodeConnection, WireData> wire : WireRenderer.getAllConnections()) {
            InWorldNodeConnection connection = wire.getFirst();

            Vec3 pos1 = connection.node1().getPosition(mc.level);
            Vec3 pos2 = connection.node2().getPosition(mc.level);

            if (pos1 == null || pos2 == null) {
                pos1 = connection.node1().sourcePos().getCenter();
                pos2 = connection.node2().sourcePos().getCenter();
            }

            double wirePointsDistance = pos1.distanceTo(pos2);
            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wire.getSecond().getSag(wirePointsDistance), 1f);

            double miny = pos1.y;
            for (Vec3 point : points)
                miny = Math.min(miny, point.y());

            AABB wireBB = new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z).setMinY(miny).inflate(0.1);
            if (!wireBB.contains(from) && wireBB.clip(from, to).isEmpty())
                continue;

            for (int i = 0; i < points.size(); i++) {
                Vec3 prevPoint = i == 0 ? pos1 : points.get(i - 1);
                Vec3 point = points.get(i);
                Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
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

                if (points.size() == 1)
                    bestProgress = (float) t / (points.size());
                if (points.size() == 2) {
                    if (i == 0 && t < 0.5)
                        continue;
                    bestProgress = (float) Mth.lerp(t, i - 1, i + 1) / (points.size());
                } else {
                    if (t < 0)
                        continue;
                    bestProgress = (float) Mth.lerp(t, i - 1, i + 1) / (points.size() - 0.5f);
                }
                bestPosition = VecHelper.lerp((float) t, prevPoint, nextPoint);
                bestWireData = wire.getSecond();
                bestWirePointDistance = wirePointsDistance;
            }
        }

        mc.getProfiler().pop();

        if (bestPosition == null) {
            targetedPoint = null;
            return;
        }

        targetedPoint = new NodeConnectionPoint(bestConnection.node1(), bestConnection.node2(), bestProgress);
        Vec3 pos1 = targetedPoint.node1().getPosition(mc.level);
        Vec3 pos2 = targetedPoint.node2().getPosition(mc.level);

        if (pos1 == null || pos2 == null)
            return;

        bestPosition = QuadraticWireHelper.posAt(pos1, pos2, targetedPoint.point(), bestWireData.getSag(bestWirePointDistance));
        targetedPos = bestPosition;
        WireInteractionBehaviour.DisplayType displayType = behaviour.getWireDisplayType(targetedPoint, mc.level, mc.player, stackInHand);
        if (displayType == WireInteractionBehaviour.DisplayType.DOT) {
            Outliner.getInstance().chaseAABB("cee_wire_interaction_point", AABB.ofSize(bestPosition, 0.01, 0.01, 0.01))
                    .lineWidth(0.15f)
                    .colored(behaviour.getWireDisplayColor(targetedPoint, mc.level, mc.player, stackInHand))
                    .disableLineNormals();
        } else if (displayType == WireInteractionBehaviour.DisplayType.LINE) {

            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, bestWireData.getSag(bestWirePointDistance), 1f);
            points.add(pos2);

            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 point = points.get(i);
                Vec3 nextPoint = points.get(i + 1);

                Outliner.getInstance().showLine("cee_wire_interaction_line_" + i, point, nextPoint)
                        .lineWidth(0.07f * 16 * bestWireData.wireType().getThickness())
                        .colored(behaviour.getWireDisplayColor(targetedPoint, mc.level, mc.player, stackInHand))
                        .disableLineNormals();
            }
        }

    }


    // From Create from RaycastHelper. That class got moved in 6.0.7, so it is copied here to ensure the mod is compatible with both 6.0.6 and 6.0.7.
    public static Vec3 getTraceTarget(Player playerIn, double range, Vec3 origin) {
        float f = playerIn.getXRot();
        float f1 = playerIn.getYRot();
        float f2 = Mth.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = Mth.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -Mth.cos(-f * 0.017453292F);
        float f5 = Mth.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        return origin.add((double) f6 * range, (double) f5 * range, (double) f7 * range);
    }

    public static Vec3 getTraceOrigin(Player playerIn) {
        double d0 = playerIn.getX();
        double d1 = playerIn.getY() + (double) playerIn.getEyeHeight();
        double d2 = playerIn.getZ();
        return new Vec3(d0, d1, d2);
    }
}
