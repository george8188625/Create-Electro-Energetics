package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class DamperWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        WireAttachment attachment = new WireAttachment(CEEWireAttachments.DAMPER.get());
        attachToWire(point, level, player, stack, attachment);
    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        return stack.getItem() == Items.IRON_INGOT;
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return attachmentColor();
    }
}
