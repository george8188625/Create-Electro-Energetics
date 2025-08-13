package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        WireAttachment attachment = new WireAttachment(CEEWireAttachments.BANNER.get());
        BannerItem item = (BannerItem) stack.getItem();
        attachment.data.putString("BaseColor", item.getColor().getName());
        BannerPatternLayers pattern = stack.get(DataComponents.BANNER_PATTERNS);
        if (pattern != null)
            attachment.data.put("Pattern", BannerPatternLayers.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), pattern).getOrThrow());

        attachToWire(point, level, player, stack, attachment);
    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        return stack.getItem() instanceof BannerItem;
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return attachmentColor();
    }
}
