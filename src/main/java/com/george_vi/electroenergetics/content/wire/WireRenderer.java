package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.mixins.LevelRendererAccessor;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.WireData;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireRenderer {
    public static List<Pair<NodeConnection, WireData>> WIRE_CONNECTIONS = new ArrayList<>();
    public static Map<Node, Float> NODE_VOLTAGES = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void render(LevelRenderer levelRenderer, PoseStack pose, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        mc.level.getProfiler().push("renderWires");
        if (!(levelRenderer instanceof LevelRendererAccessor acc))
            return;

        MultiBufferSource buffer = acc.electroEnergetics$getRenderBuffers().bufferSource();

        Map<Node, List<Vec3>> outerInsulatorJumpers = new HashMap<>();

        pose.pushPose();
        pose.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());

        for (Pair<NodeConnection, WireData> wire : getAllConnections()) {
            NodeConnection connection = wire.getFirst();
            WireData wireData = wire.getSecond();

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

            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, 1);
            List<Vec3> renderedPoints = QuadraticWireHelper.cablePoints(pos1, pos2, 1, mc.gameRenderer.getMainCamera().getPosition());
            mc.getProfiler().popPush("renderWireAttachments");
            for (Pair<Float, WireAttachment> attachment : wire.getSecond().attachments()) {
                mc.getProfiler().push(CEERegistries.WIRE_ATTACHMENT_TYPE.getKey(attachment.getSecond().type).toString());
                pose.pushPose();
                Vec3 offset;
                if (wire.getFirst().node1().compareTo(wire.getFirst().node2()) > 0)
                    offset = QuadraticWireHelper.posAt(pos1, pos2, 1.0f - attachment.getFirst());
                else
                    offset = QuadraticWireHelper.posAt(pos1, pos2, attachment.getFirst());
                PoseTransformStack msr = TransformStack.of(pose);
                msr.translate(offset);
                double angleY = Math.toDegrees(Math.atan2(pos2.x - pos1.x, pos2.z - pos1.z)) + 90;
                msr.rotateYDegrees((float) angleY);
                msr.rotateXDegrees(180);
                int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(offset));
                attachment.getSecond().type.render(pose, buffer, levelRenderer, attachment.getSecond(), offset, light, QuadraticWireHelper.pointElevationInDegrees(pos1, pos2, attachment.getFirst()));
                pose.popPose();
                mc.getProfiler().pop();
            }
            mc.getProfiler().popPush("renderWires");

            renderWire(renderedPoints, pos1, pos2, pose, buffer, levelRenderer);

            if (points.size() >= 10) {
                if (state1.getBlock() instanceof DeviceBlock db &&
                        db.isOuterInsulator(mc.level, connection.node1().sourcePos(), state1, connection.node1().id())) {
                    Vec3 nextPoint = points.get(5);
                    CachedBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                            .translate(pos1)
                            .rotateY((float) Math.atan2(nextPoint.x() - pos1.x(), nextPoint.z() - pos1.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - pos1.y, Math.hypot(nextPoint.x - pos1.x, nextPoint.z - pos1.z)))
                            .light(LevelRenderer.getLightColor(mc.level, BlockPos.containing(pos1.add(nextPoint).multiply(0.5, 0.5, 0.5))))
                            .renderInto(pose, buffer.getBuffer(RenderType.SOLID));

                    if (!outerInsulatorJumpers.containsKey(connection.node1()))
                        outerInsulatorJumpers.put(connection.node1(), new ArrayList<>());
                    outerInsulatorJumpers.get(connection.node1()).add(nextPoint);
                }

                if (state2.getBlock() instanceof DeviceBlock db &&
                        db.isOuterInsulator(mc.level, connection.node2().sourcePos(), state2, connection.node2().id())) {
                    Vec3 nextPoint = points.get(points.size() - 6);
                    CachedBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                            .translate(pos2)
                            .rotateY((float) Math.atan2(nextPoint.x() - pos2.x(), nextPoint.z() - pos2.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - pos2.y, Math.hypot(nextPoint.x - pos2.x, nextPoint.z - pos2.z)))
                            .light(LevelRenderer.getLightColor(mc.level, BlockPos.containing(pos2.add(nextPoint).multiply(0.5, 0.5, 0.5))))
                            .renderInto(pose, buffer.getBuffer(RenderType.SOLID));

                    if (!outerInsulatorJumpers.containsKey(connection.node2()))
                        outerInsulatorJumpers.put(connection.node2(), new ArrayList<>());
                    outerInsulatorJumpers.get(connection.node2()).add(nextPoint);
                }
            }
        }

        for (Map.Entry<Node, List<Vec3>> e : outerInsulatorJumpers.entrySet()) {
            Node node = e.getKey();
            List<Vec3> positionsToConnect = e.getValue();

            for (int i = 0; i < positionsToConnect.size() - 1; i++) {
                Vec3 pos1 = positionsToConnect.get(i);
                Vec3 pos2 = positionsToConnect.get(i + 1);

                List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, 3);

                renderWire(points, pos1, pos2, pose, buffer, levelRenderer);
            }
        }

        pose.popPose();

        mc.level.getProfiler().pop();
    }

    public static void renderWire(List<Vec3> points, Vec3 pos1, Vec3 pos2, PoseStack pose, MultiBufferSource buffer, LevelRenderer levelRenderer) {
        Minecraft mc = Minecraft.getInstance();
        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        if (!levelRenderer.getFrustum().isVisible(new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z).setMinY(miny)))
            return;

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
                CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                        .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(mc.level, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))) :
                                Math.max(LevelRenderer.getLightColor(mc.level, BlockPos.containing(point)),
                                        LevelRenderer.getLightColor(mc.level, BlockPos.containing(nextPoint))))
                        .renderInto(pose, buffer.getBuffer(RenderType.SOLID));
        }
    }

    public static void removeConnections(List<NodeConnection> toRemove) {
        WIRE_CONNECTIONS.removeAll(toRemove);
    }

    public static void removeConnections(NodeConnection toRemove) {
        WIRE_CONNECTIONS.removeIf(w -> w.getFirst().equals(toRemove));
        WIRE_CONNECTIONS.remove(toRemove);
    }

    public static List<Pair<NodeConnection, WireData>> getAllConnections() {
       return List.copyOf(WIRE_CONNECTIONS);
    }

    public static WireData getConnectionData(NodeConnection connection) {
        for (Pair<NodeConnection, WireData> wire : getAllConnections()) {
            if (wire.getFirst().equals(connection))
                return wire.getSecond();
        }
        return null;
    }

    public static void addConnection(NodeConnection newConnection, WireData data) {
        for (int i = 0; i < WIRE_CONNECTIONS.size(); i++) {
            if (WIRE_CONNECTIONS.get(i).getFirst().equals(newConnection)) {
                WIRE_CONNECTIONS.remove(i);
                break;
            }
        }
        WIRE_CONNECTIONS.add(Pair.of(newConnection, data));
    }
    public static void removeVoltageData(Node node) {
        synchronized ("get_node_voltages") {
            NODE_VOLTAGES.remove(node);
        }
    }

    public static Map<Node, Float> getAllVoltages() {
        synchronized ("get_node_voltages") {
            return Map.copyOf(NODE_VOLTAGES);
        }
    }

    public static void addVoltageData(Node node, float voltage) {
        synchronized ("get_node_voltages") {
            if (!NODE_VOLTAGES.containsKey(node))
                NODE_VOLTAGES.put(node, voltage);
            else
                NODE_VOLTAGES.replace(node, voltage);
        }
    }
}
