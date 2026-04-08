package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FuseBlock extends SimpleElectricalDeviceBlock<FuseDevice> implements IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public final boolean broken;


    public FuseBlock(Properties properties, boolean broken) {
        super(properties);
        this.broken = broken;
    }

    public FuseBlock(Properties properties) {
        this(properties, false);
    }

    public static FuseBlock broken(Properties properties) {
        return new FuseBlock(properties, true);
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (broken)
            tag.putBoolean("Broken", true);
        return tag;
    }

    @Override
    public SimulatedDeviceType<FuseDevice> getDevice() {
        return CEESimulatedDevices.FUSE.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(CEETags.COPPER_WIRE) || !broken)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel serverLevel) {
            FuseDevice device = DevicesSavedData.load(serverLevel).getDevice(pos, FuseDevice.class);
            if (device != null)
                device.isBroken = false;
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
        }

        level.setBlockAndUpdate(pos, CEEBlocks.FUSE.get().withPropertiesOf(state));
        if (!player.isCreative())
            stack.shrink(1);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }


}
