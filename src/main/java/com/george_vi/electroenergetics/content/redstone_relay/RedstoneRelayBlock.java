package com.george_vi.electroenergetics.content.redstone_relay;

import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RedstoneRelayBlock extends DirectionalRolledDeviceBlock implements IWrenchable {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public RedstoneRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(INVERTED, false));
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Inverted", state.getValue(INVERTED));
        tag.putBoolean("Powered", state.getValue(POWERED));
        return tag;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.hasNeighborSignal(pos) != state.getValue(POWERED))
            level.setBlockAndUpdate(pos, state.cycle(POWERED));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        SimulatedDeviceInstance<?> instance = sd.getDevice(pos);

        if (instance == null || !(instance.extraData() instanceof RedstoneRelayDevice.DataHolder dataHolder))
            return;

        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
        dataHolder.powered = state.getValue(POWERED);
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.REDSTONE_RELAY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED, INVERTED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.REDSTONE_RELAY.get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.REDSTONE_RELAY.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.REDSTONE_RELAY.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.REDSTONE_RELAY.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.REDSTONE_RELAY.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            SimulatedDeviceInstance<?> deviceInstance = InfrastructureSavedData.load(serverLevel).getDevice(context.getClickedPos());
            if (deviceInstance != null && deviceInstance.extraData() instanceof RedstoneRelayDevice.DataHolder dataHolder)
                dataHolder.inverted = !state.getValue(INVERTED);
            serverLevel.setBlockAndUpdate(context.getClickedPos(), state.cycle(INVERTED));
            AllSoundEvents.WRENCH_ROTATE.playOnServer(context.getLevel(), context.getClickedPos());
        }

        return InteractionResult.SUCCESS;
    }
}
