package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class MomentarySwitchBlock extends DirectionalRolledDeviceBlock {
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    public MomentarySwitchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CLOSED, false));
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.MOMENTARY_SWITCH;
    }

    @Override
    protected CompoundTag getExtraDeviceData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Closed", state.getValue(CLOSED));
        return tag;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CLOSED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? CEEShapes.MOMENTARY_SWITCH_ROLL : CEEShapes.MOMENTARY_SWITCH).get(state.getValue(FACING));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
            if (device == null || device.extraData().getInt("ClosedTicks") == -1)
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            if (device != null)
                device.extraData().putInt("ClosedTicks", 4);
        }

        level.setBlockAndUpdate(pos, state.setValue(CLOSED, true));
        return ItemInteractionResult.SUCCESS;
    }

    protected void openSwitch(BlockState state, ServerLevel level, BlockPos pos) {
        if (!state.getValue(CLOSED))
            return;

        level.setBlockAndUpdate(pos, state.setValue(CLOSED, false));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.MOMENTARY_SWITCH.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.MOMENTARY_SWITCH.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.MOMENTARY_SWITCH.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.MOMENTARY_SWITCH.getNodePos(state.getValue(FACING), id);
    }
}
