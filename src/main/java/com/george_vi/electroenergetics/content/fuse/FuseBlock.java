package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FuseBlock extends SimpleDeviceBlock implements IWrenchable {
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
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.FUSE;
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
        if (!stack.is(AllTags.commonItemTag("wires/copper")) || !broken)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
            if (device != null)
                device.extraData().putBoolean("Broken", false);
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
        }

        level.setBlockAndUpdate(pos, CEEBlocks.FUSE.get().withPropertiesOf(state));
        stack.shrink(1);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.BI_POLAR_DIRECTIONAL.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected boolean shouldReplaceDeviceFor(BlockState thisState, BlockState newState) {
        return thisState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
