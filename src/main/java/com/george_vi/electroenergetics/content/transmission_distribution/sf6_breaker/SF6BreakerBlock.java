package com.george_vi.electroenergetics.content.transmission_distribution.sf6_breaker;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SF6BreakerBlock extends SimpleElectricalDeviceBlock<SF6BreakerDevice> implements ProperWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty BASE = BooleanProperty.create("base");

    public SF6BreakerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context))
            return withWater(defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()), context);
        else
            return null;
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Base", state.getValue(BASE));
        return tag;
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        return state.getValue(BASE) || blockstate.is(this);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        boolean isBase = state.getValue(BASE);
        if (!level.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(state, level, pos)) && !isBase) {
            BlockPos otherPos = pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(state.getBlock()) && otherState.getValue(BASE)) {
                BlockState newState = otherState.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(otherPos, newState, 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }

        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level instanceof ServerLevel sl) {
            SF6BreakerDevice device = DevicesSavedData.load(sl).getDevice(pos, SF6BreakerDevice.class);

            if (device != null) {
                for (Direction direction : Iterate.directions) {
                    BlockPos otherPos = pos.relative(direction);
                    int power = level.getSignal(otherPos, direction.getOpposite());

                    device.updateRedstoneInput(power, direction);
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        boolean isBase = state.getValue(BASE);
        if (facing == (isBase ? Direction.UP : Direction.DOWN) && (!CEEBlocks.SF6_BREAKER.has(facingState) || facingState.getValue(BASE) == isBase))
            return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, AXIS, BASE);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? state.cycle(AXIS) : state;
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlockAndUpdate(pos.above(), state.setValue(BASE, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.EIGHT_VOXEL_POLE.get(Direction.Axis.Y);
    }

    @Override
    public SimulatedDeviceType<SF6BreakerDevice> getDevice() {
        return CEESimulatedDevices.SF6_BREAKER.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, new Vec3(8/16f, (state.getValue(BASE) ? 2/16f : 14/16f), 8/16f));
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return new Vec3(8/16f, (state.getValue(BASE) ? 2/16f : 14/16f), 8/16f);
    }

}
