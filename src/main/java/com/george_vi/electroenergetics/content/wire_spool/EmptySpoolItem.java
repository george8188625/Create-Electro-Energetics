package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class EmptySpoolItem extends Item {
    public EmptySpoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!player.isShiftKeyDown())
            return super.use(level, player, usedHand);

        ItemStack heldItem = player.getItemInHand(usedHand);
        if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE))
            heldItem.remove(CEEDataComponents.SELECTED_NODE);
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(CEEDataComponents.SELECTED_NODE);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();

        if (player.isShiftKeyDown()) {
            if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE)) {
                heldItem.remove(CEEDataComponents.SELECTED_NODE);
                if (level.isClientSide)
                    player.displayClientMessage(Component.translatable("electroenergetics.wire_spool.cancelled_connection"), true);
            }
            return InteractionResult.SUCCESS;
        }

        InWorldNode hoveredNode = InWorldNode.closestNode(level, context.getClickLocation(), 1.5f);

        if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE)) {
            if (!(player.level() instanceof ServerLevel sl))
                return InteractionResult.SUCCESS;

            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            InWorldNode originalNode = heldItem.get(CEEDataComponents.SELECTED_NODE);

            if ((originalNode == null || hoveredNode == null) || !sd.isConnected(originalNode, hoveredNode)) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                return InteractionResult.FAIL;
            }
            WireData wireData = null;
            if ((level.getBlockState(hoveredNode.sourcePos()).getBlock() instanceof CatenaryHolderBlock) && (level.getBlockState(originalNode.sourcePos()).getBlock() instanceof CatenaryHolderBlock))
                sd.removeCatenary(hoveredNode.sourcePos(), originalNode.sourcePos());
            else
                wireData = sd.removeConnection(new InWorldNodeConnection(originalNode, hoveredNode));

            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos);

            if (!player.isCreative()) {
                heldItem.shrink(1);
                if (wireData == null)
                    player.getInventory().placeItemBackInInventory(CEEItems.COPPER_WIRE_SPOOL.asStack());
                else
                    player.getInventory().placeItemBackInInventory(wireData.wireType().getSpooledItem().getDefaultInstance());
            }

            if (heldItem.has(CEEDataComponents.SELECTED_NODE))
                heldItem.remove(CEEDataComponents.SELECTED_NODE);

            return InteractionResult.SUCCESS;
        }

        heldItem.set(CEEDataComponents.SELECTED_NODE, hoveredNode);

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);

        return InteractionResult.SUCCESS;
    }
}
