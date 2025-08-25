package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConnectorBlock extends SimpleDeviceBlock implements IWrenchable, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);

    public ConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(STYLE, Style.SHORT));
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CONNECTOR;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, STYLE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(STYLE) == Style.LONG)
            return AllShapes.FOUR_VOXEL_POLE.get(state.getValue(FACING).getAxis());
        if (state.getValue(STYLE) == Style.OUTER)
            return CEEShapes.CONNECTOR_SHORT.get(state.getValue(FACING));
        return CEEShapes.CONNECTOR.get(state.getValue(FACING));
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
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.cycle(STYLE);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(STYLE) == Style.LONG ? CEENodeConfigurations.SINGLE_MIDDLE_TOP.getNodes(state.getValue(FACING)) : state.getValue(STYLE) == Style.OUTER ? CEENodeConfigurations.SHORT_CONNECTOR.getNodes(state.getValue(FACING)) : Map.of(0, new Vec3(0.5,0.5,0.5));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(STYLE) == Style.LONG ? CEENodeConfigurations.SINGLE_MIDDLE_TOP.getNodePos(state.getValue(FACING), id) : state.getValue(STYLE) == Style.OUTER ? CEENodeConfigurations.SHORT_CONNECTOR.getNodePos(state.getValue(FACING), id) : new Vec3(0.5, 0.5, 0.5);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        InfrastructureSavedData.SimulatedDeviceInstance instance = sd.getDevice(pos);
        if (instance != null) {
            if (state.getValue(STYLE) == Style.LONG)
                instance.extraData().putBoolean("HVSwitchTarget", true);
            else
                instance.extraData().remove("HVSwitchTarget");
        }
    }

    @Override
    public boolean isOuterInsulator(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(STYLE) == Style.OUTER;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public enum Style implements StringRepresentable {
        SHORT,
        LONG,
        OUTER;

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
}
