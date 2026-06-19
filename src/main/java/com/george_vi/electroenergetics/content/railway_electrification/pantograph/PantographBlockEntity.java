package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographBlockEntity extends SmartBlockEntity {
    public float prevExtensionState = 0;
    public float currentExtensionState = 0;
    public float targetExtensionState = 0.85f;
    public boolean extended = true;
    public DyeColor color = DyeColor.WHITE;

    AttachedNode attachedNode;
    WireSimulationState.WireCutHandle handle;
    InWorldNodeConnection lastConnection = null;

    public PantographBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        prevExtensionState = currentExtensionState;
        currentExtensionState = currentExtensionState < targetExtensionState ? Math.min(targetExtensionState, currentExtensionState + 0.05f) : Math.max(targetExtensionState, currentExtensionState - 0.05f);

        if (level.isClientSide && currentExtensionState == targetExtensionState && prevExtensionState != currentExtensionState)
            level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.25, worldPosition.getZ() + 0.5, SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.1f, 1f, false);

        if (!extended) {
            targetExtensionState = 0;
            disconnect();
            return;
        }

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

    private void handleOnClient(UnaryOperator<Vec3> positionTransform, UnaryOperator<Vec3> inversePositionTransform) {
        boolean isDouble = getBlockState().getValue(PantographBlock.DOUBLE);
        double pantographX = getBlockState().getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.25 : 0.75;
        if (isDouble)
            pantographX = getBlockState().getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 1;
        Direction.Axis axis = getBlockState().getValue(FACING).getAxis();
        float halfPantoReach = 1.625f;

        Vec3 pantographPos = new Vec3(axis == Direction.Axis.X ? pantographX : 0.5, halfPantoReach, axis == Direction.Axis.Z ? pantographX : 0.5).add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

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
                if (connectionPoint == null || connectionPoint.y > cp.y) {
                    connectionPoint = cp.add(0, -halfThickness + 0.015f, 0);
                }
            }
        }

        for (CatenaryConnection connection : WireRenderer.CATENARY) {
            Vec3 start = connection.getStartPos();
            Vec3 end = connection.getEndingPos();

            start = inversePositionTransform.apply(start);
            end = inversePositionTransform.apply(end);
            float progress = closestPointOnWire(start, end, pantographPos);
            Vec3 cp = checkCatenary(start, end, pantographPos, progress, halfPantoReach);
            if (cp != null) {
                if (connectionPoint == null || connectionPoint.y > cp.y) {
                    connectionPoint = cp;
                }
            }
        }


        if (CEEConfigs.client().debugPantographRange.get())
            for (float x = -3; x < 3; x += 0.3f) {
                for (float y = -3; y < 3; y += 0.3f) {
                    for (float z = -3; z < 3; z += 0.3f) {
                        Vec3 pos = pantographPos.add(x, y, z);
                        if (checkCatenary(pos, pos, pantographPos, 0f, halfPantoReach) != null) {
                            pos = positionTransform.apply(pos);
                            level.addParticle(ParticleTypes.SCULK_CHARGE_POP, pos.x, pos.y, pos.z, 0, 0, 0);
                        }
                    }
                }
            }

        if (connectionPoint != null) {
            float lo = 0;
            float hi = 2.7f;
            for (int i = 0; i < 20; i++) {
                float m1 = lo + (hi - lo) / 3;
                float m2 = hi - (hi - lo) / 3;
                if (Math.abs(getConnectorPos(m1).y - connectionPoint.y) < Math.abs(getConnectorPos(m2).y - connectionPoint.y))
                    hi = m2;
                else
                    lo = m1;
            }
            float newExtensionState = (lo + hi) / 2;
            if (targetExtensionState == currentExtensionState && targetExtensionState != 0)
                currentExtensionState = newExtensionState;

            targetExtensionState = newExtensionState;
        } else {
            targetExtensionState = 1f;
        }
    }

    private void handleOnServer(UnaryOperator<Vec3> positionTransform, UnaryOperator<Vec3> inversePositionTransform) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(serverLevel);

        boolean isDouble = getBlockState().getValue(PantographBlock.DOUBLE);
        double pantographX = getBlockState().getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.25 : 0.75;
        if (isDouble)
            pantographX = getBlockState().getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 1;
        Direction.Axis axis = getBlockState().getValue(FACING).getAxis();
        float halfPantoReach = 1.625f;

        Vec3 pantographPos = new Vec3(axis == Direction.Axis.X ? pantographX : 0.5, halfPantoReach, axis == Direction.Axis.Z ? pantographX : 0.5).add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

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
                    connectionPoint = cp.add(0, -halfThickness + 0.015f, 0);
                    connectedConnection = connection.getKey();
                    connectionProgress = progress;
                }
            }
        }

        for (CatenaryConnection connection : sd.getAllCatenaryConnections()) {
            Vec3 start = connection.getStartPos();
            Vec3 end = connection.getEndingPos();

            start = inversePositionTransform.apply(start);
            end = inversePositionTransform.apply(end);
            float progress = closestPointOnWire(start, end, pantographPos);
            Vec3 cp = checkCatenary(start, end, pantographPos, progress, halfPantoReach);
            if (cp != null) {
                if (connectionPoint == null || connectionPoint.y > cp.y) {
                    connectionPoint = cp;
                    connectedConnection = new InWorldNodeConnection(
                            new InWorldNode(0, connection.pos1()),
                            new InWorldNode(0, connection.pos2()));
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
            targetExtensionState = 1f;
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

    Vec3 getConnectorPos(float extensionState) {
        Direction.Axis axis = getBlockState().getValue(FACING).getAxis();
        if (getBlockState().getValue(PantographBlock.DOUBLE)) {
            float lowerArmRadians = (-90 + extensionState * 27) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5 + 0.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;

            float b = (float) (-0.4f - Math.abs(armHingePosX));
            float a = Mth.sqrt(-b * b + 2f * 2f);

            double pantographX = getBlockState().getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 1;

            Vec3 connectorPlatePos = new Vec3(axis == Direction.Axis.X ? pantographX : 0.5f, 0.1875 + a + armHingePosY, axis == Direction.Axis.Z ? pantographX : 0.5f);
            connectorPlatePos = connectorPlatePos.add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            return connectorPlatePos;
        }

        float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

        double armHingePosY = Mth.cos(lowerArmRadians) * 1.875 + 0.5;
        double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

        float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
        double pantographX = getBlockState().getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.25 : 0.75;
        Vec3 connectorPlatePos = new Vec3((axis == Direction.Axis.X ? pantographX : 0.5), armHingePosY, armHingePosX + (axis == Direction.Axis.Z ? pantographX : 0.5))
                .add(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9);
        connectorPlatePos = connectorPlatePos.add(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return connectorPlatePos;
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

    Vec3 checkCatenary(Vec3 start, Vec3 end, Vec3 pantographPos, float closestPointOnWire, float halfPantoReach) {

        Vec3 closest = VecHelper.lerp(closestPointOnWire, start, end);
        Vec3 distance = pantographPos.subtract(closest);

        distance = VecHelper.rotate(distance, getBlockState().getValue(FACING).toYRot() + 90, Direction.Axis.Y);
        float xTol = (float) ((-distance.y + halfPantoReach) * 0.2f + 0.125f);
        if (Math.abs(distance.z) < 1.5 && Math.abs(distance.x) < xTol && Math.abs(distance.y) < halfPantoReach)
            return closest;

        return null;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        color = DyeColor.byName(tag.getString("Color"), DyeColor.WHITE);
        targetExtensionState = tag.getFloat("TargetExtensionState");
        currentExtensionState = tag.getFloat("ExtensionState");
        prevExtensionState = tag.getFloat("PrevExtensionState");
        extended = tag.getBoolean("Extended");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Color", color.getSerializedName());
        tag.putFloat("TargetExtensionState", targetExtensionState);
        tag.putFloat("ExtensionState", currentExtensionState);
        tag.putFloat("PrevExtensionState", prevExtensionState);
        tag.putBoolean("Extended", extended);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(3);
    }
}
