package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEETags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "renderItem", at=@At("HEAD"), remap = false, cancellable = true)
    public void electroEnergetics$renderItem(LivingEntity entity, ItemStack itemStack,
                                             ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack,
                                             MultiBufferSource buffer, int seed, CallbackInfo ci) {

        if (entity instanceof Player player)
            if (player.getMainHandItem().getItem() == CEEItems.LINEMANS_STICK.get() &&
                    (player.getOffhandItem().is(CEETags.HIDE_ON_LINEMANS_STICK)))
                ci.cancel();
    }
}
