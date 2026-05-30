package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

public class EmergencyStopBlock extends DirectionalRolledDeviceBlock<CutOffSwitchDevice> {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public EmergencyStopBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVATED, false));
    }

    @Override
    public SimulatedDeviceType<CutOffSwitchDevice> getDevice() {
        return CEESimulatedDevices.CUT_OFF_SWITCH.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED);
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Closed", !state.getValue(ACTIVATED));
        return tag;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.BUZZER.get(state.getValue(FACING));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean activate = !player.isShiftKeyDown();

        if (level instanceof ServerLevel serverLevel) {
            CutOffSwitchDevice device = DevicesSavedData.load(serverLevel).getDevice(pos, CutOffSwitchDevice.class);
            if (device != null) {
                if (device.isClosed == activate) {
                    CEESoundEvents.playOnServer(level, pos, activate ? CEESoundEvents.CONTACT_OPEN.get() : CEESoundEvents.CONTACT_CLOSE.get(), 1f, 1f);
                }
                device.isClosed = !activate;

            }

            if (activate) {
                CEEAdvancements.ESTOP.awardTo(player);
            }
        }

        if (state.getValue(ACTIVATED) != activate) {
            level.setBlockAndUpdate(pos, state.setValue(ACTIVATED, activate));
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.ELECTRONIC_4.rotate(new Vec3(0, -90, 0)).getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.ELECTRONIC_4.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.ELECTRONIC_4.rotate(new Vec3(0, -90, 0)).getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.ELECTRONIC_4.getNodePos(state.getValue(FACING), id);
    }
}
