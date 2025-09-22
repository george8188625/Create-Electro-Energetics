package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TripleConnectorBlock extends DirectionalRolledDeviceBlock implements IWrenchable {

    public TripleConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CONNECTOR;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? CEEShapes.DOUBLE_CONNECTOR_ROLL : CEEShapes.DOUBLE_CONNECTOR).get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.TRIPLE_CONNECTOR_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.TRIPLE_CONNECTOR.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.TRIPLE_CONNECTOR_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.TRIPLE_CONNECTOR.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return true;
    }
}
