package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DamperWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        WireAttachment attachment = new WireAttachment(CEEWireAttachments.DAMPER.get());
        attachToWire(point, level, player, stack, attachment);
    }

    @Override
    public boolean isActiveFor(ItemStack stack, Player player) {
        return stack.is(CEETags.WIRE_DAMPER_ITEM);
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return attachmentColor();
    }
}
