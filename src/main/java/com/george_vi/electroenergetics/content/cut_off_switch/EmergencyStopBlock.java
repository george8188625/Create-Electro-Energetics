package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

public class EmergencyStopBlock extends DirectionalRolledDeviceBlock {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public EmergencyStopBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVATED, false));
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.CUT_OFF_SWITCH;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(ROLL) ? CEEShapes.BUZZER_ROLL : CEEShapes.BUZZER).get(state.getValue(FACING));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean activate = !player.isShiftKeyDown();

        if (level instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
            if (device != null)
                device.extraData().putBoolean("Closed", !activate);
        }

        if (state.getValue(ACTIVATED) != activate) {
            level.setBlockAndUpdate(pos, state.setValue(ACTIVATED, activate));
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
            if (level.isClientSide) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                pPos = pPos.subtract(Vec3.atLowerCornerOf(state.getValue(FACING).getNormal()).multiply(0.25, 0.25, 0.25));
                if (activate) {
                    Double v1 = WireRenderer.getAllVoltages().get(new InWorldNode(0, pos));
                    Double v2 = WireRenderer.getAllVoltages().get(new InWorldNode(1, pos));
                    if (v1 != null && v2 != null && Math.abs(v1 - v2) > 0.0003)
                        for (int i = 0; i < Math.min(30, Math.abs(v1 - v2) * 10) + 1; i++)
                            level.addParticle(ParticleTypes.BUBBLE_POP, pPos.offsetRandom(level.random, 0.3f).x, pPos.offsetRandom(level.random, 0.3f).y, pPos.offsetRandom(level.random, 0.3f).z, 0, 0, 0);
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.ELECTRONIC_4_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.ELECTRONIC_4.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.ELECTRONIC_4_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.ELECTRONIC_4.getNodePos(state.getValue(FACING), id);
    }
}
