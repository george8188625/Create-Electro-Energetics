package com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.CapacitanceScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HVCapacitorBlockEntity extends SmartBlockEntity {
    public HVCapacitorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected CapacitanceScrollValueBehaviour capacitance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        capacitance = new CapacitanceScrollValueBehaviour(CEELang.translate("capacitor.capacitance").component(), this, new ValueBox());
        capacitance.withCallback(i -> this.updateCapacitance());
        behaviours.add(capacitance);
    }

    private void updateCapacitance() {
        if (!(level instanceof ServerLevel sl))
            return;
        HVCapacitorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, HVCapacitorDevice.class);

        if (device != null)
            device.capacitance = capacitance.getCapacitance();
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 13);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Vec3 location = super.getLocalOffset(level, pos, state);
            Direction facing = state.getValue(HVCapacitorBlock.FACING);
            if (state.getValue(HVCapacitorBlock.ROLL)) {
                if (facing == Direction.DOWN)
                    location = location.subtract(0, 8 / 16f, 0);
                else if (facing == Direction.SOUTH)
                    location = location.subtract(0, 4 / 16f, -4 / 16f);
                else if (facing == Direction.NORTH)
                    location = location.subtract(0, 4 / 16f, 4 / 16f);
                else if (facing == Direction.WEST)
                    location = location.subtract(4 / 16f, 4 / 16f, 0);
                else if (facing == Direction.EAST)
                    location = location.subtract(-4 / 16f, 4 / 16f, 0);
            } else if (facing.getAxis().isHorizontal()) {
                location = VecHelper.rotateCentered(location, -facing.toYRot() + (getSide() == Direction.DOWN ? 0 : 180), Direction.Axis.Y);
            }
            return location;
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            super.rotate(level, pos, state, ms);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            if (state.getValue(HVCapacitorBlock.FACING).getAxis().isVertical())
                return state.getValue(HVCapacitorBlock.ROLL) ?
                        direction.getAxis() == Direction.Axis.X : direction.getAxis() == Direction.Axis.Z;
            else if (!state.getValue(HVCapacitorBlock.ROLL))
                    return direction.getAxis().isVertical();
            return direction.getAxis().isHorizontal() && direction.getAxis() != state.getValue(HVCapacitorBlock.FACING).getAxis();
        }
    }
}
