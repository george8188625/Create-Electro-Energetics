package com.george_vi.electroenergetics.content.potentiometer;

import com.george_vi.electroenergetics.content.fuse.FuseHolderDevice;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ResistanceScrollValueBehaviour;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PotentiometerBlockEntity extends KineticBlockEntity {

    protected ResistanceScrollValueBehaviour resistance;
    protected float progress;
    protected LerpedFloat smoothProgress = LerpedFloat.linear();

    public PotentiometerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        resistance = new ResistanceScrollValueBehaviour(CEELang.translate("resistor.resistance").component(), this, new ValueBox());
        resistance.withCallback(i -> this.updateResistance());
        behaviours.add(resistance);
    }

    @Override
    public void tick() {
        if (level instanceof ServerLevel sl) {
            float prevProgress = progress;
            progress = Mth.clamp(progress + getSpeed() / 1000, 0, 1);
            if (prevProgress != progress) {
                PotentiometerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, PotentiometerDevice.class);

                if (device != null)
                    device.progress = progress;
                sendData();
            }
        } else {
            smoothProgress.tickChaser();
        }
    }

    private void updateResistance() {
        if (!(level instanceof ServerLevel sl))
            return;

        PotentiometerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, PotentiometerDevice.class);

        if (device != null)
            device.resistance = resistance.getResistance();
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        progress = tag.getFloat("Progress");
        if (clientPacket)
            smoothProgress.chase(progress, 0.1, LerpedFloat.Chaser.LINEAR);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Progress", progress);
    }

    @Override
    public int getFlickerScore() {
        // Makes create not destroy the block when it's speed is repeatedly changed.
        return 0;
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 2, 14);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal() && !state.getValue(PotentiometerBlock.HORIZONTAL_FACING).equals(direction);
        }
    }
}
