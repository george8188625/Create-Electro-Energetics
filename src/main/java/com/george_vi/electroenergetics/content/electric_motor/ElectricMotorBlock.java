package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.List;
import java.util.Map;

public class ElectricMotorBlock extends DirectionalKineticBlock implements DeviceBlock, IBE<ElectricMotorBlockEntity>, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;


    public ElectricMotorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ELECTRIC_MOTOR.get(state.getValue(FACING));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        InfrastructureSavedData.load(level).addDevice(pos, CEESimulatedDevices.ELECTRIC_MOTOR, List.of(0, 1));
        super.tick(state, level, pos, random);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel sl && state.getBlock() != level.getBlockState(pos).getBlock()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            for (Node node : sd.getNodesAt(pos))
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.INSULATED_WIRE.asStack(sd.getConnections(node).size() * CEEConfigs.server().wiresPerSpool.get()));
            sd.removeDevice(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }


    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING).getAxis() == face.getAxis();
    }

    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.Y)
            return Map.of(new Vec3(2/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f), 0,
                    new Vec3(14/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f), 1);
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            return Map.of(new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f, 2/16f), 0,
                    new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f, 14/16f), 1);

        return Map.of(new Vec3(2/16f, 8/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f), 0,
                new Vec3(14/16f, 8/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f), 1);

    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.Y)
            return id == 0 ? new Vec3(2/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f) :
                    new Vec3(14/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f);
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            return id == 0 ? new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f, 2/16f) :
                    new Vec3((state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f, 8/16f, 14/16f);

        return id == 0 ? new Vec3(2/16f, 8/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f) :
                new Vec3(14/16f, 8/16f, (state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 13)/16f);

    }

    @Override
    public Class<ElectricMotorBlockEntity> getBlockEntityClass() {
        return ElectricMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectricMotorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ELECTRIC_MOTOR.get();
    }
}
