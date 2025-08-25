package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class HVSwitchBlock extends SimpleDeviceBlock implements IBE<HVSwitchBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public HVSwitchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.HV_SWITCH;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection());
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return CEENodeConfigurations.HV_SWITCH.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return CEENodeConfigurations.HV_SWITCH.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.SIX_VOXEL_POLE.get(Direction.Axis.Y);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || CEEItems.WIRE_SPOOL.isIn(stack) || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel serverLevel) {
            InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
            if (device != null) {
                if (level.getBlockEntity(pos) instanceof HVSwitchBlockEntity be) {
                    device.extraData().putBoolean("Connected", !be.connected);
                    be.connected = !be.connected;
                    be.sendData();
                }
            }
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        boolean powered = level.hasNeighborSignal(pos);

        if (state.getValue(POWERED) != powered) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, powered));

            if (!powered)
                return;
            if (level instanceof ServerLevel serverLevel) {
                InfrastructureSavedData.SimulatedDeviceInstance device = InfrastructureSavedData.load(serverLevel).getDevice(pos);
                if (device != null) {
                    if (level.getBlockEntity(pos) instanceof HVSwitchBlockEntity be) {
                        device.extraData().putBoolean("Connected", !be.connected);
                        be.connected = !be.connected;
                        be.sendData();
                    }
                }
                AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
            }
        }

    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        BlockPos targetPos = pos.relative(state.getValue(HVSwitchBlock.FACING), 2);
        InfrastructureSavedData.SimulatedDeviceInstance instance = sd.getDevice(pos);

        if (instance != null)
            instance.extraData().put("Target", NbtUtils.writeBlockPos(targetPos));
    }

    @Override
    public Class<HVSwitchBlockEntity> getBlockEntityClass() {
        return HVSwitchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HVSwitchBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.HV_SWITCH.get();
    }
}
