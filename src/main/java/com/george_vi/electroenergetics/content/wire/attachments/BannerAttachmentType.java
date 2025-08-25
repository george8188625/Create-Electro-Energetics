package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class BannerAttachmentType extends WireAttachmentType {
    @Override
    public float getWidth(WireAttachment attachment) {
        return 1f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 2f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, LevelRenderer levelRenderer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        Frustum frustum = levelRenderer.getFrustum();
        if (!frustum.isVisible(AABB.ofSize(pos, 1.2f, 4, 1.2f).setMaxY(pos.y())))
            return;

        BannerPatternLayers pattern = null;
        if (attachment.data.contains("Pattern"))
            pattern = BannerPatternLayers.CODEC
                    .parse(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), attachment.data.get("Pattern"))
                    .resultOrPartial(s -> LogUtils.getLogger().error("Failed to parse banner patterns: '{}'", s))
                    .orElse(null);

        if (pattern == null)
            pattern = BannerPatternLayers.EMPTY;
        ModelPart flag = mc.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
        DyeColor color = DyeColor.byName(attachment.data.getString("BaseColor"), DyeColor.WHITE);

        PoseTransformStack msr = TransformStack.of(pose);
        msr.pushPose();
        msr.rotateXDegrees(180);
        msr.rotateYDegrees(90.1f);
        renderChain(pose, buffer, light);
        msr.popPose();
        msr.scale(0.66f);

        msr.translate(0, 0.375, 0.07);
        BannerRenderer.renderPatterns(pose, buffer, light, OverlayTexture.NO_OVERLAY, flag, ModelBakery.BANNER_BASE, true, color, pattern);
        msr.translate(0, 0, -0.07);
        msr.rotateYDegrees(180);
        msr.translate(0, 0, 0.07);
        BannerRenderer.renderPatterns(pose, buffer, light, OverlayTexture.NO_OVERLAY, flag, ModelBakery.BANNER_BASE, true, color, pattern);

    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        BannerPatternLayers pattern = null;
        if (attachment.data.contains("Pattern"))
            pattern = BannerPatternLayers.CODEC
                    .parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), attachment.data.get("Pattern"))
                    .resultOrPartial(s -> LogUtils.getLogger().error("Failed to parse banner patterns: '{}'", s))
                    .orElse(null);
        if (pattern == null)
            pattern = BannerPatternLayers.EMPTY;

        DyeColor color = DyeColor.byName(attachment.data.getString("BaseColor"), DyeColor.WHITE);
        ItemStack stack = BannerBlock.byColor(color).asItem().getDefaultInstance();
        stack.set(DataComponents.BANNER_PATTERNS, pattern);
        return List.of(stack);
    }
}
