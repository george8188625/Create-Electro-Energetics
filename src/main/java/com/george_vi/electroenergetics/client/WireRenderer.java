package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterRenderer;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.WirePoints;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixins.LevelRendererAccessor;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;

public class WireRenderer {
    public static List<Pair<InWorldNodeConnection, WireData>> WIRE_CONNECTIONS = new ArrayList<>();

    private static final Map<InWorldNode, String> NODE_LABELS = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    protected static Map<InWorldNodeConnection, WireEffect> WIRE_EFFECTS = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    protected static Map<CatenaryConnection, WireEffect> CATENARY_EFFECTS = new HashMap<>();
    public static List<CatenaryConnection> CATENARY = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    public static void render(LevelRenderer levelRenderer, PoseStack pose, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        float partialTick = AnimationTickHolder.getPartialTicks();
        ClientLevel level = mc.level;
        if (level == null)
            return;
        
        level.getProfiler().push("renderWires");
        if (!(levelRenderer instanceof LevelRendererAccessor acc))
            return;

        MultiBufferSource buffer = acc.electroEnergetics$getRenderBuffers().bufferSource();

        LinemansStickRenderer.renderFirstPerson(level, pose, buffer, camera);
        ClampMeterRenderer.renderFirstPerson(level, pose, buffer, camera);

        Map<InWorldNode, List<Vec3>> outerInsulatorJumpers = new HashMap<>();

        pose.pushPose();
        pose.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());

