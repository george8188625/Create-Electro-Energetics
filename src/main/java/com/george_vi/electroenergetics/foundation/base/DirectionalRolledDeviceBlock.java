package com.george_vi.electroenergetics.foundation.base;

import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class DirectionalRolledDeviceBlock<T extends SimulatedDevice> extends SimpleElectricalDeviceBlock<T> implements ProperOilAndWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROLL = BooleanProperty.create("roll");
    public static final EnumProperty<ProperOilAndWaterloggedBlock.LoggedState> LOGGED_STATE = ProperOilAndWaterloggedBlock.LOGGED_STATE;

    public DirectionalRolledDeviceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LOGGED_STATE, LoggedState.DRY).setValue(ROLL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROLL, LOGGED_STATE);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis())
            return originalState.cycle(ROLL);
        return super.getRotatedBlockState(originalState, targetedFace);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isVertical())
            return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(ROLL, context.getHorizontalDirection().getAxis() == Direction.Axis.X), context);
        return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()), context);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.Y)
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
                return state.cycle(ROLL);
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    public static <T extends Block> void generateBlockState(DataGenContext<Block, T> c, RegistrateBlockstateProvider p, Function<BlockState, ResourceLocation> modelFunc) {
        p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                ConfiguredModel.builder()
                        .modelFile(!state.getValue(ROLL) ? p.models().getExistingFile(modelFunc.apply(state)) : p.models().getExistingFile(modelFunc.apply(state).withSuffix("_roll")))
                        .rotationX(state.getValue(FACING) == Direction.DOWN ? 180 : state.getValue(FACING).getAxis().isHorizontal() ? 270 : 0)
                        .rotationY(state.getValue(FACING).getAxis().isHorizontal() ? (int) state.getValue(FACING).toYRot() : 0)
                        .build()));
    }

    public static <T extends Block> void generateBlockState(DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                ConfiguredModel.builder()
                        .modelFile(!state.getValue(ROLL) ? AssetLookup.partialBaseModel(c, p) : AssetLookup.partialBaseModel(c, p, "roll"))
                        .rotationX(state.getValue(FACING) == Direction.DOWN ? 180 : state.getValue(FACING).getAxis().isHorizontal() ? 270 : 0)
                        .rotationY(state.getValue(FACING).getAxis().isHorizontal() ? (int) state.getValue(FACING).toYRot() : 0)
                        .build()));
    }
}
