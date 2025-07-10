package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.Node;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.Map;

public class WireSpoolItem extends Item {
    public WireSpoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack heldItem = player.getItemInHand(usedHand);
            if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED_POS))
                heldItem.remove(CEEDataComponents.CONNECTOR_CLICKED_POS);
            if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED))
                heldItem.remove(CEEDataComponents.CONNECTOR_CLICKED);
            return InteractionResultHolder.success(heldItem);
        }
        return super.use(level, player, usedHand);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (player.isShiftKeyDown()) {
            if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED_POS))
                heldItem.remove(CEEDataComponents.CONNECTOR_CLICKED_POS);
            if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED))
                heldItem.remove(CEEDataComponents.CONNECTOR_CLICKED);
            return InteractionResult.SUCCESS;
        }

        if (!(state.getBlock() instanceof DeviceBlock db))
            return InteractionResult.PASS;


        // Get clicked Node

        int closestSlot = 0;
        double closestDistance = 100;
        Vec3 lookingPos = context.getClickLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        for (Map.Entry<Vec3, Integer> n : db.getNodePositions(level, pos, state).entrySet()) {
            if (lookingPos.distanceTo(n.getKey()) < closestDistance) {
                closestDistance = lookingPos.distanceTo(n.getKey());
                closestSlot = n.getValue();
            }
        }

        // Connect / Select

        if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED_POS) && heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED)) {
            if (!(player.level() instanceof ServerLevel sl))
                return InteractionResult.SUCCESS;

            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            Integer selectedID = heldItem.get(CEEDataComponents.CONNECTOR_CLICKED);
            BlockPos selectedPos =  heldItem.get(CEEDataComponents.CONNECTOR_CLICKED_POS);

            if (selectedID == null || selectedPos == null)
                return InteractionResult.FAIL;

            Node node1 = sd.getNode(selectedPos, selectedID);
            Node node2 = sd.getNode(pos, closestSlot);

            if ((node1 == null || node2 == null) ||
                    (node1.sourcePos().equals(node2.sourcePos()) && !db.canSelfConnect(level, pos, state, node1.id(), node2.id())) ||
                    (sd.isConnected(node1, node2)) ||
                    selectedPos.distManhattan(pos) > CEEConfigs.server().maxWireLength.get()) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                return InteractionResult.FAIL;
            }

            sd.connect(node1, node2);

            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos);

            if (!player.isCreative()) {
                heldItem.shrink(1);
                player.getInventory().placeItemBackInInventory(CEEItems.EMPTY_SPOOL.asStack());
            }

            if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED_POS))
                heldItem.remove(CEEDataComponents.CONNECTOR_CLICKED_POS);
            if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED_POS))
                heldItem.remove(CEEDataComponents.CONNECTOR_CLICKED_POS);

            return InteractionResult.SUCCESS;
        }

        heldItem.set(CEEDataComponents.CONNECTOR_CLICKED_POS, pos);
        heldItem.set(CEEDataComponents.CONNECTOR_CLICKED, closestSlot);

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);

        return InteractionResult.SUCCESS;
    }
}
