package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public abstract class WireAttachmentType {
    public abstract float getWidth(WireAttachment attachment);

    public abstract float getHeight(WireAttachment attachment);

    @OnlyIn(Dist.CLIENT)
    public abstract void render(PoseStack pose, MultiBufferSource buffer, LevelRenderer levelRenderer, WireAttachment attachment, Vec3 pos, int light, float pitch);

    @OnlyIn(Dist.CLIENT)
    public void renderChain(PoseStack pose, MultiBufferSource buffer, int light) {
        CachedBuffers.partial(CEEPartialModels.ATTACHMENT_CHAIN, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderType.CUTOUT));
    }

    public abstract List<ItemStack> getDrops(WireAttachment attachment, Level level);

    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        return List.of(ItemStack.EMPTY);
    }
}
