package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
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

public class ResistorBlock extends DirectionalRolledDeviceBlock<ResistorDevice> implements IBE<ResistorBlockEntity> {
    public final boolean creative;

    public ResistorBlock(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
    }

    @Override
    public SimulatedDeviceType<ResistorDevice> getDevice() {
        return creative ? CEESimulatedDevices.CREATIVE_RESISTOR.get() : CEESimulatedDevices.RESISTOR.get();
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof ResistorBlockEntity be)
            tag.putDouble("Resistance", be.resistance.getResistance());
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
    public Class<ResistorBlockEntity> getBlockEntityClass() {
        return ResistorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ResistorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.RESISTOR.get();
    }
}
