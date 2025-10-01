package com.george_vi.electroenergetics.content.relay;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class RelayBlock extends DirectionalRolledDeviceBlock {
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public RelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(INVERTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVERTED);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.RELAY;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.REDSTONE_RELAY.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEENodeConfigurations.RELAY_ROLL.getNodes(state.getValue(FACING));
        return CEENodeConfigurations.RELAY.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return CEENodeConfigurations.RELAY_ROLL.getNodePos(state.getValue(FACING), id);
        return CEENodeConfigurations.RELAY.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (id == 0 || id == 1)
            return CEELang.nodeLabel("coil");
        return super.getNodeLabel(level, pos, state, id);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(context.getClickedPos());
            if (device != null)
                device.extraData().putBoolean("Inverted", !state.getValue(INVERTED));
            serverLevel.setBlockAndUpdate(context.getClickedPos(), state.cycle(INVERTED));
            AllSoundEvents.WRENCH_ROTATE.playOnServer(context.getLevel(), context.getClickedPos());
        }

        return InteractionResult.SUCCESS;
    }
}
