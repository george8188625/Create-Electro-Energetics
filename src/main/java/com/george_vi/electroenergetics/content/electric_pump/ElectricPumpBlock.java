package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElectricPumpBlock extends PumpBlock implements ElectricalDeviceBlock<ElectricPumpDevice> {
    public static final BooleanProperty ROLL = BooleanProperty.create("roll");

    public ElectricPumpBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROLL);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(FACING).getAxis())
            return originalState.cycle(ROLL);
        return super.getRotatedBlockState(originalState, targetedFace);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Integer> nodes = new ArrayList<>(getNodePositions(level, pos, state).keySet());

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        sd.registerOrUpdateNodes(pos, nodes);
        super.tick(state, level, pos, random);
    }

    @Override
    public boolean isLargeCog() {
        return false;
    }

    @Override
    public boolean isSmallCog() {
        return false;
    }

    @Override
    public BlockEntityType<? extends PumpBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ELECTRIC_PUMP.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(ROLL))
            return CEENodeConfigurations.PUMP.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING));
        return CEENodeConfigurations.PUMP.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(ROLL))
            return CEENodeConfigurations.PUMP.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id);
        return CEENodeConfigurations.PUMP.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public SimulatedDeviceType<ElectricPumpDevice> getDevice() {
        return CEESimulatedDevices.ELECTRIC_PUMP.get();
    }
}
