package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BundledWireTerminationBlock extends DirectionalRolledDeviceBlock<BundledWireTerminationDevice> {
    public static final BooleanProperty FLIP = BooleanProperty.create("flip");
    public final BundledWireType wireType;

    public BundledWireTerminationBlock(Properties properties, BundledWireType type) {
        super(properties);
        this.wireType = type;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Vec3 position = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        Direction clickedFace = context.getClickedFace();
        BooleanBooleanPair desirableState = AccumulatorBlock.desirableState(position, clickedFace);

        boolean roll = desirableState.firstBoolean();
        boolean flip = desirableState.secondBoolean();

        roll = !roll;
        flip ^= roll;

        return withWater(defaultBlockState().setValue(ROLL, roll).setValue(FLIP, flip)
                .setValue(FACING, clickedFace), context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIP);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return BundledWireNodeConfigurator.getNodesFor(
                state.getValue(FACING),
                state.getValue(ROLL),
                state.getValue(FLIP));
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return BundledWireNodeConfigurator.getPos(state.getValue(FACING), id);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(ROLL))
            return CEEShapes.DUPLEX_WIRE_TERMINATION_ROLL.get(state.getValue(FACING));
        return CEEShapes.DUPLEX_WIRE_TERMINATION.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public SimulatedDeviceType<BundledWireTerminationDevice> getDevice() {
        return CEESimulatedDevices.BUNDLED_WIRE_TERMINATION.get();
    }

    @Override
    public boolean isNodeAccessible(Level level, BlockPos pos, BlockState state, int id) {
        return BundledWireNodeConfigurator.isAccessible(id);
    }
}

