package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.SimpleDeviceBlock;
import com.george_vi.electroenergetics.content.wire_spool.WireRenderer;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.*;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CutOffSwitchBlock extends SimpleDeviceBlock implements IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROLL = BooleanProperty.create("roll");
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final boolean isDouble;

    public CutOffSwitchBlock(Properties properties, boolean isDouble) {
        super(properties);
        this.isDouble = isDouble;
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected SimulatedDevice getDevice() {
        if (isDouble)
            return CEESimulatedDevices.DOUBLE_SWITCH;
        return CEESimulatedDevices.CUT_OFF_SWITCH;
    }

    @Override
    protected CompoundTag getExtraData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Closed", state.getValue(CLOSED));
        return tag;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROLL, CLOSED, WATERLOGGED);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || CEEItems.WIRE_SPOOL.isIn(stack) || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
            if (device != null)
                device.extraData().putBoolean("Closed", !state.getValue(CLOSED));
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
        } else {
            Vec3 pPos = Vec3.atCenterOf(pos);
            pPos = pPos.subtract(Vec3.atLowerCornerOf(state.getValue(FACING).getNormal()).multiply(0.25, 0.25, 0.25));
            if (state.getValue(CLOSED)) {
                for (int l : isDouble ? Iterate.zeroAndOne : new int[]{0}) {
                    Float v1 = WireRenderer.getAllVoltages().get(new Node(l, pos));
                    Float v2 = WireRenderer.getAllVoltages().get(new Node((isDouble ? 2 : 1) + l, pos));
                    if (v1 != null && v2 != null && Math.abs(v1 - v2) > 0.0003)
                        for (int i = 0; i < (Math.abs(v1 - v2) * 10) + 1; i++)
                            level.addParticle(ParticleTypes.BUBBLE_POP, pPos.offsetRandom(level.random, 0.3f).x, pPos.offsetRandom(level.random, 0.3f).y, pPos.offsetRandom(level.random, 0.3f).z, 0, 0, 0);
                }
            }
        }
        level.setBlockAndUpdate(pos, state.cycle(CLOSED));
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis())
            return originalState.cycle(ROLL);
        return IWrenchable.super.getRotatedBlockState(originalState, targetedFace);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isVertical())
            return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace()).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X);
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (isDouble)
            return CEEShapes.DOUBLE_SWITCH.get(state.getValue(FACING));
        return (state.getValue(ROLL) ? CEEShapes.CUT_OFF_SWITCH_ROLL : CEEShapes.CUT_OFF_SWITCH).get(state.getValue(FACING));
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
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (isDouble)
            return state.getValue(ROLL) ?
                    CEENodeConfigurations.DOUBLE_SWITCH_ROLL.getNodes(state.getValue(FACING)) :
                    CEENodeConfigurations.DOUBLE_SWITCH.getNodes(state.getValue(FACING));
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_CONNECTOR_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.DOUBLE_CONNECTOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (isDouble)
            return state.getValue(ROLL) ?
                    CEENodeConfigurations.DOUBLE_SWITCH_ROLL.getNodePos(state.getValue(FACING), id) :
                    CEENodeConfigurations.DOUBLE_SWITCH.getNodePos(state.getValue(FACING), id);
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_CONNECTOR_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.DOUBLE_CONNECTOR.getNodePos(state.getValue(FACING), id);
    }
}
