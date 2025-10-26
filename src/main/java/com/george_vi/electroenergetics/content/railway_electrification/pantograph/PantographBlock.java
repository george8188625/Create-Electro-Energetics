package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEShapes;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PantographBlock extends Block implements IBE<PantographBlockEntity> {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean extended = false;
        if (level.getBlockEntity(pos) instanceof PantographBlockEntity be) {
            extended = be.targetExtensionState <= 0.325f;
            if (extended)
                be.targetExtensionState = 0.75f;
            else
                be.targetExtensionState = state.getValue(DOUBLE) ? 0.3f : 0f;
        }

        if (state.getValue(DOUBLE)) {
            if (level.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof PantographBlockEntity be) {
                if (extended)
                    be.targetExtensionState = 0.75f;
                else
                    be.targetExtensionState = 0.3f;
            }
        }
        return InteractionResult.SUCCESS;
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
}
