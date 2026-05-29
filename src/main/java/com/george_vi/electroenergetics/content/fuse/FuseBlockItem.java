package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class FuseBlockItem extends BlockItem {
    public final boolean broken;

    FuseBlockItem(Block block, Properties properties, boolean broken) {
        super(block, properties);
        this.broken = broken;
    }

    public static FuseBlockItem standard(Block block, Properties properties) {
        return new FuseBlockItem(block, properties, false);
    }

    public static FuseBlockItem blown(Block block, Properties properties) {
        return new FuseBlockItem(block, properties, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int currentAmperage = stack.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);

        CEELang.builder().translate("tooltip.fuse.set_amperage", CEELang.formatAmperage(currentAmperage))
                .style(ChatFormatting.GRAY).addTo(tooltipComponents);
    }

    private static boolean playScroll = false;

    @OnlyIn(Dist.CLIENT)
    public static boolean mouseScrolled(double delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return false;

        ItemStack heldItem = mc.player.getMainHandItem();
        if (!heldItem.is(CEETags.FUSE_AMPERAGE_SETTING))
            return false;

        if (!AllKeys.altDown())
            return false;

        int currentAmperage = heldItem.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);

        int change = (int) delta;
        int flooredAmperage = currentAmperage;
        if (!AllKeys.ctrlDown())
            if (currentAmperage >= 100) {
                flooredAmperage = Mth.floor(currentAmperage / 10f) * 10;
                change *= 10;
            } else if (currentAmperage >= 15) {
                flooredAmperage = Mth.floor(currentAmperage / 5f) * 5;
                change *= 5;
            }
        int target = flooredAmperage + change;
        target = Mth.clamp(target, 1, CEEConfigs.server().maxFuseAmperage.get());

        if (target != currentAmperage) {
            CatnipServices.NETWORK.sendToServer(new ConfigureFusePacket(target));
            playScroll = true;
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static void tickClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        if (playScroll) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    AllSoundEvents.SCROLL_VALUE.getMainEvent(), SoundSource.PLAYERS,
                    0.25f, 1f, false);
            playScroll = false;
        }

        ItemStack heldItem = mc.player.getMainHandItem();

        boolean shouldDisplayAmperage = (heldItem.is(CEETags.FUSE_AMPERAGE_SETTING));

        if (shouldDisplayAmperage) {
            int currentAmperage = heldItem.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);

            ElectricPropertiesOverlay.INSTANCE.setAmperageSetting(currentAmperage);
        } else {
            ElectricPropertiesOverlay.INSTANCE.removeAmperageSetting();
        }
    }
}
