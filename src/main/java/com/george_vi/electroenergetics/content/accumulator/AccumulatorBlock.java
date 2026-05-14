package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.block.IBE;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Map;

public class AccumulatorBlock extends DirectionalRolledDeviceBlock<AccumulatorDevice> implements IBE<AccumulatorBlockEntity> {

    public static final BooleanProperty FLIP = BooleanProperty.create("flip");
    public static final EnumProperty<AccumulatorStack> STACK = EnumProperty.create("stack", AccumulatorStack.class);

    public AccumulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(STACK, AccumulatorStack.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLIP, STACK);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public CompoundTag getDefaultDeviceData(Level level, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        if (state.getValue(STACK).isDouble())
            tag.putBoolean("IsDoubleCell", true);
        return tag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Vec3 position = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        Direction clickedFace = context.getClickedFace();
        BooleanBooleanPair desirableState = desirableState(position, clickedFace);

        boolean roll = desirableState.firstBoolean();
        boolean flip = desirableState.secondBoolean();

        return withWater(defaultBlockState().setValue(ROLL, roll).setValue(FLIP, flip)
                .setValue(FACING, clickedFace), context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(STACK).isDouble())
            return AllShapes.CASING_11PX.get(state.getValue(FACING));

        if (state.getValue(ROLL))
            return CEEShapes.ACCUMULATOR_SINGLE_ROLL.get(state.getValue(FACING));
        else
            return CEEShapes.ACCUMULATOR_SINGLE.get(state.getValue(FACING));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public SimulatedDeviceType<AccumulatorDevice> getDevice() {
        return CEESimulatedDevices.ACCUMULATOR.get();
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(STACK)
                .getNodes()
                .scale((state.getValue(FLIP) ? -1 : 1), 1, 1)
                .rotate(new Vec3(0, (state.getValue(ROLL) ? 90 : 0), 0))
                .getNodes(state.getValue(FACING));
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(STACK)
                .getNodes()
                .scale((state.getValue(FLIP) ? -1 : 1), 1, 1)
                .rotate(new Vec3(0, (state.getValue(ROLL) ? 90 : 0), 0))
                .getNodePos(state.getValue(FACING), id);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id % 2 == 0 ?
                CEELang.nodeLabel("positive") :
                CEELang.nodeLabel("negative");
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderHighlightBlock(RenderHighlightEvent.Block event, BlockState state) {
        BlockPos pos = event.getTarget().getBlockPos();
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = event.getTarget();

        Direction facing = state.getValue(AccumulatorBlock.FACING);
        boolean roll = state.getValue(AccumulatorBlock.ROLL);

        boolean clickedOnRight = AccumulatorBlock.clickedOnFirst(roll, facing,
                hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos)));

        if (state.getValue(AccumulatorBlock.STACK).isSingle())
            return;

        VertexConsumer vb = event.getMultiBufferSource()
                .getBuffer(RenderType.lines());

        Vec3 camPos = event.getCamera()
                .getPosition();

        PoseStack ms = event.getPoseStack();

        Vec3 localPos = Vec3.atLowerCornerOf(pos);

        ms.pushPose();
        ms.translate(localPos.x - camPos.x, localPos.y - camPos.y, localPos.z - camPos.z);

        VoxelShape shape;
        if (clickedOnRight)
            shape = roll ? CEEShapes.ACCUMULATOR_OUTLINE_RF.get(facing) : CEEShapes.ACCUMULATOR_OUTLINE_F.get(facing);
        else
            shape = roll ? CEEShapes.ACCUMULATOR_OUTLINE_R.get(facing) : CEEShapes.ACCUMULATOR_OUTLINE.get(facing);
        TrackBlockOutline.renderShape(shape, ms, vb, null);

        ms.popPose();

        event.setCanceled(true);
    }


    /**
     * @param position Clicked position x: [0.0 - 1.0], y: [0.0 - 1.0], z: [0.0 - 1.0]. Basically block-position-local
     * @return first: roll second: flip
     */
    public static BooleanBooleanPair desirableState(Vec3 position, Direction clickedFace) {
        double x = position.x();
        double y = position.y();
        double z = position.z();

        boolean roll = false;
        boolean flip = false;

        if (clickedFace.getAxis().isVertical()) {
            if (z > x && z > 1 - x) roll = true;
            else if (z < x && z < 1 - x) roll = flip = true;
            else if (z < x && z > 1 - x) flip = true;
            if (clickedFace == Direction.DOWN && roll)
                flip ^= true;
        } else {
            if (clickedFace.getAxis() == Direction.Axis.X) {
                x = x + z;
                z = x - z;
                x = x - z;
            }
            if (y > x && y > 1 - x) roll = true;
            else if (y < x && y < 1 - x) roll = flip = true;
            else if (y < x && y > 1 - x) flip = true;
            if (clickedFace.getAxisDirection() == Direction.AxisDirection.NEGATIVE && roll)
                flip ^= true;
            if (clickedFace == Direction.NORTH && !roll)
                flip ^= true;
            if (clickedFace == Direction.SOUTH && roll)
                flip ^= true;
            if (clickedFace == Direction.EAST)
                flip ^= true;
        }

        return BooleanBooleanPair.of(roll, flip);
    }

    public static boolean clickedOnFirst(boolean roll, Direction facing, Vec3 position) {
        boolean firstSlot = false;

        if (facing.getAxis().isVertical()) {
            firstSlot = (roll ? position.x() : position.z()) < 0.5;
            if (!roll && facing == Direction.DOWN)
                firstSlot = !firstSlot;
        }

        if (facing.getAxis() == Direction.Axis.X) {
            firstSlot = (roll ? position.z() : position.y()) > 0.5;
            if (roll && facing == Direction.WEST)
                firstSlot = !firstSlot;
        }

        if (facing.getAxis() == Direction.Axis.Z) {
            firstSlot = (roll ? position.x() : position.y()) > 0.5;
            if (roll && facing == Direction.SOUTH)
                firstSlot = !firstSlot;
        }

        if (roll)
            firstSlot = !firstSlot;

        return firstSlot;
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return (id1 <= 1 && id2 >= 2) || (id2 <= 1 && id1 >= 2);
    }

    @Override
    public Class<AccumulatorBlockEntity> getBlockEntityClass() {
        return AccumulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AccumulatorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ACCUMULATOR.get();
    }
}
