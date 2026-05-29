package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.foundation.CEELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MCBItem extends Item {
    public MCBItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int currentAmperage = stack.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);

        CEELang.builder().translate("tooltip.fuse.set_amperage", CEELang.formatAmperage(currentAmperage))
                .style(ChatFormatting.GRAY).addTo(tooltipComponents);
    }
}
