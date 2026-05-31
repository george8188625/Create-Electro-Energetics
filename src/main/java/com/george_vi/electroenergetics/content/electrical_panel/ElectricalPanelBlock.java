package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock;
import com.george_vi.electroenergetics.foundation.base.SimpleElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElectricalPanelBlock extends SimpleElectricalDeviceBlock<ElectricalPanelDevice> implements ProperOilAndWaterloggedBlock, IBE<ElectricalPanelBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ElectricalPanelNodeState> NODE_STATE = EnumProperty.create("node_state", ElectricalPanelNodeState.class);
    public static final EnumProperty<LoggedState> LOGGED_STATE = ProperOilAndWaterloggedBlock.LOGGED_STATE;
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public ElectricalPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(NODE_STATE, ElectricalPanelNodeState.NONE)
                .setValue(LOGGED_STATE, LoggedState.DRY)
                .setValue(TOP, false)
                .setValue(BOTTOM, false)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        return withWater(defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BOTTOM, !CEEBlocks.ELECTRICAL_PANEL.has(below))
                .setValue(TOP, !CEEBlocks.CURRENT_TRANSFORMER.has(above)),
                context);
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());

        updateWater(level, state, pos);

        boolean bottom = !CEEBlocks.ELECTRICAL_PANEL.has(below);
        boolean top = !CEEBlocks.ELECTRICAL_PANEL.has(above);

        return state.setValue(BOTTOM, bottom)
                .setValue(TOP, top);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.CASING_5PX.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, NODE_STATE, LOGGED_STATE, TOP, BOTTOM);
    }

    @Override
    public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
        return state.getValue(NODE_STATE).nodeConfigurator.getNodes(state.getValue(FACING));
    }

    @Override
    public @Nullable Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        return state.getValue(NODE_STATE).nodeConfigurator.getNodePos(state.getValue(FACING), id);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() != state.getValue(FACING) ||
                !(level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Direction facing = state.getValue(FACING);
        ElectricalPanelLayoutType layout = be.getLayoutType();
        Vec3 localClickPos = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos));

        ElectricalPanelSlot clickedSlot;
        PanelAttachmentType attachmentType = stack.is(CEETags.ELECTRICAL_PANEL_ATTACHMENT) ? PanelAttachmentType.getForItem(stack) : null;

        if (layout == ElectricalPanelLayoutType.FULL) {
            clickedSlot = ElectricalPanelSlot.FULL_SLOT;
        } else if (layout == ElectricalPanelLayoutType.THIRD) {
            clickedSlot = PanelAttachmentMode.THIRD.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.HALF_HORIZONTAL) {
            clickedSlot = PanelAttachmentMode.HALF_ONLY_HORIZONTAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.HALF_VERTICAL) {
            clickedSlot = PanelAttachmentMode.HALF_ONLY_VERTICAL.getSlot(facing, localClickPos);
        } else if (attachmentType != null) {

            clickedSlot = attachmentType.mode.getSlot(facing, localClickPos);
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (layout == ElectricalPanelLayoutType.NONE) {
            be.setLayoutType(layout = clickedSlot.layoutType());

            be.setAttachments(new PanelAttachment[layout.slots.length]);
        }

        int slotIndex = layout.getIndexOfSlot(clickedSlot);
        PanelAttachment[] attachments = be.getAttachments();

        if (attachments[slotIndex] == null) {
            if (attachmentType == null || !attachmentType.mode.isCompatible(layout))
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            // Insert attachment
            attachments[slotIndex] = attachmentType.createNew(pos,
                    attachmentType.mode.getNodesFor(slotIndex, pos, layout), level, layout.slots[slotIndex], facing);

            attachments[slotIndex].onInserted(stack, player, hand, hitResult);

            Component label = stack.get(DataComponents.CUSTOM_NAME);
            if (label != null)
                attachments[slotIndex].label = label.getString();

            if (!player.isCreative())
                stack.shrink(1);
            be.attachmentUpdate();
            return ItemInteractionResult.SUCCESS;
        } else {
            Vec3 rotatedClickPos = VecHelper.rotateCentered(localClickPos, facing.toYRot() + 180, Direction.Axis.Y);
            double clickedX = rotatedClickPos.x;
            double clickedY = rotatedClickPos.y;

            // If the player presses the panel, it won't cause an interaction.
            if (clickedX < 2/16f || clickedX > 14/16f || clickedY < 2/16f || clickedY > 14/16f)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            ItemInteractionResult result = attachments[slotIndex].onInteract(stack, player, hand, hitResult);
            if (result == null)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            be.sendData();
            return result;
        }
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (context.getClickedFace() != state.getValue(FACING) ||
                !(level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be))
            return super.onSneakWrenched(state, context);

        Vec3 localClickPos = context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos));

        int slotIndex = be.getHoveringAttachmentIndex(localClickPos);
        if (slotIndex == -1)
            return super.onSneakWrenched(state, context);

        if (be.getAttachments()[slotIndex] == null)
            return super.onSneakWrenched(state, context);


        PanelAttachment attachment = be.getAttachments()[slotIndex];
        be.getAttachments()[slotIndex] = null;
        if (Arrays.stream(be.getAttachments()).allMatch(Objects::isNull)) {
            be.setLayoutType(ElectricalPanelLayoutType.NONE);
            be.setAttachments(new PanelAttachment[0]);
        }

        attachment.onRemoved(player);
        if (player != null && !player.isCreative()) {
            attachment.getDrops()
                    .forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
        }
        IWrenchable.playRemoveSound(level, pos);

        be.attachmentUpdate();
        return InteractionResult.SUCCESS;

    }


    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ElectricalPanelBlockEntity be))
            return drops;

        for (PanelAttachment attachment : be.getAttachments())
            if (attachment != null)
                drops.addAll(attachment.getDrops());
        return drops;
    }

        @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public SimulatedDeviceType<ElectricalPanelDevice> getDevice() {
        return CEESimulatedDevices.ELECTRICAL_PANEL.get();
    }

    @Override
    public Class<ElectricalPanelBlockEntity> getBlockEntityClass() {
        return ElectricalPanelBlockEntity.class;
    }

    @Override
    public boolean canSelfConnect(Level level, BlockPos pos, BlockState state, int id1, int id2) {
        return true;
    }

    @Override
    public BlockEntityType<? extends ElectricalPanelBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ELECTRICAL_PANEL.get();
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (!(level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be))
            return super.getNodeLabel(level, pos, state, id);
        for (PanelAttachment attachment : be.getAttachments()) {
            if (attachment == null)
                continue;

            InWorldNode[] nodes = attachment.nodes;
            for (int i = 0; i < nodes.length; i++) {
                InWorldNode node = nodes[i];
                if (node.id() == id)
                    return attachment.getNodeLabel(level, pos, state, i);
            }
        }
        return super.getNodeLabel(level, pos, state, id);
    }
}
