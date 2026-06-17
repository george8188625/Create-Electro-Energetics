package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelSlot;
import com.george_vi.electroenergetics.content.electrical_panel.PanelAttachmentMode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class PanelAttachmentType {

    private final PanelAttachmentFactory factory;
    public final ItemLike item;
    public final PanelAttachmentMode mode;

    public PanelAttachmentType(PanelAttachmentFactory factory, ItemLike item, PanelAttachmentMode mode) {
        this.factory = factory;
        this.item = item;
        this.mode = mode;
    }

    public PanelAttachment createNew(BlockPos pos, InWorldNode[] nodes, Level level, ElectricalPanelSlot slot,
                                     Direction panelFacing, HolderLookup.Provider registries) {

        PanelAttachment panelAttachment = factory.create(this);
        panelAttachment.pos = pos;
        panelAttachment.nodes = nodes;
        panelAttachment.level = level;
        panelAttachment.slot = slot;
        panelAttachment.panelFacing = panelFacing;
        panelAttachment.read(new CompoundTag(), false, registries);
        return panelAttachment;
    }

    public static PanelAttachmentType getForItem(ItemStack stack) {
        if (!stack.isEmpty())
            for (PanelAttachmentType type : CEERegistries.PANEL_ATTACHMENT_TYPE) {
                if (type.item.asItem() != Items.AIR && type.item.asItem() == stack.getItem())
                    return type;
            }
        return null;
    }

    public interface PanelAttachmentFactory {
        PanelAttachment create(PanelAttachmentType type);
    }
}
