package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WireVisual implements EffectVisual<WireEffect>, LightUpdatedVisual {
    private final NodeConnection connection;
    private final WireType wireType;
    private final VisualizationContext visualizationContext;

    private Vec3 prevPos1;
    private Vec3 prevPos2;
    protected final List<TransformedInstance> instances = new ArrayList<>();
    final LongSet lightSections;

    public WireVisual(VisualizationContext visualizationContext, NodeConnection connection, WireType wireType) {
        this.visualizationContext = visualizationContext;
        this.connection = connection;
        this.wireType = wireType;

        ClientLevel level = Minecraft.getInstance().level;
        Vec3 pos1 = connection.node1().getPosition(level);
        Vec3 pos2 = connection.node2().getPosition(level);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }

        prevPos1 = pos1;
        prevPos2 = pos2;

        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireType.getSag());

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                    .createInstance();
            instance.translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))) :
                            Math.max(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                    LevelRenderer.getLightColor(level, BlockPos.containing(nextPoint))));
            instances.add(instance);
        }

        lightSections = new LongOpenHashSet();

        int minSectionX = SectionPos.blockToSectionCoord(Math.min(pos1.x, pos2.x));
        int minSectionY = SectionPos.blockToSectionCoord(Math.min(pos1.y, pos2.y));
        int minSectionZ = SectionPos.blockToSectionCoord(Math.min(pos1.z, pos2.z));
        int maxSectionX = SectionPos.blockToSectionCoord(Math.max(pos1.x, pos2.x));
        int maxSectionY = SectionPos.blockToSectionCoord(Math.max(pos1.y, pos2.y));
        int maxSectionZ = SectionPos.blockToSectionCoord(Math.max(pos1.z, pos2.z));

        for (int x = minSectionX; x <= maxSectionX; x++)
            for (int y = minSectionY; y <= maxSectionY; y++)
                for (int z = minSectionZ; z <= maxSectionZ; z++)
                    lightSections.add(SectionPos.asLong(x, y, z));

    }

    @Override
    public void update(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 pos1 = connection.node1().getPosition(level);
        Vec3 pos2 = connection.node2().getPosition(level);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }

        if (pos1.equals(prevPos1) && pos2.equals(prevPos2))
            return;
        prevPos1 = pos1;
        prevPos2 = pos2;
        for (TransformedInstance instance : instances)
            instance.delete();

        instances.clear();
        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireType.getSag());

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                    .createInstance();
            instance.translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))) :
                            Math.max(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                    LevelRenderer.getLightColor(level, BlockPos.containing(nextPoint))));
            instances.add(instance);
        }
    }

    @Override
    public void delete() {
        for (TransformedInstance instance : instances)
            instance.delete();

        instances.clear();
    }

    @Override
    public void updateLight(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 pos1 = connection.node1().getPosition(level);
        Vec3 pos2 = connection.node2().getPosition(level);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }

        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireType.getSag());

        boolean useOld = true;
        if (instances.size() != points.size()) {
            for (TransformedInstance instance : instances)
                instance.delete();
            instances.clear();
            useOld = false;
        }

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            TransformedInstance instance = useOld ? instances.get(i).setIdentityTransform() : visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                    .createInstance();
            instance.translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LevelRenderer.getLightColor(level, BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5))) :
                            Math.max(LevelRenderer.getLightColor(level, BlockPos.containing(point)),
                                    LevelRenderer.getLightColor(level, BlockPos.containing(nextPoint))));
            instance.setChanged();
            if (!useOld)
                instances.add(instance);
        }
    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        collector.sections(lightSections);
    }
}
