package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.wire.WireTargeting;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EmptySpoolItem extends Item implements WireTargeting {
    public EmptySpoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (tryUseOnWire(level, player, usedHand) == InteractionResult.SUCCESS)
            return InteractionResultHolder.success(player.getItemInHand(usedHand));

        if (!player.isShiftKeyDown())
            return super.use(level, player, usedHand);

        ItemStack heldItem = player.getItemInHand(usedHand);
        if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE))
            heldItem.remove(CEEDataComponents.SELECTED_NODE);
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();

        if (tryUseOnWire(level, player, context.getHand()) == InteractionResult.SUCCESS)
            return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE))
                heldItem.remove(CEEDataComponents.SELECTED_NODE);
            return InteractionResult.SUCCESS;
        }

        Node hoveredNode = Node.closestNode(level, context.getClickLocation(), 1f);

        if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE)) {
            if (!(player.level() instanceof ServerLevel sl))
                return InteractionResult.SUCCESS;

            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            Node originalNode = heldItem.get(CEEDataComponents.SELECTED_NODE);

            if ((originalNode == null || hoveredNode == null) || !sd.isConnected(originalNode, hoveredNode)) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                return InteractionResult.FAIL;
            }

            sd.removeConnection(new NodeConnection(originalNode, hoveredNode));

            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos);

            if (!player.isCreative()) {
                heldItem.shrink(1);
                player.getInventory().placeItemBackInInventory(CEEItems.WIRE_SPOOL.asStack());
            }

            if (heldItem.has(CEEDataComponents.SELECTED_NODE))
                heldItem.remove(CEEDataComponents.SELECTED_NODE);

            return InteractionResult.SUCCESS;
        }

        heldItem.set(CEEDataComponents.SELECTED_NODE, hoveredNode);

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(player.level() instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);

        sd.removeConnection(new NodeConnection(point.node1(), point.node2()));

        AllSoundEvents.WRENCH_REMOVE.playOnServer(level, BlockPos.containing(point.posAt(Vec3.atCenterOf(point.node1().sourcePos()), Vec3.atCenterOf(point.node2().sourcePos()))));

        if (!player.isCreative()) {
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(CEEItems.WIRE_SPOOL.asStack());
        }

        if (stack.has(CEEDataComponents.SELECTED_NODE))
            stack.remove(CEEDataComponents.SELECTED_NODE);
    }

    @Override
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return DisplayType.LINE;
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return 0xff7171;
    }
}
