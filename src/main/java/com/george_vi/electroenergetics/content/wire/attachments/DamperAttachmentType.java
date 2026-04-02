package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DamperAttachmentType extends WireAttachmentType {
    @Override
    public float getWidth(WireAttachment attachment) {
        return 1f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 0.5f;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        CachedBuffers.partial(CEEPartialModels.WIRE_DAMPER_ATTACHMENT, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .rotateZDegrees(-pitch + 180)
                .renderInto(pose, buffer.getBuffer(RenderType.SOLID));
    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        Item item = CEETags.itemFromTag(CEETags.WIRE_DAMPER_ITEM);
        return List.of(item.getDefaultInstance());
    }

    @Override
    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        Item item = CEETags.itemFromTag(CEETags.WIRE_DAMPER_ITEM);
        return List.of(item.getDefaultInstance());
    }
}
