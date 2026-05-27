package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class BuntingWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!ModList.get().isLoaded("supplementaries"))
            return;

        WireAttachment attachment = new WireAttachment(CEEWireAttachments.BUNTING.get());
        BuntingItem item = (BuntingItem) stack.getItem();
        attachment.data.putString("BaseColor", item.getColor().getName());

        attachToWire(point, level, player, stack, attachment);
    }

    @Override
    public boolean isActiveFor(ItemStack stack, Player player) {
        if (!ModList.get().isLoaded("supplementaries"))
            return false;
        return stack.getItem() instanceof BuntingItem;
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return attachmentColor();
    }
}
