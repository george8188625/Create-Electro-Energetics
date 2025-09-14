package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BulbBlock extends DirectionalRolledDeviceBlock {
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 2);
    public final boolean broken;

    public BulbBlock(Properties properties) {
        this(properties, false);
    }

    public BulbBlock(Properties properties, boolean broken) {
        super(properties);
        this.broken = broken;
    }

    public static BulbBlock broken(Properties properties) {
        return new BulbBlock(properties, true);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.BULB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? CEEShapes.BULB_ROLL : CEEShapes.BULB).get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = InfrastructureSavedData.load(sl).getDevice(pos);
            if (deviceInstance != null)
                deviceInstance.extraData().putBoolean("Destroyed", broken);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (broken)
            return 0;
        return state.getValue(LIGHT) * 7;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.BULB_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.BULB.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.BULB_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.BULB.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected boolean shouldReplaceDeviceFor(BlockState thisState, BlockState newState) {
        return thisState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
