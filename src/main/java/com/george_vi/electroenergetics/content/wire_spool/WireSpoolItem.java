package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.wire.interaction.IInteractDetachedNodes;
import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
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
import net.minecraft.world.phys.Vec3;

import java.util.Stack;
import java.util.function.Supplier;

public class WireSpoolItem extends Item implements IInteractDetachedNodes {
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
        if (heldItem.has(CEEDataComponents.SELECTED_NODE)) {
            heldItem.remove(CEEDataComponents.SELECTED_NODE);
            if (level.isClientSide)
                player.displayClientMessage(CEELang.translateDirect("wire_spool.cancelled_connection"), true);
        }
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
        BlockState state = level.getBlockState(pos);

        if (player != null && player.isShiftKeyDown()) {
            if (heldItem.has(CEEDataComponents.SELECTED_NODE)) {
                heldItem.remove(CEEDataComponents.SELECTED_NODE);
                if (level.isClientSide)
                    player.displayClientMessage(CEELang.translateDirect("wire_spool.cancelled_connection"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (!(state.getBlock() instanceof ElectricalDeviceBlock<?> db))
            return InteractionResult.PASS;

        InWorldNode hoveredNode = InWorldNode.closestNode(level, context.getClickLocation(), 1.5f);

        if (hoveredNode == null)
            hoveredNode = InWorldNode.closestNode(level, pos, state, 1.5f, context.getClickLocation());


        if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE)) {
            if (!(level instanceof ServerLevel sl))
                return InteractionResult.SUCCESS;

            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            InWorldNode originalNode = heldItem.get(CEEDataComponents.SELECTED_NODE);

            if (hoveredNode == null || originalNode == null) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                return InteractionResult.FAIL;
            }

            BlockPos originalPos = originalNode.sourcePos();
            BlockPos hoveredPos = hoveredNode.sourcePos();

            if (hoveredNode.equals(originalNode) ||
                    (hoveredPos.equals(originalPos) && !db.canSelfConnect(level, pos, state, hoveredNode.id(), originalNode.id())) ||
                    (sd.isConnected(hoveredNode, originalNode)) ||
                    Math.sqrt(originalNode.sableSourcePos(level).distSqr(hoveredNode.sableSourcePos(level))) > wireType.get().getMaxLength()) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                return InteractionResult.FAIL;
            }

            BlockState originalState = level.getBlockState(originalPos);
            BlockState hoveredState = level.getBlockState(hoveredPos);

            if (originalState.getBlock() instanceof ElectricalDeviceBlock<?> edb)
                edb.ensureNodesExist(sl, originalPos, originalState);

            if (hoveredState.getBlock() instanceof ElectricalDeviceBlock<?> edb)
                edb.ensureNodesExist(sl, hoveredPos, hoveredState);

            if (!sd.getNodes().contains(hoveredNode) || !sd.getNodes().contains(originalNode))
                return InteractionResult.FAIL;

            if (wireType.get() == CEEWireTypes.COPPER.get() && db instanceof CatenaryHolderBlock && (level.getBlockState(originalPos).getBlock() instanceof CatenaryHolderBlock)) {
                if (Math.sqrt(originalPos.distSqr(hoveredPos)) > CEEConfigs.server().maxCatenaryLength.get()) {
                    AllSoundEvents.DENY.playOnServer(level, pos);
                    return InteractionResult.FAIL;
                }

                CEEAdvancements.CONNECT_WIRES.awardTo(player);
                sd.connectCatenary(hoveredPos, originalPos);
                WireSparkEffectTicker.placedConnections.computeIfAbsent(level, l -> new Stack<>()).add(Pair.of(new InWorldNodeConnection(originalNode, hoveredNode), Pair.of(hoveredNode.getPosition(level), player)));
            } else {
                if (wireType.get() instanceof WireType.Dyeable dyeableWire && player.getOffhandItem().getItem() instanceof DyeItem di) {
                    WireType newWiretype = dyeableWire.getDyed(di.getDyeColor());
                    sd.connect(originalNode, hoveredNode, newWiretype);
                } else
                    sd.connect(originalNode, hoveredNode, wireType.get());

                CEEAdvancements.CONNECT_WIRES.awardTo(player);
                if (!level.isClientSide)
                    WireSparkEffectTicker.placedConnections.computeIfAbsent(level, l -> new Stack<>()).add(Pair.of(new InWorldNodeConnection(originalNode, hoveredNode), Pair.of(hoveredNode.getPosition(level), player)));
            }

            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos);

            if (!player.isCreative()) {
                heldItem.shrink(1);
                if (!CEEConfigs.server().alternateWirePlacement.get())
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

    @Override
    public void interactDetachedNode(InWorldNode hoveredNode, Player player, Level level, ItemStack heldItem) {

        if (player.isShiftKeyDown()) {
            if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE)) {
                heldItem.remove(CEEDataComponents.SELECTED_NODE);
                if (level.isClientSide)
                    player.displayClientMessage(CEELang.translateDirect("wire_spool.cancelled_connection"), true);
            }
            return;
        }
        InWorldNode originalNode = heldItem.get(CEEDataComponents.SELECTED_NODE);

        if (originalNode != null) {
            if (!(level instanceof ServerLevel sl))
                return;

            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);


            BlockPos originalPos = originalNode.sourcePos();
            BlockPos hoveredPos = hoveredNode.sourcePos();
            Vec3 actualHoverPos = hoveredNode.getPosition(level);

            if (actualHoverPos == null)
                return;

            if (hoveredNode.equals(originalNode) ||
                    (sd.isConnected(hoveredNode, originalNode)) ||
                    Math.sqrt(originalNode.sableSourcePos(level).distSqr(hoveredNode.sableSourcePos(level))) > wireType.get().getMaxLength()) {
                AllSoundEvents.DENY.playOnServer(level, BlockPos.containing(actualHoverPos));
                return;
            }

            BlockState originalState = level.getBlockState(originalPos);
            BlockState hoveredState = level.getBlockState(hoveredPos);

            if (originalState.getBlock() instanceof ElectricalDeviceBlock<?> edb)
                edb.ensureNodesExist(sl, originalPos, originalState);

            if (hoveredState.getBlock() instanceof ElectricalDeviceBlock<?> edb)
                edb.ensureNodesExist(sl, hoveredPos, hoveredState);

            if (!sd.getNodes().contains(hoveredNode) || !sd.getNodes().contains(originalNode))
                return;

            if (wireType.get() instanceof WireType.Dyeable dyeableWire && player.getOffhandItem().getItem() instanceof DyeItem di) {
                WireType newWiretype = dyeableWire.getDyed(di.getDyeColor());
                sd.connect(originalNode, hoveredNode, newWiretype);
            } else
                sd.connect(originalNode, hoveredNode, wireType.get());

            CEEAdvancements.CONNECT_WIRES.awardTo(player);
            if (!level.isClientSide)
                WireSparkEffectTicker.placedConnections.computeIfAbsent(level, l -> new Stack<>()).add(Pair.of(new InWorldNodeConnection(originalNode, hoveredNode), Pair.of(hoveredNode.getPosition(level), player)));


            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, BlockPos.containing(actualHoverPos));

            if (!player.isCreative()) {
                heldItem.shrink(1);
                player.getInventory().placeItemBackInInventory(CEEItems.EMPTY_SPOOL.asStack());
            }

            if (heldItem.getComponents().has(CEEDataComponents.SELECTED_NODE))
                heldItem.remove(CEEDataComponents.SELECTED_NODE);
            return;
        }

        heldItem.set(CEEDataComponents.SELECTED_NODE, hoveredNode);

    }
}
