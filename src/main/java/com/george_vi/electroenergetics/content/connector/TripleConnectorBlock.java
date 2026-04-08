package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TripleConnectorBlock extends DirectionalRolledDeviceBlock<ConnectorDevice> implements IWrenchable {

    public static final BooleanProperty DIAGONAL = BooleanProperty.create("diagonal");

    public TripleConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(DIAGONAL, false));
    }

    @Override
    public SimulatedDeviceType<ConnectorDevice> getDevice() {
        return CEESimulatedDevices.CONNECTOR.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(DIAGONAL))
            return CEEShapes.DOUBLE_CONNECTOR_DIAGONAL.get(state.getValue(FACING));
        return (state.getValue(ROLL) ? CEEShapes.DOUBLE_CONNECTOR_ROLL : CEEShapes.DOUBLE_CONNECTOR).get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DIAGONAL);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                (state.getValue(DIAGONAL) ? CEENodeConfigurations.TRIPLE_CONNECTOR_DIAGONAL : CEENodeConfigurations.TRIPLE_CONNECTOR)
                        .rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                (state.getValue(DIAGONAL) ? CEENodeConfigurations.TRIPLE_CONNECTOR_DIAGONAL : CEENodeConfigurations.TRIPLE_CONNECTOR)
                        .getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                (state.getValue(DIAGONAL) ? CEENodeConfigurations.TRIPLE_CONNECTOR_DIAGONAL : CEENodeConfigurations.TRIPLE_CONNECTOR)
                        .rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                (state.getValue(DIAGONAL) ? CEENodeConfigurations.TRIPLE_CONNECTOR_DIAGONAL : CEENodeConfigurations.TRIPLE_CONNECTOR)
                        .getNodePos(state.getValue(FACING), id);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis()) {
            boolean diagonal = originalState.getValue(DIAGONAL);
            boolean roll = originalState.getValue(ROLL);
            return diagonal && roll ? originalState.setValue(DIAGONAL, false).setValue(ROLL, false) :
                    !diagonal && !roll ? originalState.setValue(DIAGONAL, true).setValue(ROLL, false) :
                    diagonal ? originalState.setValue(DIAGONAL, false).setValue(ROLL, true) :
                            originalState.setValue(DIAGONAL, true).setValue(ROLL, true);
        }
        return super.getRotatedBlockState(originalState, targetedFace);
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return true;
    }
}
