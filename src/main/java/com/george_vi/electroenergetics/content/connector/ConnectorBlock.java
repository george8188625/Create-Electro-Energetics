package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchBlock;
import com.george_vi.electroenergetics.foundation.base.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.data.Iterate;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
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
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        if (facing == Direction.UP)
            for (Direction direction : Iterate.horizontalDirections) {
                BlockPos targetPos = pos.relative(direction, 2);
                BlockState targetState = context.getLevel().getBlockState(targetPos);
                if (CEEBlocks.HV_SWITCH.has(targetState))
                    if (targetState.getValue(HVSwitchBlock.FACING) == direction.getOpposite())
                        return super.getStateForPlacement(context).setValue(FACING, facing).setValue(STYLE, Style.LONG);
            }
        return super.getStateForPlacement(context).setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(STYLE) == Style.LONG || state.getValue(STYLE) == Style.LONG_HANGER)
            return AllShapes.FOUR_VOXEL_POLE.get(state.getValue(FACING).getAxis());
        if (state.getValue(STYLE) == Style.SHORT || state.getValue(STYLE) == Style.HANGER)
            return CEEShapes.CONNECTOR.get(state.getValue(FACING));
        return CEEShapes.CONNECTOR_SHORT.get(state.getValue(FACING));
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
        if (state.getValue(STYLE) == Style.LONG || state.getValue(STYLE) == Style.LONG_HANGER)
            return CEENodeConfigurations.SINGLE_MIDDLE_TOP.getNodes(state.getValue(FACING));
        if (state.getValue(STYLE) == Style.SHORT || state.getValue(STYLE) == Style.HANGER)
            return Map.of(0, new Vec3(0.5, 0.5, 0.5));
        return CEENodeConfigurations.SHORT_CONNECTOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(STYLE) == Style.LONG || state.getValue(STYLE) == Style.LONG_HANGER)
            return CEENodeConfigurations.SINGLE_MIDDLE_TOP.getNodePos(state.getValue(FACING), id);
        if (state.getValue(STYLE) == Style.SHORT || state.getValue(STYLE) == Style.HANGER)
            return new Vec3(0.5, 0.5, 0.5);
        return CEENodeConfigurations.SHORT_CONNECTOR.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        SimulatedDeviceInstance<?> instance = sd.getDevice(pos);
        if (instance != null && instance.extraData() instanceof ConnectorDevice.DataHolder dataHolder) {
            if (state.getValue(STYLE) == Style.LONG && state.getValue(FACING) == Direction.UP)
                dataHolder.isHVSwitchTarget = true;
            else
                dataHolder.isHVSwitchTarget = false;
        }
    }

    @Override
    public boolean isOuterInsulator(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(STYLE) == Style.OUTER;
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
        SHORT(""),
        SHORTER("short"),
        LONG("long"),
        UNINSULATED("outer"),
        OUTER("outer"),
        SHORT_HANGER("short_hanger"),
        HANGER("hanger"),
        LONG_HANGER("long_hanger");

        public final String suffix;

        Style(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
}
