package com.george_vi.electroenergetics.content.varistor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class VaristorBlock extends DirectionalRolledDeviceBlock implements IBE<VaristorBlockEntity> {
    public VaristorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.VARISTOR;
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof VaristorBlockEntity be) {
            tag.putDouble("VoltageAtOneAmp", be.voltageAtOneAmp.value);
            tag.putFloat("Tangent", 0.4f);
        }
        return tag;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEEShapes.ELECTRONIC_10_ROLL.get(state.getValue(FACING));
        return CEEShapes.ELECTRONIC_10.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEENodeConfigurations.MOMENTARY_SWITCH.rotate(new Vec3(0, -90, 0)).getNodes(state.getValue(FACING));
        return CEENodeConfigurations.MOMENTARY_SWITCH.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEENodeConfigurations.MOMENTARY_SWITCH.rotate(new Vec3(0, -90, 0)).getNodePos(state.getValue(FACING), id);
        return CEENodeConfigurations.MOMENTARY_SWITCH.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public Class<VaristorBlockEntity> getBlockEntityClass() {
        return VaristorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends VaristorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.VARISTOR.get();
    }
}
