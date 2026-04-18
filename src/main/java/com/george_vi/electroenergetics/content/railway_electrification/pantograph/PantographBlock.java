package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PantographBlock extends SimpleElectricalDeviceBlock<PantographDevice> implements IBE<PantographBlockEntity>, IPantographBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

    public PantographBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(DOUBLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DOUBLE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.PANTOGRAPH.get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState facingState = level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
        if (CEEBlocks.PANTOGRAPH.has(facingState) && facingState.getValue(FACING).equals(state.getValue(FACING).getOpposite())) {
            if (!state.getValue(DOUBLE)) {
                level.setBlock(pos.relative(state.getValue(FACING).getOpposite()), facingState.setValue(DOUBLE, true), 2);
                return state.setValue(DOUBLE, true);
            }
        } else if (state.getValue(DOUBLE))
            return state.setValue(DOUBLE, false);
        return state;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        BlockState facingState = level.getBlockState(pos.relative(state.getValue(FACING)));
        if (CEEBlocks.PANTOGRAPH.has(facingState) && facingState.getValue(FACING).equals(state.getValue(FACING).getOpposite())) {
            if (!state.getValue(DOUBLE)) {
                level.setBlockAndUpdate(pos.relative(state.getValue(FACING)), facingState.setValue(DOUBLE, true));
                level.setBlockAndUpdate(pos, state.setValue(DOUBLE, true));
            }
        } else
            if (state.getValue(DOUBLE))
                level.setBlockAndUpdate(pos, state.setValue(DOUBLE, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            boolean extended = false;
            if (level.getBlockEntity(pos) instanceof PantographBlockEntity be) {
                extended = be.extended ^= true;
                if (extended)
                    be.targetExtensionState = state.getValue(DOUBLE) ? 1.7f : 0.85f;
                else
                    be.targetExtensionState = 0f;
            }

            if (state.getValue(DOUBLE)) {
                if (level.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof PantographBlockEntity be) {
                    be.extended = extended;
                    if (extended)
                        be.targetExtensionState = 1.7f;
                    else
                        be.targetExtensionState = 0f;
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (!(stack.getItem() instanceof DyeItem di))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.getBlockEntity(pos) instanceof PantographBlockEntity be) {
            be.color = di.getDyeColor();

            if (!level.isClientSide)
                be.sendData();
        }

        if (state.getValue(DOUBLE)) {
            if (level.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof PantographBlockEntity be) {
                be.color = di.getDyeColor();

                if (!level.isClientSide)
                    be.sendData();
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public Class<PantographBlockEntity> getBlockEntityClass() {
        return PantographBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PantographBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.PANTOGRAPH.get();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public PantographType getPantographType(BlockState state) {
        return state.getValue(DOUBLE) ? CEEPantographTypes.DOUBLE.get() : CEEPantographTypes.STANDARD.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return Map.of(0, new Vec3(0.5f, 0.375f, 0.5f));
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return new Vec3(0.5f, 0.375f, 0.5f);
    }

    @Override
    public SimulatedDeviceType<PantographDevice> getDevice() {
        return CEESimulatedDevices.PANTOGRAPH.get();
    }
}
