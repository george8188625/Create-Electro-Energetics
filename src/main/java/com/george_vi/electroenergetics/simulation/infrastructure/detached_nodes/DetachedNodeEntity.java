package com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes;

import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DetachedNodeEntity extends Entity {
    public int detachedNodeID;

    public DetachedNodeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        applyGravity();
        Vec3 deltaMovement = this.getDeltaMovement();
        deltaMovement = deltaMovement.scale(0.9);

        if (level() instanceof ServerLevel level) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(level);
            InWorldNodeData nodeData = sd.getNodeData(DetachedNodeHelper.getFromId(detachedNodeID));
            // Remove entity if node no longer exists
            if (nodeData == null || !Objects.equals(nodeData.detachedNodeEntityId, getUUID())) {
                discard();
                return;
            }
            Vec3 prevPos = position();
            Vec3 targetPos = prevPos.add(deltaMovement);
            Vec3 targetSum = Vec3.ZERO;
            int targets = 0;
            for (Int2ObjectMap.Entry<WireData> e : nodeData.adjacency.int2ObjectEntrySet()) {
                int id = e.getIntKey();
                InWorldNodeData adjacentData = sd.getNodeData(id);
                if (adjacentData == null)
                    continue;

                Vec3 adjacentPos = adjacentData.getGlobalPos();
                WireData wireData = e.getValue();

                double distance = adjacentPos.distanceTo(targetPos);
                double length = wireData.length;
                boolean isRigid = wireData.wireType().getSag() <= 0.2f;
                double stretch = (distance + Math.min(length * 0.1f, isRigid ? 0 : 1)) - length;
                if (stretch != 0) {
                    Vec3 diff = adjacentPos.subtract(targetPos)
                            .normalize()
                            .scale(isRigid ? stretch : Math.max(0, stretch));
                    targets++;
                    targetSum = targetSum.add(targetPos.add(diff));
                }
            }

            if (targets != 0)
                targetPos = targetSum.scale(1f / targets);

            deltaMovement = targetPos.subtract(prevPos);
            if (deltaMovement.lengthSqr() > 0.25f)
                deltaMovement = deltaMovement.normalize().scale(0.5f);
            setDeltaMovement(deltaMovement);
            move(MoverType.SELF, deltaMovement);
        }
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);

        if (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED)
            if (level() instanceof ServerLevel level) {
                InfrastructureSavedData sd = InfrastructureSavedData.load(level);
                InWorldNodeData nodeData = sd.getNodeData(DetachedNodeHelper.getFromId(detachedNodeID));
                if (nodeData != null && Objects.equals(nodeData.detachedNodeEntityId, getUUID()))
                    sd.removeNode(nodeData.node);
            }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(@NotNull Entity entity) {
        return canVehicleCollide(this, entity);
    }

    // from Boat
    public static boolean canVehicleCollide(Entity vehicle, Entity entity) {
        return (entity.canBeCollidedWith() || entity.isPushable()) && !vehicle.isPassengerOfSameVehicle(entity);
    }

    @Override
    public void push(@NotNull Entity entity) {
        if (entity instanceof Boat) {
            if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
                super.push(entity);
            }
        } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
            super.push(entity);
        }
    }
    //

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        detachedNodeID = tag.getInt("DetachedNodeID");
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("DetachedNodeID", detachedNodeID);
    }
}
