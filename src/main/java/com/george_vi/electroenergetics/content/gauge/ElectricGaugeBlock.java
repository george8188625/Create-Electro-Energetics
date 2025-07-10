package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.content.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.levelWrappers.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ElectricGaugeBlock extends SimpleDeviceBlock implements IBE<ElectricGaugeBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final ElectricGaugeShaper SHAPE = ElectricGaugeShaper.make();
    public final boolean voltmeter;

    ElectricGaugeBlock(Properties properties, boolean voltmeter) {
        super(properties);
        this.voltmeter = voltmeter;
    }

    public static ElectricGaugeBlock voltmeter(Properties properties) {
        return new ElectricGaugeBlock(properties, true);
    }

    public static ElectricGaugeBlock ammeter(Properties properties) {
        return new ElectricGaugeBlock(properties, false);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return voltmeter ? CEESimulatedDevices.VOLTMETER : CEESimulatedDevices.AMMETER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING), state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE));
    }
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = getFacingForPlacement(context);
        boolean alongFirst = false;
        Direction.Axis faceAxis = facing.getAxis();

        if (faceAxis.isVertical())
            alongFirst = getAxisAlignmentForPlacement(context);

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE, alongFirst);
    }

    protected boolean getAxisAlignmentForPlacement(BlockPlaceContext context) {
        return context.getHorizontalDirection()
                .getAxis() == Direction.Axis.X;
    }

    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection()
                .getOpposite();
        if (context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown())
            facing = facing.getOpposite();
        return facing;
    }

    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodes(Direction.fromAxisAndDirection(getAxis(state), Direction.AxisDirection.POSITIVE));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodePos(Direction.fromAxisAndDirection(getAxis(state), Direction.AxisDirection.POSITIVE), id);
    }

    public Direction.Axis getAxis(BlockState state) {
        Direction.Axis pistonAxis = state.getValue(FACING)
                .getAxis();
        boolean alongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

        if (pistonAxis == Direction.Axis.X)
            return alongFirst ? Direction.Axis.Y : Direction.Axis.Z;
        if (pistonAxis == Direction.Axis.Y)
            return alongFirst ? Direction.Axis.X : Direction.Axis.Z;
        return alongFirst ? Direction.Axis.X : Direction.Axis.Y;
    }

    public boolean shouldRenderHeadOnFace(Level level, BlockPos pos, BlockState state, Direction face) {
        if (face.getAxis()
                .isVertical())
            return false;
        if (face == state.getValue(FACING)
                .getOpposite())
            return false;
        if (face.getAxis() == getAxis(state))
            return false;
        if (getAxis(state) == Direction.Axis.Y && face != state.getValue(FACING))
            return false;
        return Block.shouldRenderFace(state, level, pos, face, pos.relative(face)) || level instanceof WrappedLevel;
    }

    @Override
    public Class<ElectricGaugeBlockEntity> getBlockEntityClass() {
        return ElectricGaugeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectricGaugeBlockEntity> getBlockEntityType() {
        return voltmeter ? CEEBlockEntityTypes.VOLTMETER.get() : CEEBlockEntityTypes.AMMETER.get();
    }
}
