package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
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

public class CapacitorBlock extends DirectionalRolledDeviceBlock implements IBE<CapacitorBlockEntity> {
    public CapacitorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CAPACITOR;
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof CapacitorBlockEntity be)
            tag.putDouble("Capacitance", be.indexToCapacitance(be.capacitance.value));
        return tag;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.CAPACITOR.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEENodeConfigurations.ELECTRONIC_10.rotate(new Vec3(0, -90, 0)).getNodes(state.getValue(FACING));
        return CEENodeConfigurations.ELECTRONIC_10.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEENodeConfigurations.ELECTRONIC_10.rotate(new Vec3(0, -90, 0)).getNodePos(state.getValue(FACING), id);
        return CEENodeConfigurations.ELECTRONIC_10.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public Class<CapacitorBlockEntity> getBlockEntityClass() {
        return CapacitorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CapacitorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.CAPACITOR.get();
    }
}
