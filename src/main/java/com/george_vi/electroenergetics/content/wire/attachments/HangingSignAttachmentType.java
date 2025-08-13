package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HangingSignAttachmentType extends WireAttachmentType {
    final Supplier<Block> originalBlock;
    public static Map<WoodType, HangingSignRenderer.HangingSignModel> models;

    public HangingSignAttachmentType(Supplier<Block> originalBlock1) {
        this.originalBlock = originalBlock1;
    }

    @Override
    public float getWidth(WireAttachment attachment) {
        return 1f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 1f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, LevelRenderer levelRenderer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        Minecraft mc = Minecraft.getInstance();

    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        return null;
    }
}
