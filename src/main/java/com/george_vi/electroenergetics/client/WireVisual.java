package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WireVisual implements EffectVisual<WireEffect>, LightUpdatedVisual, SimpleTickableVisual, SimpleDynamicVisual {
    private final InWorldNodeConnection connection;
    private final WireType wireType;
    private final WireData wireData;
    private final VisualizationContext visualizationContext;

    private double prevLength;
    private Vec3 prevPos1;
    private Vec3 prevPos2;
    protected final List<TransformedInstance> instances = new ArrayList<>();
    final LongSet lightSections;

    public WireVisual(VisualizationContext visualizationContext, InWorldNodeConnection connection, WireType wireType, WireData wireData) {
        this.visualizationContext = visualizationContext;
        this.connection = connection;
        this.wireType = wireType;
        this.wireData = wireData;

        ClientLevel level = Minecraft.getInstance().level;
        Vec3 pos1 = connection.node1().getPosition(level);
        Vec3 pos2 = connection.node2().getPosition(level);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }
        double distance = pos1.distanceTo(pos2);

        lightSections = new LongOpenHashSet();

        if (distance > 1000)
            return; // Wire is wrong. It's going to be updated at some point.

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

        pos1 = pos1.subtract(visualizationContext.renderOrigin().getX(), visualizationContext.renderOrigin().getY(), visualizationContext.renderOrigin().getZ());
        pos2 = pos2.subtract(visualizationContext.renderOrigin().getX(), visualizationContext.renderOrigin().getY(), visualizationContext.renderOrigin().getZ());
        prevPos1 = pos1;
        prevPos2 = pos2;

        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance));

        createWire(visualizationContext, wireType, points, pos2, level);

    }

    @Override
    public void update(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 pos1 = connection.node1().getPosition(level, partialTick);
        Vec3 pos2 = connection.node2().getPosition(level, partialTick);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }

        pos1 = pos1.subtract(visualizationContext.renderOrigin().getX(), visualizationContext.renderOrigin().getY(), visualizationContext.renderOrigin().getZ());
        pos2 = pos2.subtract(visualizationContext.renderOrigin().getX(), visualizationContext.renderOrigin().getY(), visualizationContext.renderOrigin().getZ());

        if (pos1.equals(prevPos1) && pos2.equals(prevPos2) && prevLength == wireData.length)
            return;

        prevPos1 = pos1;
        prevPos2 = pos2;
        prevLength = wireData.length;
        for (TransformedInstance instance : instances)
            instance.delete();

        instances.clear();
        double distance = pos1.distanceTo(pos2);
        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance));

        createWire(visualizationContext, wireType, points, pos2, level);
    }

    @Override
    public void delete() {
        for (TransformedInstance instance : instances)
            instance.delete();

        instances.clear();
    }

    @Override
    public void updateLight(float partialTick) {
        // forces it to recreate the wires on the next frame.
        prevLength = -1;
    }

    private void createWire(VisualizationContext visualizationContext, WireType wireType, List<Vec3> points, Vec3 pos2, ClientLevel level) {
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                    .createInstance();
            BlockPos pointBlockPos = BlockPos.containing(point).offset(visualizationContext.renderOrigin());
            BlockPos nextBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
            BlockPos middleBlockPos = BlockPos.containing(point.add(nextPoint).multiply(0.5, 0.5, 0.5)).offset(visualizationContext.renderOrigin());
            instance.translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .rotateZ(0.001f)
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                            WireRenderer.maxLightLevel(LevelRenderer.getLightColor(level, pointBlockPos),
                                    LevelRenderer.getLightColor(level, nextBlockPos)));
            instances.add(instance);
        }
    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        collector.sections(lightSections);
    }

    @Override
    public void tick(TickableVisual.Context context) {
//        update(0);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        update(ctx.partialTick());
    }
}
