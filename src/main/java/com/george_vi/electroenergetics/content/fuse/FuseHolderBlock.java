package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class FuseHolderBlock extends DirectionalRolledDeviceBlock implements IBE<FuseHolderBlockEntity> {
    public FuseHolderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.REDSTONE_RELAY.get(state.getValue(FACING));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean firstSlot = false;
        Vec3 position = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos));
        boolean roll = state.getValue(ROLL);
        Direction facing = state.getValue(FACING);

        if (facing.getAxis().isVertical()) {
            firstSlot = (roll ? position.z() : position.x()) < 0.5;
        }

        if (facing.getAxis() == Direction.Axis.X) {
            firstSlot = (roll ? position.y() : position.z()) < 0.5;
            if (!roll && facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                firstSlot = !firstSlot;
        }

        if (facing.getAxis() == Direction.Axis.Z) {
            firstSlot = (roll ? position.y() : position.x()) < 0.5;
            if (!roll && facing.getAxisDirection() == Direction.AxisDirection.POSITIVE)
                firstSlot = !firstSlot;
        }

        if (roll)
            firstSlot = !firstSlot;

        if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be) {
                Pair<FuseHoldable, CompoundTag> fuse = firstSlot ? be.firstFuse : be.secondFuse;
                if (firstSlot)
                    be.firstFuse = null;
                else
                    be.secondFuse = null;
                be.updateFuses();
                if (fuse != null)
                    player.getInventory().placeItemBackInInventory(fuse.getFirst().getDrops(fuse.getSecond()));
                AllSoundEvents.CRAFTER_CLICK.playOnServer(level, pos);
            }
            return ItemInteractionResult.SUCCESS;
        }
        for (FuseHoldable type : FuseHoldable.ALL.values()) {
            if (type.isValid(stack)) {
                if (level.getBlockEntity(pos) instanceof FuseHolderBlockEntity be) {
                    if ((firstSlot ? be.firstFuse : be.secondFuse) != null)
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    CompoundTag tag = new CompoundTag();
                    if (firstSlot)
                        be.firstFuse = Pair.of(type, tag);
                    else
                        be.secondFuse = Pair.of(type, tag);
                    type.onPlace(tag, stack, level, pos);
                    be.updateFuses();
                    stack.shrink(1);
                    AllSoundEvents.CRAFTER_CLICK.playOnServer(level, pos);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    protected SimulatedDevice getDevice() {
        return CEESimulatedDevices.FUSE_HOLDER;
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_SWITCH_ROLL.getNodes(state.getValue(FACING)) :
                CEENodeConfigurations.DOUBLE_SWITCH.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(ROLL) ?
                CEENodeConfigurations.DOUBLE_SWITCH_ROLL.getNodePos(state.getValue(FACING), id) :
                CEENodeConfigurations.DOUBLE_SWITCH.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public Class<FuseHolderBlockEntity> getBlockEntityClass() {
        return FuseHolderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FuseHolderBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.FUSE_HOLDER.get();
    }
}
