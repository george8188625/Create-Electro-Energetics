package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.List;
import java.util.Map;

public class AlternatorBrushesBlock extends DirectionalKineticBlock implements DeviceBlock, IBE<AlternatorBrushesBlockEntity> {

    public AlternatorBrushesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ALTERNATOR_BRUSHES.get(state.getValue(FACING));
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        InfrastructureSavedData.load(level).addDevice(pos, CEESimulatedDevices.ALTERNATOR_BRUSHES, List.of(0, 1));
        super.tick(state, level, pos, random);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel sl && state.getBlock() != level.getBlockState(pos).getBlock()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            sd.removeDevice(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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
