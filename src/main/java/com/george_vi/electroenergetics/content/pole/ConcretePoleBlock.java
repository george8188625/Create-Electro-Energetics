package com.george_vi.electroenergetics.content.pole;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.placement.PoleHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConcretePoleBlock extends SimpleDeviceBlock implements ProperWaterloggedBlock {
    public static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static BooleanProperty TOP = BooleanProperty.create("top");
    public static BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public ConcretePoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(BOTTOM, true).setValue(TOP, true));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        return withWater(defaultBlockState().setValue(BOTTOM, !CEEBlocks.CONCRETE_POLE.has(below))
                .setValue(TOP, !CEEBlocks.CONCRETE_POLE.has(above)).setValue(AXIS, CEEBlocks.CONCRETE_POLE.has(below) ? below.getValue(AXIS) : CEEBlocks.CONCRETE_POLE.has(above) ? above.getValue(AXIS) : context.getHorizontalDirection().getAxis()), context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, AXIS, TOP, BOTTOM);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.EIGHT_VOXEL_POLE.get(Direction.Axis.Y);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CONCRETE_POLE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack) && !player.isShiftKeyDown())
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (!hasNodes(state))
            return Collections.emptyMap();
        return CEENodeConfigurations.CONCRETE_POLE.getNodes(state.getValue(BOTTOM) ? Direction.UP : Direction.DOWN);
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (!hasNodes(state))
            return null;
        return CEENodeConfigurations.CONCRETE_POLE.getNodePos(state.getValue(BOTTOM) ? Direction.UP : Direction.DOWN, id);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        if (CEEBlocks.CONCRETE_POLE.has(below) == state.getValue(BOTTOM) ||
                CEEBlocks.CONCRETE_POLE.has(above) == state.getValue(TOP)) {
            LevelTickAccess<Block> blockTicks = level.getBlockTicks();
            if (!blockTicks.hasScheduledTick(pos, this))
                level.scheduleTick(pos, this, 1);
        }

        updateWater(level, state, pos);
        return state.setValue(BOTTOM, !CEEBlocks.CONCRETE_POLE.has(below))
                .setValue(TOP, !CEEBlocks.CONCRETE_POLE.has(above));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    private boolean hasNodes(BlockState state) {
        return state.getValue(TOP) ^ state.getValue(BOTTOM);
    }


    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? state.cycle(AXIS) : state;
    }

    @MethodsReturnNonnullByDefault
    public static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CEEBlocks.CONCRETE_POLE::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return CEEBlocks.CONCRETE_POLE::has;
        }

        public int attachedPoles(Level world, BlockPos pos, Direction direction) {
            BlockPos checkPos = pos.relative(direction);
            BlockState state = world.getBlockState(checkPos);
            int count = 0;
            while (getStatePredicate().test(state)) {
                count++;
                checkPos = checkPos.relative(direction);
                state = world.getBlockState(checkPos);
            }
            return count;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), Direction.Axis.Y);
            for (Direction dir : directions) {
                int range = AllConfigs.server().equipment.placementAssistRange.get();
                if (player != null) {
                    AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
                    if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier.id()))
                        range += 4;
                }
                int poles = attachedPoles(world, pos, dir);
                if (poles >= range)
                    continue;

                BlockPos newPos = pos.relative(dir, poles + 1);
                BlockState newState = world.getBlockState(newPos);

                if (newState.canBeReplaced())
                    return PlacementOffset.success(newPos, bs -> bs.setValue(AXIS, state.getValue(AXIS)));

            }

            return PlacementOffset.fail();
        }
    }
}
