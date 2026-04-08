package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IndicatorBulbBlock extends DirectionalRolledDeviceBlock<IndicatorBulbDevice> implements IBE<IndicatorBulbBlockEntity> {
    public static IntegerProperty SIDE = IntegerProperty.create("side", 0, 2);

    public IndicatorBulbBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIDE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Vec3 position = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        double x = position.x();
        double y = position.y();
        double z = position.z();
        
        if (context.getClickedFace().getAxis().isVertical()) {
            int side = 0;
            if (z > x && z > 1 - x) side = 0b10;
            else if (z > x && z < 1 - x) side = 0b00;
            else if (z < x && z < 1 - x) side = 0b11;
            else if (z < x && z > 1 - x) side = 0b01;
            if (context.getClickedFace() == Direction.DOWN && (side & 2) == 2)
                side = (side & 2) | ((~side) & 1);

            return withWater(defaultBlockState().setValue(ROLL, (side & 2) == 2).setValue(SIDE, (side & 1)).setValue(FACING, context.getClickedFace()), context);
        } else {
            int side = 0;
            if (context.getClickedFace().getAxis() == Direction.Axis.X) {
                x = x + z;
                z = x - z;
                x = x - z;
            }
            if (y > x && y > 1 - x) side = 0b10;
            else if (y > x && y < 1 - x) side = 0b00;
            else if (y < x && y < 1 - x) side = 0b11;
            else if (y < x && y > 1 - x) side = 0b01;
            if (context.getClickedFace().getAxisDirection() == Direction.AxisDirection.NEGATIVE && (side & 2) == 2)
                side = (side & 2) | ((~side) & 1);
            if (context.getClickedFace() == Direction.NORTH && (side & 2) == 0)
                side = (side & 2) | ((~side) & 1);
            if (context.getClickedFace() == Direction.SOUTH && (side & 2) == 2)
                side = (side & 2) | ((~side) & 1);
            if (context.getClickedFace() == Direction.EAST)
                side = (side & 2) | ((~side) & 1);

            return withWater(defaultBlockState().setValue(ROLL, (side & 2) == 2).setValue(SIDE, (side & 1)).setValue(FACING, context.getClickedFace()), context);
        
        }
//        BlockState state = super.getStateForPlacement(context);
//        if (state == null)
//            return null;
//        return state.setValue(SIDE, clickedOnFirst(state, context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()))) ? 0 : 1);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        int side = state.getValue(SIDE);

        if (!(world instanceof ServerLevel) || side != 2)
            return super.onSneakWrenched(state, context);

        boolean clickedOnFirst = clickedOnFirst(state, context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos())));

        if (player != null && !player.isCreative())
            player.getInventory().placeItemBackInInventory(CEEBlocks.INDICATOR_BULB.asStack());

        world.setBlockAndUpdate(pos, state.setValue(SIDE, clickedOnFirst ? 1 : 0));
        IWrenchable.playRemoveSound(world, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof DyeItem di))
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        boolean clickedOnFirst = clickedOnFirst(state, hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos)));

        if (level.getBlockEntity(pos) instanceof IndicatorBulbBlockEntity be) {
            if (clickedOnFirst)
                be.firstColor = di.getDyeColor();
            else
                be.secondColor = di.getDyeColor();

            if (!level.isClientSide)
                be.sendData();
        }

        return ItemInteractionResult.SUCCESS;
    }

    public static boolean clickedOnFirst(BlockState state, Vec3 position) {
        boolean firstSlot = false;
        boolean roll = state.getValue(ROLL);
        Direction facing = state.getValue(FACING);

        if (facing.getAxis().isVertical()) {
            firstSlot = (roll ? position.z()  : position.x()) < 0.5;
            if (roll && facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                firstSlot = !firstSlot;
        }

        if (facing.getAxis() == Direction.Axis.X) {
            firstSlot = (roll ? position.y() : position.z()) > 0.5;
            if (!roll && facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                firstSlot = !firstSlot;
        }

        if (facing.getAxis() == Direction.Axis.Z) {
            firstSlot = (roll ? position.y() : position.x()) > 0.5;
            if (!roll && facing.getAxisDirection() == Direction.AxisDirection.POSITIVE)
                firstSlot = !firstSlot;
        }

        if (roll)
            firstSlot = !firstSlot;

        return firstSlot;
    }

    @Override
    public SimulatedDeviceType<IndicatorBulbDevice> getDevice() {
        return CEESimulatedDevices.INDICATOR_BULB.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int side = state.getValue(SIDE);
        if (state.getValue(ROLL))
            return side == 0 ? CEEShapes.INDICATOR_BULB_0_ROLL.get(state.getValue(FACING)) :
                    side == 1 ? CEEShapes.INDICATOR_BULB_1_ROLL.get(state.getValue(FACING)) :
                            Shapes.or(CEEShapes.INDICATOR_BULB_0_ROLL.get(state.getValue(FACING)), CEEShapes.INDICATOR_BULB_1_ROLL.get(state.getValue(FACING)));
        return side == 0 ? CEEShapes.INDICATOR_BULB_0.get(state.getValue(FACING)) :
                side == 1 ? CEEShapes.INDICATOR_BULB_1.get(state.getValue(FACING)) :
                        Shapes.or(CEEShapes.INDICATOR_BULB_0.get(state.getValue(FACING)), CEEShapes.INDICATOR_BULB_1.get(state.getValue(FACING)));
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        int side = state.getValue(SIDE);
        NodeConfigurator nodeConfigurator = side == 0 ? CEENodeConfigurations.INDICATOR_BULB_0 : CEENodeConfigurations.INDICATOR_BULB_FULL;
        Map<Integer, Vec3> r;
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            r = nodeConfigurator.rotate(new Vec3(0, 90, 0)).getNodes(state.getValue(FACING));
        else
            r = nodeConfigurator.getNodes(state.getValue(FACING));
        if (side == 1) {
            r.remove(0);
            r.remove(1);
        }
        return r;
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        int side = state.getValue(SIDE);
        NodeConfigurator nodeConfigurator = side == 0 ? CEENodeConfigurations.INDICATOR_BULB_0 : CEENodeConfigurations.INDICATOR_BULB_FULL;
        if (state.getValue(DirectionalRolledDeviceBlock.ROLL))
            return nodeConfigurator.rotate(new Vec3(0, 90, 0)).getNodePos(state.getValue(FACING), id);
        return nodeConfigurator.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        IndicatorBulbDevice device = DevicesSavedData.load(level).getDevice(pos, IndicatorBulbDevice.class);
        if (device != null)
            device.side = state.getValue(SIDE).byteValue();
    }

    @Override
    public Class<IndicatorBulbBlockEntity> getBlockEntityClass() {
        return IndicatorBulbBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends IndicatorBulbBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.INDICATOR_BULB.get();
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return !((id1 == 1 && id2 == 0) || (id1 == 0 && id2 == 1) || (id1 == 2 && id2 == 3) || (id1 == 3 && id2 == 2));
    }
}
