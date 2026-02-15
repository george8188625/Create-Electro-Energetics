package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.WireType;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Stack;
import java.util.function.Supplier;

public class WireSpoolItem extends Item {
    final Supplier<WireType> wireType;

    public WireSpoolItem(Properties properties, Supplier<WireType> wireType) {
        super(properties);
        this.wireType = wireType;
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
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (player.isShiftKeyDown()) {
            if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE))
                heldItem.remove(CEEDataComponents.SELECTED_NODE);
            return InteractionResult.SUCCESS;
        }

        if (!(state.getBlock() instanceof DeviceBlock db))
            return InteractionResult.PASS;

        InWorldNode hoveredNode = InWorldNode.closestNode(level, context.getClickLocation(), 1f);


        if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE)) {
            if (!(player.level() instanceof ServerLevel sl))
                return InteractionResult.SUCCESS;

            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            InWorldNode originalNode = heldItem.get(CEEDataComponents.SELECTED_NODE);

            if ((hoveredNode == null || originalNode == null || !sd.getNodes().contains(originalNode)) ||
                    hoveredNode.equals(originalNode) ||
                    (hoveredNode.sourcePos().equals(originalNode.sourcePos()) && !db.canSelfConnect(level, pos, state, hoveredNode.id(), originalNode.id())) ||
                    (sd.isConnected(hoveredNode, originalNode)) ||
                    Math.sqrt(originalNode.sourcePos().distSqr(hoveredNode.sourcePos())) > wireType.get().getMaxLength()) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                return InteractionResult.FAIL;
            }

            if (wireType.get() == CEEWireTypes.COPPER.get() && db instanceof CatenaryHolderBlock && (level.getBlockState(originalNode.sourcePos()).getBlock() instanceof CatenaryHolderBlock)) {
                if (Math.sqrt(originalNode.sourcePos().distSqr(hoveredNode.sourcePos())) > CEEConfigs.server().maxCatenaryLength.get()) {
                    AllSoundEvents.DENY.playOnServer(level, pos);
                    return InteractionResult.FAIL;
                }

                sd.connectCatenary(hoveredNode.sourcePos(), originalNode.sourcePos());
                WireSparkEffectTicker.placedConnections.computeIfAbsent(level, l -> new Stack<>()).add(Pair.of(new InWorldNodeConnection(originalNode, hoveredNode), Pair.of(hoveredNode.getPosition(level), player)));
            } else {
                if (wireType.get() == CEEWireTypes.STANDARD.get() && player.getOffhandItem().getItem() instanceof DyeItem di) {
                    WireType newWiretype = CEEWireTypes.COLORED_WIRES.getOrDefault(di.getDyeColor(), CEEWireTypes.STANDARD).get();
                    sd.connect(originalNode, hoveredNode, newWiretype);
                } else
                    sd.connect(originalNode, hoveredNode, wireType.get());
                if (!level.isClientSide)
                    WireSparkEffectTicker.placedConnections.computeIfAbsent(level, l -> new Stack<>()).add(Pair.of(new InWorldNodeConnection(originalNode, hoveredNode), Pair.of(hoveredNode.getPosition(level), player)));
            }

            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos);

            if (!player.isCreative()) {
                heldItem.shrink(1);
                player.getInventory().placeItemBackInInventory(CEEItems.EMPTY_SPOOL.asStack());
            }

            if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE))
                heldItem.remove(CEEDataComponents.SELECTED_NODE);

            return InteractionResult.SUCCESS;
        }

        heldItem.set(CEEDataComponents.SELECTED_NODE, hoveredNode);

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);

        return InteractionResult.SUCCESS;
    }
}
