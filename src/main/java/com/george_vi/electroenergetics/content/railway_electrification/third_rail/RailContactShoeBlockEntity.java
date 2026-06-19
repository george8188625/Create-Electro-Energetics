package com.george_vi.electroenergetics.content.railway_electrification.third_rail;

import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlock;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.ConnectionEntry;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireSimulationState;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class RailContactShoeBlockEntity extends SmartBlockEntity {

    public float distanceY = 0.125f;
    public float prevDistanceY = 0.125f;

    AttachedNode attachedNode;
    WireSimulationState.WireCutHandle handle;
    InWorldNodeConnection lastConnection = null;

    public RailContactShoeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        assert level != null;
        prevDistanceY = distanceY;

        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, worldPosition);

        if (subLevelAccess == null) {
            if (level.isClientSide)
                handleOnClient(p -> p, p -> p);
            else
                handleOnServer(p -> p, p -> p);
        } else {
            if (level.isClientSide)
                handleOnClient(subLevelAccess.logicalPose()::transformPosition, subLevelAccess.logicalPose()::transformPositionInverse);
            else
                handleOnServer(subLevelAccess.logicalPose()::transformPosition, subLevelAccess.logicalPose()::transformPositionInverse);
        }
    }


    private void disconnect() {
        if (level instanceof ServerLevel serverLevel && handle != null) {
            if (handle.valid()) {
                InfrastructureSavedData sd = InfrastructureSavedData.load(serverLevel);
                sd.wireSimulationState.invalidateHandle(handle);
            }
            attachedNode = null;
            handle = null;
            lastConnection = null;
        }
    }

    private void handleOnClient(UnaryOperator<Vec3> positionTransform, UnaryOperator<Vec3> inversePositionTransform) {
        float halfPantoReach = 0.5f;
        Vec3 pantographPos = new Vec3(0.5f, -0.125f, 0.5f).add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

        Vec3 connectionPoint = null;

        for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.WIRE_CONNECTIONS) {
            if (connection.getSecond().wireType().getSag() != 0)
                continue;
            Vec3 start = connection.getFirst().node1().getPosition(level);
            Vec3 end = connection.getFirst().node2().getPosition(level);
            if (start == null || end == null)
                continue;
            start = inversePositionTransform.apply(start);
            end = inversePositionTransform.apply(end);
            float halfThickness = connection.getSecond().wireType().getThickness() * 0.5f;
            float progress = closestPointOnWire(start, end, pantographPos);
            Vec3 cp = checkCatenary(start, end, pantographPos, progress, halfPantoReach);
            if (cp != null) {
                if (connectionPoint == null || connectionPoint.y < cp.y)
                    connectionPoint = cp.add(0, halfThickness, 0);
            }
        }

        distanceY = connectionPoint == null ? (Math.min(0.05f, distanceY + 0.05f)) : (float)- (connectionPoint.y - worldPosition.getY() + 0.25f);
    }

    private void handleOnServer(UnaryOperator<Vec3> positionTransform, UnaryOperator<Vec3> inversePositionTransform) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(serverLevel);

        float halfPantoReach = 0.5f;
        Vec3 pantographPos = new Vec3(0.5f, -0.125f, 0.5f).add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

        Vec3 connectionPoint = null;
        InWorldNodeConnection connectedConnection = null;
        float connectionProgress = 0;

        Vec3 inWorldPos = positionTransform.apply(pantographPos);
        long sectionPos = SectionPos.asLong(Mth.floor(inWorldPos.x) >> 4, Mth.floor(inWorldPos.y) >> 4, Mth.floor(inWorldPos.z) >> 4);
        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> connection : sd.wireSimulationState.getConnectionsInSection(sectionPos).entrySet()) {
            if (connection.getValue().wireData.wireType().getSag() != 0)
                continue;
            Vec3 start = connection.getKey().node1().getPosition(level);
            Vec3 end = connection.getKey().node2().getPosition(level);
            if (start == null || end == null)
                continue;
            start = inversePositionTransform.apply(start);
            end = inversePositionTransform.apply(end);
            float halfThickness = connection.getValue().wireData.wireType().getThickness() * 0.5f;
            float progress = closestPointOnWire(start, end, pantographPos);
            Vec3 cp = checkCatenary(start, end, pantographPos, progress, halfPantoReach);
            if (cp != null) {
                if (connectionPoint == null || connectionPoint.y > cp.y) {
                    connectionPoint = cp.add(0, halfThickness + 0.015f, 0);
                    connectedConnection = connection.getKey();
                    connectionProgress = progress;
                }
            }
        }

        if (connectionPoint != null) {
            if (handle == null || !handle.valid())
                handle = sd.wireSimulationState.createHandle("Pantograph-"+worldPosition.toShortString());

            if (lastConnection == null) {
                sd.wireSimulationState.removeCutsFrom(handle);
                attachedNode = sd.wireSimulationState.createCut(handle, connectedConnection, connectionProgress);
            } else if (!lastConnection.equals(connectedConnection) ||
                    !sd.wireSimulationState.cutExists(handle, attachedNode)) {
                sd.wireSimulationState.removeCutsFrom(handle);
                attachedNode = sd.wireSimulationState.createCut(handle, connectedConnection, connectionProgress);
            } else {
                sd.wireSimulationState.relocateCut(handle, attachedNode, connectionProgress);
            }

            lastConnection = connectedConnection;

        } else {
            disconnect();
        }
    }


    Vec3 checkCatenary(Vec3 start, Vec3 end, Vec3 pantographPos, float closestPointOnWire, float halfPantoReach) {

        Vec3 closest = VecHelper.lerp(closestPointOnWire, start, end);

        Vec3 distance = pantographPos.subtract(closest);
        float xTol = (float) ((distance.y + halfPantoReach) * 0.2f + 0.125f);
        if (Math.abs(distance.z) < 0.5f && Math.abs(distance.x) < xTol && Math.abs(distance.y) < halfPantoReach)
            return closest;

        return null;
    }

    float closestPointOnWire(Vec3 start, Vec3 end, Vec3 checker) {
        double t = 0;

        Vec3 ab = end.subtract(start);
        Vec3 ap = checker.subtract(start);
        double denom = ab.lengthSqr();
        if (denom != 0)
            t = ap.dot(ab) / denom;
        if (t < -0.01)
            return 0;
        if (t > 1.01)
            return 1;
        return (float) t;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        distanceY = tag.getFloat("DistanceY");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("DistanceY", distanceY);
    }
}
