package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.WirePoints;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WireVisual implements EffectVisual<WireEffect>, LightUpdatedVisual, SimpleTickableVisual, SimpleDynamicVisual {
    private final InWorldNodeConnection connection;
    private final WireType wireType;
    private final WireData wireData;
    private final WirePoints points = new WirePoints(0);
    private final VisualizationContext visualizationContext;

    private double prevLength;
    private Vec3 prevPos1;
    private Vec3 prevPos2;
    private final List<TransformedInstance> instances = new ArrayList<>();
    private TransformedInstance startInstance;
    private TransformedInstance endInstance;

    protected SectionCollector lightCollector;
    protected boolean lightsDirty;
    protected final LongSet lightSections = new LongOpenHashSet();

    public WireVisual(VisualizationContext visualizationContext, InWorldNodeConnection connection, WireType wireType, WireData wireData) {
        this.visualizationContext = visualizationContext;
        this.connection = connection;
        this.wireType = wireType;
        this.wireData = wireData;

        PartialModel endpointModel = wireType.getEndPointModel();
        if (endpointModel != null) {
            startInstance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(endpointModel))
                    .createInstance();
            startInstance.setVisible(false);

            endInstance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(endpointModel))
                    .createInstance();
            endInstance.setVisible(false);
        }
    }

    public void recreateInstances(ClientLevel level, float partialTick, boolean light) {
        Vec3 pos1 = connection.node1().getPosition(level, partialTick);
        Vec3 pos2 = connection.node2().getPosition(level, partialTick);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }

        boolean isFullyLoaded = connection.isFullyLoaded(level);

        // Check if chunks are loaded on the client
        boolean chunksLoaded = level.isLoaded(BlockPos.containing(pos1)) && level.isLoaded(BlockPos.containing(pos2));
        if (CEEConfigs.client().renderWiresOnUnloadedChunks.get())
            chunksLoaded = true;
        // Clear instances if not loaded
        if (!isFullyLoaded || !chunksLoaded) {
            if (!instances.isEmpty()) {
                for (TransformedInstance instance : instances)
                    instance.delete();
                instances.clear();
            }
            if (startInstance != null)
                startInstance.setVisible(false);
            if (endInstance != null)
                endInstance.setVisible(false);
            return;
        }

        Vec3i origin = visualizationContext.renderOrigin();
        pos1 = pos1.subtract(origin.getX(), origin.getY(), origin.getZ());
        pos2 = pos2.subtract(origin.getX(), origin.getY(), origin.getZ());

        if (pos1.equals(prevPos1) && pos2.equals(prevPos2) && prevLength == wireData.length && !light)
            return;

        prevPos1 = pos1;
        prevPos2 = pos2;
        prevLength = wireData.length;
        lightsDirty = true;

        double distance = pos1.distanceTo(pos2);
        if (wireType.shouldScaleLast())
            QuadraticWireHelper.wirePoints(pos1, pos2, wireData.getSag(distance), points);
        else
            QuadraticWireHelper.wirePointsRaw(pos1, pos2, wireData.getSag(distance), 1, points);
        createWire(visualizationContext, wireType, points, pos2, level);

        PartialModel endpointModel = wireType.getEndPointModel();
        if (endpointModel == null)
            return;

        if (startInstance == null)
            startInstance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                            Models.partial(endpointModel)).createInstance();

        if (endInstance == null)
            endInstance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                            Models.partial(endpointModel)).createInstance();

        if (points.size() < 2) {
            startInstance.setVisible(false);
            endInstance.setVisible(false);
            return;
        }

        Vec3 start = points.get(0);
        BlockPos startBlockPos = BlockPos.containing(start);
        Vec3 startNext = points.get(1);
        BlockPos startNextBlockPos = BlockPos.containing(startNext);
        Vec3 end = pos2;
        BlockPos endBlockPos = BlockPos.containing(end);
        Vec3 endNext = points.get(points.size() - 1);
        BlockPos endNextBlockPos = BlockPos.containing(endNext);

        startInstance.setVisible(true);
        startInstance.setIdentityTransform()
                .translate(start)
                .rotateY((float) Mth.atan2(startNext.x() - start.x(), startNext.z() - start.z()))
                .rotateX(-(float) Mth.atan2(startNext.y - start.y, Math.hypot(startNext.x - start.x, startNext.z - start.z)))
                .rotateZ(0.001f)
                .light(startBlockPos.equals(startNextBlockPos) ? LevelRenderer.getLightColor(level, startNextBlockPos) :
                        WireRenderer.maxLightLevel(LevelRenderer.getLightColor(level, startNextBlockPos),
                                LevelRenderer.getLightColor(level, startBlockPos)))
                .setChanged();

        endInstance.setVisible(true);
        endInstance.setIdentityTransform()
                .translate(end)
                .rotateY((float) Mth.atan2(endNext.x() - end.x(), endNext.z() - end.z()))
                .rotateX(-(float) Mth.atan2(endNext.y - end.y, Math.hypot(endNext.x - end.x, endNext.z - end.z)))
                .rotateZ(0.001f)
                .light(endBlockPos.equals(endNextBlockPos) ? LevelRenderer.getLightColor(level, endNextBlockPos) :
                        WireRenderer.maxLightLevel(LevelRenderer.getLightColor(level, endNextBlockPos),
                                LevelRenderer.getLightColor(level, endBlockPos)))
                .setChanged();

    }

    @Override
    public void update(float partialTick) {
    }

    @Override
    public void delete() {
        for (TransformedInstance instance : instances)
            instance.delete();

        instances.clear();
        if (startInstance != null)
            startInstance.delete();
        if (endInstance != null)
            endInstance.delete();
    }

    @Override
    public void updateLight(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        recreateInstances(level, partialTick, true);
    }

    private void createWire(VisualizationContext visualizationContext, WireType wireType, WirePoints points, Vec3 pos2,
                            ClientLevel level) {
        // The reason that it doesn't remove instances here, is it's not safe to do this here. It just sets to invisible.
        boolean renderEnds = wireType.shouldScaleLast();
        int usedInstances = 0;
        for (int i = 0; i < points.size(); i++) {
            usedInstances++;
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            TransformedInstance instance;

            if (i >= instances.size()) {
                instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(wireType.getModel()))
                        .createInstance();
                instances.add(instance);
            } else
                instance = instances.get(i);

            BlockPos pointBlockPos = BlockPos.containing(point).offset(visualizationContext.renderOrigin());
            BlockPos nextBlockPos = BlockPos.containing(nextPoint).offset(visualizationContext.renderOrigin());
            BlockPos middleBlockPos = BlockPos.containing(VecHelper.lerp(0.5f, point, nextPoint)).offset(visualizationContext.renderOrigin());
            if (!renderEnds && (i == 0 || i == points.size() - 1)) {
                instance.setVisible(false);
                continue;
            }
            instance.setVisible(true);
            instance.setIdentityTransform()
                    .translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .rotateZ(0.001f)
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(pointBlockPos.equals(nextBlockPos) ? LevelRenderer.getLightColor(level, middleBlockPos) :
                            WireRenderer.maxLightLevel(LevelRenderer.getLightColor(level, pointBlockPos),
                                    LevelRenderer.getLightColor(level, nextBlockPos)))
                    .setChanged();
        }
        for (int i = usedInstances; i < instances.size(); i++) {
            instances.get(i).setVisible(false);
            instances.get(i).setChanged();
        }
    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        lightCollector = collector;
    }

    @Override
    public void tick(TickableVisual.Context context) {
        if (lightsDirty) {
            lightsDirty = false;
            lightSections.clear();
            points.forEachSection(lightSections::add);
            lightCollector.sections(lightSections);
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        recreateInstances(level, ctx.partialTick(), false);
    }
}
