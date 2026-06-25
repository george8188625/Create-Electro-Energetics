package com.george_vi.electroenergetics.content.potentiometer;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.List;
import java.util.Map;

public class RedstonePotentiometerBlock extends SimpleElectricalDeviceBlock<PotentiometerDevice> implements IBE<RedstonePotentiometerBlockEntity>, ProperOilAndWaterloggedBlock {
    public static final Property<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<LoggedState> LOGGED_STATE = ProperOilAndWaterloggedBlock.LOGGED_STATE;

    public RedstonePotentiometerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LOGGED_STATE, LoggedState.DRY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOGGED_STATE, HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection()
                .getOpposite()), context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.REDSTONE_POTENTIOMETER.get(Direction.Axis.Y);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        InfrastructureSavedData.load(level).registerOrUpdateNodes(pos, List.of(0, 1, 2));
        super.tick(state, level, pos, random);
    }

    @Override
    public SimulatedDeviceType<PotentiometerDevice> getDevice() {
        return CEESimulatedDevices.POTENTIOMETER.get();
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (level.getBlockEntity(pos) instanceof PotentiometerBlockEntity be) {
            tag.putDouble("Resistance", Math.max(0.01, be.resistance.value / 1000d));
            tag.putDouble("Progress", be.progress);
        }
        return tag;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof RedstonePotentiometerBlockEntity be)
            be.targetProgress = Mth.clamp(level.getBestNeighborSignal(pos) / 15f, 0, 1);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);

        return state;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id == 1 ? CEELang.nodeLabel("wiper") :
                super.getNodeLabel(level, pos, state, id);
    }

    @Override
    public Class<RedstonePotentiometerBlockEntity> getBlockEntityClass() {
        return RedstonePotentiometerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstonePotentiometerBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.REDSTONE_POTENTIOMETER.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.POTENTIOMETER.getNodes(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.POTENTIOMETER.getNodePos(state.getValue(HORIZONTAL_FACING), id);
    }

    @Override
    public float getNodeSize(Level level, BlockPos pos, BlockState state, int id) {
        return 2/16f;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING)));
    }
}
