package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.WireType;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CatenaryVisual implements EffectVisual<WireEffect>, LightUpdatedVisual, SimpleTickableVisual {
    private final CatenaryConnection connection;
    private final WireType wireType;
    private final VisualizationContext visualizationContext;

    protected final List<TransformedInstance> lowerInstances = new ArrayList<>();
    protected final List<TransformedInstance> upperInstances = new ArrayList<>();
    final LongSet lightSections;
    private boolean prevLow;

    public CatenaryVisual(VisualizationContext visualizationContext, CatenaryConnection connection, WireType wireType) {
        this.visualizationContext = visualizationContext;
        this.connection = connection;
        this.wireType = wireType;

        float wireWidth = 0.55f;
        float messengerWidth = 0.35f;

        ClientLevel level = Minecraft.getInstance().level;
        Vec3 start = Vec3.atBottomCenterOf(connection.pos1.subtract(visualizationContext.renderOrigin()));
        Vec3 end = Vec3.atBottomCenterOf(connection.pos2.subtract(visualizationContext.renderOrigin()));

        List<Vec3> lowerWirePoints = QuadraticWireHelper.cablePoints(start, end, 0, 10);

        for (int i = 0; i < lowerWirePoints.size(); i++) {
            Vec3 point = lowerWirePoints.get(i);
            Vec3 nextPoint = i == lowerWirePoints.size() - 1 ? end : lowerWirePoints.get(i + 1);
            TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                    .createInstance();
            BlockPos pointBlockPos = BlockPos.containing(point).offset(visualizationContext.renderOrigin());
            BlockPos nextBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
            BlockPos middleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5)).offset(visualizationContext.renderOrigin());
            instance.translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scale(wireWidth, wireWidth, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                            Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                    LevelRenderer.getLightColor(level, nextBlockPos)));
            lowerInstances.add(instance);
        }
        BlockState startingState = level.getBlockState(connection.pos1);
        BlockState endingState = level.getBlockState(connection.pos2);
        boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        prevLow = isStartingLow || isEndingLow;

        if (!prevLow) {
            Vec3 topStart = start.add(0, 1.5, 0);
            Vec3 topEnd = end.add(0, 1.5, 0);

            float distance = (float) topStart.distanceTo(topEnd);

            List<Vec3> upperWirePoints = QuadraticWireHelper.cablePoints(topStart, topEnd, 350f * (0.05f / distance), 4);

            for (int i = 0; i < upperWirePoints.size(); i++) {
                Vec3 point = upperWirePoints.get(i);

                Vec3 nextPoint = i == upperWirePoints.size() - 1 ? topEnd : upperWirePoints.get(i + 1);

                TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                        .createInstance();
                BlockPos pointBlockPos = BlockPos.containing(point).offset(visualizationContext.renderOrigin());
                BlockPos nextBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
                BlockPos middleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5)).offset(visualizationContext.renderOrigin());
                instance.translate(point)
                        .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                        .scale(wireWidth, wireWidth, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                        .light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                                Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                        LevelRenderer.getLightColor(level, nextBlockPos)));
                upperInstances.add(instance);
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
                    t = Math.max(0, Math.min(1, t));
                    closest = start.add(ab.scale(t));
                }

                TransformedInstance messengerInstance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                        .createInstance();
                BlockPos closestBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
                BlockPos closestMiddleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5)).offset(visualizationContext.renderOrigin());
                messengerInstance.translate(point)
                        .rotateY((float) Mth.atan2(closest.x() - point.x(), closest.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(closest.y - point.y, Math.hypot(closest.x - point.x, closest.z - point.z)))
                        .scale(messengerWidth, messengerWidth, (float) (point.distanceTo(closest) * 2) + 0.02f)
                        .light(pointBlockPos.equals(closestBlockPos) ? LevelRenderer.getLightColor(level, closestMiddleBlockPos) :
                                Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                        LevelRenderer.getLightColor(level, closestBlockPos)));
                upperInstances.add(messengerInstance);
            }
        }

        lightSections = new LongOpenHashSet();

        int minSectionX = SectionPos.blockToSectionCoord(Math.min(connection.pos1.getX(), connection.pos2.getX()));
        int minSectionY = SectionPos.blockToSectionCoord(Math.min(connection.pos1.getY(), connection.pos2.getY()));
        int minSectionZ = SectionPos.blockToSectionCoord(Math.min(connection.pos1.getZ(), connection.pos2.getZ()));
        int maxSectionX = SectionPos.blockToSectionCoord(Math.max(connection.pos1.getX(), connection.pos2.getX()));
        int maxSectionY = SectionPos.blockToSectionCoord(Math.max(connection.pos1.getY(), connection.pos2.getY()) + 1);
        int maxSectionZ = SectionPos.blockToSectionCoord(Math.max(connection.pos1.getZ(), connection.pos2.getZ()));

        for (int x = minSectionX; x <= maxSectionX; x++)
            for (int y = minSectionY; y <= maxSectionY; y++)
                for (int z = minSectionZ; z <= maxSectionZ; z++)
                    lightSections.add(SectionPos.asLong(x, y, z));

    }

    @Override
    public void update(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 start = Vec3.atBottomCenterOf(connection.pos1.subtract(visualizationContext.renderOrigin()));
        Vec3 end = Vec3.atBottomCenterOf(connection.pos2.subtract(visualizationContext.renderOrigin()));

        float wireWidth = 0.66f;

        BlockState startingState = level.getBlockState(connection.pos1);
        BlockState endingState = level.getBlockState(connection.pos2);
        boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean low = isStartingLow || isEndingLow;

        if (low && !prevLow) {
            for (TransformedInstance instance : upperInstances)
                instance.delete();
            upperInstances.clear();
        } else if (!low && prevLow) {
            Vec3 topStart = start.add(0, 1.5, 0);
            Vec3 topEnd = end.add(0, 1.5, 0);

            float distance = (float) topStart.distanceTo(topEnd);

            List<Vec3> upperWirePoints = QuadraticWireHelper.cablePoints(topStart, topEnd, 350f * (0.05f / distance), 4);

            for (int i = 0; i < upperWirePoints.size(); i++) {
                Vec3 point = upperWirePoints.get(i);

                Vec3 nextPoint = i == upperWirePoints.size() - 1 ? topEnd : upperWirePoints.get(i + 1);

                TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                        .createInstance();
                BlockPos pointBlockPos = BlockPos.containing(point).offset(visualizationContext.renderOrigin());
                BlockPos nextBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
                BlockPos middleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5)).offset(visualizationContext.renderOrigin());
                instance.translate(point)
                        .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                        .scale(wireWidth, wireWidth, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                        .light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                                Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                        LevelRenderer.getLightColor(level, nextBlockPos)));
                upperInstances.add(instance);
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
                    t = Math.max(0, Math.min(1, t));
                    closest = start.add(ab.scale(t));
                }

                TransformedInstance messengerInstance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                        .createInstance();
                BlockPos closestBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
                BlockPos closestMiddleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5)).offset(visualizationContext.renderOrigin());
                messengerInstance.translate(point)
                        .rotateY((float) Mth.atan2(closest.x() - point.x(), closest.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(closest.y - point.y, Math.hypot(closest.x - point.x, closest.z - point.z)))
                        .scale(wireWidth, wireWidth, (float) (point.distanceTo(closest) * 2) + 0.02f)
                        .light(pointBlockPos.equals(closestBlockPos) ? LevelRenderer.getLightColor(level, closestMiddleBlockPos) :
                                Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                        LevelRenderer.getLightColor(level, closestBlockPos)));
                upperInstances.add(messengerInstance);
            }
        }
        prevLow = low;
    }

    @Override
    public void delete() {
        for (TransformedInstance instance : lowerInstances)
            instance.delete();
        lowerInstances.clear();

        for (TransformedInstance instance : upperInstances)
            instance.delete();
        upperInstances.clear();
    }

    @Override
    public void updateLight(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 start = Vec3.atBottomCenterOf(connection.pos1);
        Vec3 end = Vec3.atBottomCenterOf(connection.pos2);

        List<Vec3> lowerWirePoints = QuadraticWireHelper.cablePoints(start, end, 0, 10);

        for (int i = 0; i < lowerWirePoints.size(); i++) {
            Vec3 point = lowerWirePoints.get(i);
            Vec3 nextPoint = i == lowerWirePoints.size() - 1 ? end : lowerWirePoints.get(i + 1);
            TransformedInstance instance = lowerInstances.get(i);
            BlockPos pointBlockPos = BlockPos.containing(point);
            BlockPos nextBlockPos = BlockPos.containing(nextPoint);
            BlockPos middleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5));
            instance.light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                    Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                            LevelRenderer.getLightColor(level, nextBlockPos)));
            instance.setChanged();
        }

        if (!prevLow) {
            Vec3 topStart = start.add(0, 1.5, 0);
            Vec3 topEnd = end.add(0, 1.5, 0);

            float distance = (float) start.distanceTo(topEnd);

            List<Vec3> upperWirePoints = QuadraticWireHelper.cablePoints(topStart, topEnd, 350f * (0.05f / distance), 4);

            int ii = 0;
            for (int i = 0; i < upperWirePoints.size(); i++) {
                Vec3 point = upperWirePoints.get(i);

                Vec3 nextPoint = i == upperWirePoints.size() - 1 ? topEnd : upperWirePoints.get(i + 1);

                TransformedInstance instance = upperInstances.get(ii++);
                BlockPos pointBlockPos = BlockPos.containing(point);
                BlockPos nextBlockPos = BlockPos.containing(nextPoint);
                BlockPos middleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5));
                instance.light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                        Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                LevelRenderer.getLightColor(level, nextBlockPos)));
                instance.setChanged();
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
                    t = Math.max(0, Math.min(1, t));
                    closest = start.add(ab.scale(t));
                }

                TransformedInstance messengerInstance = upperInstances.get(ii++);
                BlockPos closestBlockPos = BlockPos.containing(nextPoint);
                BlockPos closestMiddleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5));
                messengerInstance.light(pointBlockPos.equals(closestBlockPos) ? LevelRenderer.getLightColor(level, closestMiddleBlockPos) :
                        Math.max(LevelRenderer.getLightColor(level, pointBlockPos),
                                LevelRenderer.getLightColor(level, closestBlockPos)));
                messengerInstance.setChanged();
            }
        }

    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        collector.sections(lightSections);
    }

    @Override
    public void tick(Context context) {
        update(0);
    }
}
