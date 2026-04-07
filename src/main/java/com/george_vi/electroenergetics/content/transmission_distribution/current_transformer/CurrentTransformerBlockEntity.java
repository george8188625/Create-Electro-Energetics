package com.george_vi.electroenergetics.content.transmission_distribution.current_transformer;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ScalingScrollValueBehaviour;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CurrentTransformerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public CurrentTransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public ScalingScrollValueBehaviour scaling;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        scaling = new ScalingScrollValueBehaviour(CEELang.translateDirect("gauge.scaling"), this, new ValueBox());
        scaling.withCallback(i -> updateScale());
        behaviours.add(scaling);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
    }

    private void updateScale() {
        if (!(level instanceof ServerLevel sl))
            return;

        CurrentTransformerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, CurrentTransformerDevice.class);

        if (device != null) {
            device.ratio = scaling.getScale();
        }

        if (!getBlockState().getValue(CurrentTransformerBlock.BOTTOM))
            if (level.getBlockEntity(worldPosition.below()) instanceof CurrentTransformerBlockEntity be)
                be.scaling.setValue(scaling.getValue());
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 14, 13);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CurrentTransformerBlock.TOP) && direction.getAxis().isHorizontal() && direction.getAxis() != state.getValue(CurrentTransformerBlock.FACING).getAxis();
        }
    }
}
