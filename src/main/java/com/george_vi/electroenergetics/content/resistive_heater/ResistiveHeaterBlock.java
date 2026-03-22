package com.george_vi.electroenergetics.content.resistive_heater;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ResistiveHeaterBlock extends SimpleDeviceBlock implements ProperWaterloggedBlock, IBE<ResistiveHeaterBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ResistiveHeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(LIT, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(defaultBlockState().setValue(FACING, context.getPlayer().isShiftKeyDown() ?
                context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection()), context);
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().campfire(), 1);
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.RESISTIVE_HEATER.get(Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, LIT, HEAT_LEVEL);
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.RESISTIVE_HEATER;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.RESISTIVE_HEATER.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.RESISTIVE_HEATER.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public Class<ResistiveHeaterBlockEntity> getBlockEntityClass() {
        return ResistiveHeaterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ResistiveHeaterBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.RESISTIVE_HEATER.get();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