        float baseCatenaryWidth = 0.66f;
        boolean renderImmediately = !VisualizationManager.supportsVisualization(level);
        if (CEEConfigs.client().disableFlywheelWireRendering.get())
            renderImmediately = true;
        Vec3 cameraPosition = mc.gameRenderer.getMainCamera().getPosition();
        if (renderImmediately)
            for (CatenaryConnection connection : CATENARY) {
                BlockPos pos1 = connection.pos1();
                BlockPos pos2 = connection.pos2();

                if (!InWorldNode.isPosFullyLoadable(level, pos1) ||
                        !InWorldNode.isPosFullyLoadable(level, pos2))
                    continue;
                
                BlockState startingState = level.getBlockState(pos1);
                BlockState endingState = level.getBlockState(pos2);
                AABB bb = AABB.encapsulatingFullBlocks(pos1, pos2);
                if (!levelRenderer.getFrustum().isVisible(bb))
                    continue;

                Vec3 start = Vec3.atCenterOf(pos1).add(0, -0.5, 0);
                Vec3 end = Vec3.atCenterOf(pos2).add(0, -0.5, 0);

                List<Vec3> lowerWirePoints = QuadraticWireHelper.cablePoints(start, end, 0, 10);

                for (int i = 0; i < lowerWirePoints.size(); i++) {
                    Vec3 point = lowerWirePoints.get(i);

                    if (point.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                        continue;

                    Vec3 nextPoint = i == lowerWirePoints.size() - 1 ? end : lowerWirePoints.get(i + 1);

                    // the reason for catenaries to have variable width, is that when the player is far away, normal-width catenaries look out of place, and they give off a vibe of something solid or smth??

                    float catenaryThickness = (float) Mth.clamp(baseCatenaryWidth / (cameraPosition.distanceTo(point) / 30), 0.4f, baseCatenaryWidth);

                    CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                            .translate(point)
                            .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                            .scale(catenaryThickness, catenaryThickness, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                            .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                    maxLightLevel(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                            LevelRenderer.getLightColor(level, BlockPos.containing(nextPoint))))
                            .renderInto(pose, buffer.getBuffer(RenderType.solid()));
                }
                boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
                boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
                if (isStartingLow || isEndingLow)
                    continue;

                Vec3 topStart = start.add(0, 1.5, 0);
                Vec3 topEnd = end.add(0, 1.5, 0);

                float distance = (float) topStart.distanceTo(topEnd);


                List<Vec3> upperWirePoints = QuadraticWireHelper.cablePoints(topStart, topEnd, 350f * (0.05f / distance), 4);

                for (int i = 0; i < upperWirePoints.size(); i++) {
                    Vec3 point = upperWirePoints.get(i);

                    if (point.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                        continue;

                    Vec3 nextPoint = i == upperWirePoints.size() - 1 ? topEnd : upperWirePoints.get(i + 1);

                    // the reason for catenaries to have variable width, is that when the player is far away, normal-width catenaries look out of place, and they give off a vibe of something solid or smth??

                    float catenaryThickness = (float) Mth.clamp(baseCatenaryWidth / (cameraPosition.distanceTo(point) / 30), 0.4f, baseCatenaryWidth);

                    CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                            .translate(point)
                            .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                            .scale(catenaryThickness, catenaryThickness, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                            .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                    maxLightLevel(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                            LevelRenderer.getLightColor(level, BlockPos.containing(nextPoint))))
                            .renderInto(pose, buffer.getBuffer(RenderType.solid()));
                    if (i == 0)
                        continue;
                    if (i == upperWirePoints.size() - 1)
                        if (point.distanceToSqr(topEnd) < 1.3)
                            continue;
                    Vec3 closest;
                    Vec3 ab = end.subtract(start);
                    Vec3 ap = point.subtract(start);
                    double denom = ab.lengthSqr();
                    if (denom == 0)
                        closest = start;
                    else {
                        double t = ap.dot(ab) / denom;
                        t = Mth.clamp(t, 0, 1);
                        closest = start.add(ab.scale(t));
                    }

                    CachedBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                            .translate(point)
                            .rotateY((float) Math.atan2(closest.x() - point.x(), closest.z() - point.z()))
                            .rotateX(-(float) Math.atan2(closest.y - point.y, Math.hypot(closest.x - point.x, closest.z - point.z)))
                            .scale(catenaryThickness * 0.66f, catenaryThickness * 0.66f, (float) (point.distanceTo(closest) * 2) + 0.02f)
                            .light(BlockPos.containing(point).equals(BlockPos.containing(closest)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(closest).scale(0.5))) :
                                    maxLightLevel(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                            LevelRenderer.getLightColor(level, BlockPos.containing(closest))))
                            .rotateZDegrees(45)
                            .renderInto(pose, buffer.getBuffer(RenderType.solid()));
                }
            }

        for (Pair<InWorldNodeConnection, WireData> wire : getAllConnections()) {
            InWorldNodeConnection connection = wire.getFirst();
            WireData wireData = wire.getSecond();

            BlockState state1 = level.getBlockState(connection.node1().sourcePos());
            BlockState state2 = level.getBlockState(connection.node2().sourcePos());

            Vec3 pos1 = connection.node1().getPosition(level, partialTick);
            Vec3 pos2 = connection.node2().getPosition(level, partialTick);

            if (pos1 == null)
                pos1 = connection.node1().sourcePos().getCenter();
            if (pos2 == null)
                pos2 = connection.node2().sourcePos().getCenter();

            boolean isBlock1Outer = state1.getBlock() instanceof ElectricalDeviceBlock<?> db &&
                    db.isOuterInsulator(level, connection.node1().sourcePos(), state1, connection.node1().id());
            boolean isBlock2Outer = state2.getBlock() instanceof ElectricalDeviceBlock<?> db &&
                    db.isOuterInsulator(level, connection.node2().sourcePos(), state2, connection.node2().id());
            double distance = pos1.distanceTo(pos2);

            mc.getProfiler().popPush("renderBirds");
            IntSet birdPositions = WireEffects.birds.get(connection);
            if (birdPositions != null) {
                WirePoints wirePoints = QuadraticWireHelper.wirePoints(pos1, pos2, wire.getSecond().getSag(distance));
                for (int birdPos : birdPositions) {
                    if (birdPos >= wirePoints.size())
                        continue;

                    renderBird(wirePoints.get(birdPos), pos1, pos2, pose, buffer);
                }
            }

            mc.getProfiler().popPush("renderWireAttachments");
            for (Pair<Float, WireAttachment> attachment : wireData.attachments()) {
                WireAttachment attachmentType = attachment.getSecond();
                float positionOnWire = attachment.getFirst();
                mc.getProfiler().push(CEERegistries.WIRE_ATTACHMENT_TYPE.getKey(attachmentType.type).toString());

                Vec3 offset = QuadraticWireHelper.posAt(pos1, pos2, positionOnWire, wireData.getSag(distance));
                float elevation = QuadraticWireHelper.pointElevationInDegrees(pos1, pos2, positionOnWire, wireData.getSag(distance));

                if (offset.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                    continue;

                pose.pushPose();
                PoseTransformStack msr = TransformStack.of(pose);
                msr.translate(offset);
                double angleY = Math.toDegrees(Math.atan2(pos2.x - pos1.x, pos2.z - pos1.z)) + 90;
                msr.rotateYDegrees((float) angleY);
                msr.rotateXDegrees(180);
                int light = LevelRenderer.getLightColor(level, BlockPos.containing(offset));
                attachmentType.type.render(pose, buffer, levelRenderer, attachmentType, offset, light, elevation);
                pose.popPose();
                mc.getProfiler().pop();
            }
            mc.getProfiler().popPush("renderWires");

            if (isBlock1Outer || isBlock2Outer || renderImmediately) {
                if (!connection.isFullyLoaded(level))
                    continue;
                List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance));
                List<Vec3> renderedPoints = CEEConfigs.client().wireLOD.get() ?
                        QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance), cameraPosition) :
                        QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance));

                if (renderImmediately)
                    level(renderedPoints, pos1, pos2, pose, buffer, levelRenderer, wireData.wireType(), level);

                if (points.size() >= 10) {
                    if (isBlock1Outer) {
                        Vec3 nextPoint = points.get(5);
                        if (nextPoint.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                            continue;
                        CachedBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                                .translate(pos1)
                                .rotateY((float) Math.atan2(nextPoint.x() - pos1.x(), nextPoint.z() - pos1.z()))
                                .rotateX(-(float) Math.atan2(nextPoint.y - pos1.y, 
                                        Math.hypot(nextPoint.x - pos1.x, nextPoint.z - pos1.z)))
                                .light(LevelRenderer.getLightColor(level, 
                                        BlockPos.containing(pos1.add(nextPoint).scale(0.5))))
                                .renderInto(pose, buffer.getBuffer(RenderType.solid()));

                        outerInsulatorJumpers.computeIfAbsent(connection.node1(), (k) -> new ArrayList<>())
                                .add(nextPoint);
                    }

                    if (isBlock2Outer) {
                        Vec3 nextPoint = points.get(points.size() - 6);
                        if (nextPoint.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                            continue;
                        CachedBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                                .translate(pos2)
                                .rotateY((float) Math.atan2(nextPoint.x() - pos2.x(), nextPoint.z() - pos2.z()))
                                .rotateX(-(float) Math.atan2(nextPoint.y - pos2.y,
                                        Math.hypot(nextPoint.x - pos2.x, nextPoint.z - pos2.z)))
                                .light(LevelRenderer.getLightColor(level,
                                        BlockPos.containing(pos2.add(nextPoint).scale(0.5))))
                                .renderInto(pose, buffer.getBuffer(RenderType.solid()));

                        outerInsulatorJumpers.computeIfAbsent(connection.node2(), (k) -> new ArrayList<>())
                                .add(nextPoint);
                    }
                }
            }
        }

        for (Map.Entry<InWorldNode, List<Vec3>> e : outerInsulatorJumpers.entrySet()) {
            List<Vec3> positionsToConnect = e.getValue();

            for (int i = 0; i < positionsToConnect.size() - 1; i++) {
                Vec3 pos1 = positionsToConnect.get(i);
                Vec3 pos2 = positionsToConnect.get(i + 1);

                List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, 3);

                level(points, pos1, pos2, pose, buffer, levelRenderer, CEEWireTypes.IRON.get(), level);
            }
        }

        pose.popPose();

        level.getProfiler().pop();
    }

    public static void level(List<Vec3> points, Vec3 pos1, Vec3 pos2, PoseStack pose, MultiBufferSource buffer, LevelRenderer levelRenderer, WireType wireType, BlockAndTintGetter level) {
        Minecraft mc = Minecraft.getInstance();
        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        if (!levelRenderer.getFrustum().isVisible(new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z).setMinY(miny)))
            return;

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);

            if (point.distanceTo(mc.gameRenderer.getMainCamera().getPosition()) > CEEConfigs.client().wireRenderDistance.get())
                continue;
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            CachedBuffers.partial(wireType.getModel(), Blocks.ANDESITE.defaultBlockState())
                    .translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                            maxLightLevel(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                    LevelRenderer.getLightColor(level, BlockPos.containing(nextPoint))))
                    .renderInto(pose, buffer.getBuffer(RenderType.solid()));
        }
    }

    public static void renderWire(List<Vec3> points, Vec3 pos1, Vec3 pos2, PoseStack pose, MultiBufferSource buffer, WireType wireType, BlockAndTintGetter level) {
        Minecraft mc = Minecraft.getInstance();
        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);

            if (point.distanceTo(mc.gameRenderer.getMainCamera().getPosition()) > CEEConfigs.client().wireRenderDistance.get())
                continue;
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            BlockPos pointBlockPos = BlockPos.containing(point);
            BlockPos nextPointBlockPos = BlockPos.containing(nextPoint);
            CachedBuffers.partial(wireType.getModel(), Blocks.ANDESITE.defaultBlockState())
                    .translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(pointBlockPos.equals(nextPointBlockPos) ?
                            LevelRenderer.getLightColor(level,
                                    BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                            maxLightLevel(LevelRenderer.getLightColor(level, pointBlockPos),
                                    LevelRenderer.getLightColor(level, nextPointBlockPos)))
                    .renderInto(pose, buffer.getBuffer(wireType.renderType()));
        }
    }

    public static void forceRenderWire(List<Vec3> points, Vec3 pos1, Vec3 pos2, PoseStack pose, MultiBufferSource buffer, WireType wireType, BlockAndTintGetter level) {

        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);

            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            BlockPos pointBlockPos = BlockPos.containing(point);
            BlockPos nextPointBlockPos = BlockPos.containing(nextPoint);
            CachedBuffers.partial(wireType.getModel(), Blocks.ANDESITE.defaultBlockState())
                    .translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(pointBlockPos.equals(nextPointBlockPos) ?
                            LevelRenderer.getLightColor(level,
                                    BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                            maxLightLevel(LevelRenderer.getLightColor(level, pointBlockPos),
                                    LevelRenderer.getLightColor(level, nextPointBlockPos)))
                    .renderInto(pose, buffer.getBuffer(wireType.renderType()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderBird(Vec3 pos, Vec3 wirePos1, Vec3 wirePos2, PoseStack pose, MultiBufferSource buffer) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        CachedBuffers.partial(CEEPartialModels.BIRB, Blocks.ANDESITE.defaultBlockState())
                .translate(pos)
                .rotateY((float) Mth.atan2(wirePos2.x() - wirePos1.x(), wirePos2.z() - wirePos1.z()))
                .light(LevelRenderer.getLightColor(mc.level, BlockPos.containing(pos)))
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
    }

    public static void removeConnections(InWorldNodeConnection toRemove) {
        WIRE_CONNECTIONS.removeIf(w -> w.getFirst().equals(toRemove));
        WireEffects.birds.remove(toRemove);
        WireEffect we = WIRE_EFFECTS.remove(toRemove);
        if (we != null)
            VisualizationHelper.queueRemove(we);
    }

    public static List<Pair<InWorldNodeConnection, WireData>> getAllConnections() {
       return List.copyOf(WIRE_CONNECTIONS);
    }

    public static WireData getConnectionData(InWorldNodeConnection connection) {
        for (Pair<InWorldNodeConnection, WireData> wire : getAllConnections()) {
            if (wire.getFirst().equals(connection))
                return wire.getSecond();
        }
        return null;
    }

    public static void addConnection(InWorldNodeConnection newConnection, WireData data) {
        for (int i = 0; i < WIRE_CONNECTIONS.size(); i++) {
            if (WIRE_CONNECTIONS.get(i).getFirst().equals(newConnection)) {
                WIRE_CONNECTIONS.remove(i);
                break;
            }
        }
        WIRE_CONNECTIONS.add(Pair.of(newConnection, data));
        if (CEEConfigs.client().disableFlywheelWireRendering.get())
            return;

        WireEffect we = new WireEffect(Minecraft.getInstance().level, newConnection, data.wireType(), data);
        WireEffect owe = WIRE_EFFECTS.put(newConnection, we);
        if (owe != null)
            VisualizationHelper.queueRemove(owe);
        VisualizationHelper.queueAdd(we);
        WIRE_EFFECTS.put(newConnection, we);
    }

    public static void clearAllWireConnections() {
        WIRE_CONNECTIONS.clear();
        WireEffects.birds.clear();

        for (WireEffect we : WIRE_EFFECTS.values())
            VisualizationHelper.queueRemove(we);
        WIRE_EFFECTS.clear();
    }

    public static void addCatenary(CatenaryConnection connection) {
        if (!CATENARY.contains(connection.swap())) {
            if (!CATENARY.contains(connection)) {
                CATENARY.add(connection);
                if (CEEConfigs.client().disableFlywheelWireRendering.get())
                    return;
                WireEffect we = new WireEffect(Minecraft.getInstance().level, connection, CEEWireTypes.STANDARD.get(),
                        new WireData(CEEWireTypes.STANDARD.get(), 0, Collections.emptyList(), 0));
                VisualizationHelper.queueAdd(we);
                CATENARY_EFFECTS.put(connection, we);
            }
        }
    }

    public static void removeCatenary(CatenaryConnection connection) {
        CATENARY.removeIf(c -> c.equals(connection.swap()));
        CATENARY.removeIf(c -> c.equals(connection));
        WireEffect we = CATENARY_EFFECTS.get(connection);
        WireEffect weSwapped = CATENARY_EFFECTS.get(connection.swap());
        if (we != null)
            VisualizationHelper.queueRemove(we);
        if (weSwapped != null)
            VisualizationHelper.queueRemove(weSwapped);
    }

    public static void removeVoltageData(InWorldNode node) {
        NodeVoltageHolder.NODE_VOLTAGES.remove(node);
    }

    public static void clearAllCatenaryConnections() {
        for (WireEffect we : CATENARY_EFFECTS.values())
            VisualizationHelper.queueRemove(we);

        CATENARY_EFFECTS.clear();
        CATENARY.clear();
    }

    public static void recreateVisuals() {
        for (WireEffect we : CATENARY_EFFECTS.values())
            VisualizationHelper.queueRemove(we);
        for (WireEffect we : WIRE_EFFECTS.values())
            VisualizationHelper.queueRemove(we);
        CATENARY_EFFECTS.clear();
        WIRE_EFFECTS.clear();
        if (CEEConfigs.client().disableFlywheelWireRendering.get())
            return;

        for (CatenaryConnection connection : CATENARY) {
            WireEffect we = new WireEffect(Minecraft.getInstance().level, connection, CEEWireTypes.STANDARD.get(),
                    new WireData(CEEWireTypes.STANDARD.get(), 0, Collections.emptyList(), 0));
            VisualizationHelper.queueAdd(we);
            CATENARY_EFFECTS.put(connection, we);
        }

        for (Pair<InWorldNodeConnection, WireData> connection : WIRE_CONNECTIONS) {
            WireEffect we = new WireEffect(Minecraft.getInstance().level, connection.getFirst(), connection.getSecond().wireType(),
                    connection.getSecond());
            VisualizationHelper.queueAdd(we);
            WIRE_EFFECTS.put(connection.getFirst(), we);
        }
    }

    public static void setNodeLabel(InWorldNode node, @Nullable String label) {
        if (label == null)
            NODE_LABELS.remove(node);
        else
            NODE_LABELS.put(node, label);
    }

    public static @Nullable String getNodeLabel(InWorldNode node) {
        return NODE_LABELS.get(node);
    }

    public static Map<InWorldNode, String> getNodeLabels() {
        return NODE_LABELS;
    }

    public static int maxLightLevel(int lightColo1, int lightColor2) {
        return LightTexture.pack(Math.max(LightTexture.block(lightColo1), LightTexture.block(lightColor2)), Math.max(LightTexture.sky(lightColo1), LightTexture.sky(lightColor2)));
    }
}
