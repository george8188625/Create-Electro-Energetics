package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock;
import com.george_vi.electroenergetics.foundation.base.DirectionalKineticElectricBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class ElectricMotorBlock extends DirectionalKineticElectricBlock<ElectricMotorDevice> implements IBE<ElectricMotorBlockEntity>, ProperOilAndWaterloggedBlock {
    public static final BooleanProperty ROLL = DirectionalRolledDeviceBlock.ROLL;
    public static final EnumProperty<ProperOilAndWaterloggedBlock.LoggedState> LOGGED_STATE = ProperOilAndWaterloggedBlock.LOGGED_STATE;

    public ElectricMotorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(LOGGED_STATE, ProperOilAndWaterloggedBlock.LoggedState.DRY)
                .setValue(ROLL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOGGED_STATE, ROLL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if (preferred == null || context.getPlayer().isShiftKeyDown())
            preferred = context.getNearestLookingDirection();
        if (!context.getPlayer().isShiftKeyDown())
            preferred = preferred.getOpposite();

        if (preferred.getAxis().isVertical())
            return withWater(defaultBlockState().setValue(FACING, preferred).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X), context);

        return withWater(defaultBlockState().setValue(FACING, preferred), context);
    }

    @Override
    public SimulatedDeviceType<ElectricMotorDevice> getDevice() {
        return CEESimulatedDevices.ELECTRIC_MOTOR.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ELECTRIC_MOTOR.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (stack.getItem() instanceof DyeItem item) {
            DyeColor color = item.getDyeColor();

            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);

            level.setBlockAndUpdate(pos, CEEBlocks.ELECTRIC_MOTORS[color.ordinal()].get().withPropertiesOf(state));

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
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
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.ELECTRIC_MOTOR.getNodes(state.getValue(FACING), state.getValue(ROLL));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.ELECTRIC_MOTOR.getNodePos(state.getValue(FACING), state.getValue(ROLL), id);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis())
            return originalState.cycle(ROLL);
        return super.getRotatedBlockState(originalState, targetedFace);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.Y)
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
                return state.cycle(ROLL);
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
