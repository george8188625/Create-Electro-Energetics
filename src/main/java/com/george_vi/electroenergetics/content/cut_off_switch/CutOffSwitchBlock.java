package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryDevice;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CutOffSwitchBlock extends DirectionalRolledDeviceBlock {
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
    public final boolean isDouble;

    public CutOffSwitchBlock(Properties properties, boolean isDouble) {
        super(properties);
        this.isDouble = isDouble;
    }

    @Override
    protected SimulatedDevice getDevice() {
        if (isDouble)
            return CEESimulatedDevices.DOUBLE_SWITCH;
        return CEESimulatedDevices.CUT_OFF_SWITCH;
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel serverLevel) {
            SimulatedDeviceInstance<?> device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
            if (device != null && device.extraData() instanceof CutOffSwitchDevice.DataHolder dataHolder)
                dataHolder.isClosed = !state.getValue(CLOSED);
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
        } else {
            Vec3 pPos = Vec3.atCenterOf(pos);
            pPos = pPos.subtract(Vec3.atLowerCornerOf(state.getValue(FACING).getNormal()).multiply(0.25, 0.25, 0.25));
            if (state.getValue(CLOSED)) {
                for (int l : isDouble ? Iterate.zeroAndOne : new int[]{0}) {
                    Double v1 = WireRenderer.getAllVoltages().get(new InWorldNode(l, pos));
                    Double v2 = WireRenderer.getAllVoltages().get(new InWorldNode((isDouble ? 2 : 1) + l, pos));
                    if (v1 != null && v2 != null && Math.abs(v1 - v2) > 0.0003)
                        for (int i = 0; i < Math.min(30, Math.abs(v1 - v2) * 10) + 1; i++)
                            level.addParticle(ParticleTypes.BUBBLE_POP, pPos.offsetRandom(level.random, 0.3f).x, pPos.offsetRandom(level.random, 0.3f).y, pPos.offsetRandom(level.random, 0.3f).z, 0, 0, 0);
                }
            }
        }
        level.setBlockAndUpdate(pos, state.cycle(CLOSED));
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (isDouble)
            return CEEShapes.DOUBLE_SWITCH.get(state.getValue(FACING));
        return (state.getValue(ROLL) ? CEEShapes.CUT_OFF_SWITCH_ROLL : CEEShapes.CUT_OFF_SWITCH).get(state.getValue(FACING));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (isDouble)
            return state.getValue(ROLL) ?
                    CEENodeConfigurations.DOUBLE_SWITCH.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                    CEENodeConfigurations.DOUBLE_SWITCH.getNodes(state.getValue(FACING));
        return state.getValue(ROLL) ?
                CEENodeConfigurations.BULB.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.BULB.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (isDouble)
            return state.getValue(ROLL) ?
                    CEENodeConfigurations.DOUBLE_SWITCH.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                    CEENodeConfigurations.DOUBLE_SWITCH.getNodePos(state.getValue(FACING), id);
        return state.getValue(ROLL) ?
                CEENodeConfigurations.BULB.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.BULB.getNodePos(state.getValue(FACING), id);
    }


    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
