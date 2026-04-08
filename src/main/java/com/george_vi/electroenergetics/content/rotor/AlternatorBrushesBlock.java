package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.DirectionalKineticElectricBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class AlternatorBrushesBlock extends DirectionalKineticElectricBlock<AlternatorBrushesDevice> implements IBE<AlternatorBrushesBlockEntity> {

    public AlternatorBrushesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ALTERNATOR_BRUSHES.get(state.getValue(FACING));
    }

    @Override
    public SimulatedDeviceType<AlternatorBrushesDevice> getDevice() {
        return CEESimulatedDevices.ALTERNATOR_BRUSHES.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(FACING).getAxis().isHorizontal())
            return Map.of(0, new Vec3(8/16f, 2/16f, 8/16f), 1, new Vec3(8/16f, 14/16f, 8/16f));
        return Map.of(0, new Vec3(8/16f, 8/16f, 2/16f), 1, new Vec3(8/16f, 8/16f, 14/16f));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(FACING).getAxis().isHorizontal())
            return new Vec3(8/16f, id == 0 ? 2/16f : 14/16f, 8/16f);
        return new Vec3(8/16f, 8/16f, id == 0 ? 2/16f : 14/16f);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<AlternatorBrushesBlockEntity> getBlockEntityClass() {
        return AlternatorBrushesBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AlternatorBrushesBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ALTERNATOR_BRUSHES.get();
    }
}
