package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class BulbBlock extends DirectionalRolledDeviceBlock<BulbDevice> implements IBE<BulbBlockEntity> {
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    public static final BooleanProperty COMPACT = BooleanProperty.create("compact");
    public final boolean broken;

    public BulbBlock(Properties properties) {
        this(properties, false);
    }

    public BulbBlock(Properties properties, boolean broken) {
        super(properties);
        this.broken = broken;
        registerDefaultState(defaultBlockState().setValue(COMPACT, false).setValue(LIGHT, 0));
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (broken)
            tag.putBoolean("Destroyed", true);
        return tag;
    }

    public static BulbBlock broken(Properties properties) {
        return new BulbBlock(properties, true);
    }

    @Override
    public SimulatedDeviceType<BulbDevice> getDevice() {
        return CEESimulatedDevices.BULB.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT, COMPACT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(COMPACT))
            return CEEShapes.BULB_COMPACT.get(state.getValue(FACING));
        return (state.getValue(ROLL) ? CEEShapes.BULB_ROLL : CEEShapes.BULB).get(state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof ServerLevel sl) {
            BulbDevice device = DevicesSavedData.load(sl).getDevice(pos, BulbDevice.class);
            if (device != null)
                device.destroyed = broken;
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(CEETags.BULB_REPAIR_ITEM) || !broken)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level instanceof ServerLevel sl) {
            BulbDevice device = DevicesSavedData.load(sl).getDevice(pos, BulbDevice.class);
            if (device != null)
                device.destroyed = false;
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
        }

        level.setBlockAndUpdate(pos, CEEBlocks.BULB.get().withPropertiesOf(state));
        if (!player.isCreative())
            stack.shrink(1);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return targetedFace.getAxis() == originalState.getValue(FACING).getAxis() ? super.getRotatedBlockState(originalState, targetedFace) : originalState.cycle(COMPACT);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (broken)
            return 0;
        return state.getValue(LIGHT);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(COMPACT))
            return state.getValue(ROLL) ?
                    CEENodeConfigurations.BULB_COMPACT.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                    CEENodeConfigurations.BULB_COMPACT.getNodes(state.getValue(FACING));
        return state.getValue(ROLL) ?
                CEENodeConfigurations.BULB.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.BULB.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (state.getValue(COMPACT))
            return state.getValue(ROLL) ?
                    CEENodeConfigurations.BULB_COMPACT.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                    CEENodeConfigurations.BULB_COMPACT.getNodePos(state.getValue(FACING), id);
        return state.getValue(ROLL) ?
                CEENodeConfigurations.BULB.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.BULB.getNodePos(state.getValue(FACING), id);
    }


    @Override
    public Class<BulbBlockEntity> getBlockEntityClass() {
        return BulbBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BulbBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.BULB.get();
    }
}
