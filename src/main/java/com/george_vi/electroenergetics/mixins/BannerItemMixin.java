package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireTargeting;
import com.george_vi.electroenergetics.foundation.NodeConnectionPoint;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BannerItem.class)
public class BannerItemMixin extends StandingAndWallBlockItem implements WireTargeting {
    @Shadow
    DyeColor getColor() {
        throw new AssertionError();
    }

    public BannerItemMixin(Block block, Block wallBlock, Properties properties, Direction attachmentDirection) {
        super(block, wallBlock, properties, attachmentDirection);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (tryUseOnWire(level, player, usedHand) == InteractionResult.SUCCESS)
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        return super.use(level, player, usedHand);
    }

    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        WireAttachment attachment = new WireAttachment(CEEWireAttachments.BANNER.get());
        attachment.data.putString("BaseColor", this.getColor().getName());
        BannerPatternLayers pattern = stack.get(DataComponents.BANNER_PATTERNS);
        if (pattern != null)
            attachment.data.put("Pattern", BannerPatternLayers.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), pattern).getOrThrow());

        attachToWire(point, level, player, stack, attachment);
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return attachmentColor();
    }
}
