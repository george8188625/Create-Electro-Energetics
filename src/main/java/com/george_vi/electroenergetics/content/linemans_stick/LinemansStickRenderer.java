package com.george_vi.electroenergetics.content.linemans_stick;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class LinemansStickRenderer extends CustomRenderedItemModelRenderer {

    protected static final PartialModel HEAD = PartialModel.of(CreateElectroEnergetics.rl("item/linemans_stick_head"));
    protected static final PartialModel BASE = PartialModel.of(CreateElectroEnergetics.rl("item/linemans_stick"));
    protected static final PartialModel TELESCOPE = PartialModel.of(CreateElectroEnergetics.rl("item/linemans_stick_telescope"));

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        float extensionState = 0;

        if (transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ||
                transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            extensionState = 0.25f;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if ((transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND && player.getMainArm() == HumanoidArm.LEFT) ||
                (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && player.getMainArm() == HumanoidArm.RIGHT))
            return;


        renderer.render(model.getOriginalModel(), light);

        ms.translate(0, extensionState * 1.7, 0);
        renderer.render(TELESCOPE.get(), light);

        ms.translate(0, extensionState * 1.7, 0);
        renderer.render(HEAD.get(), light);
    }

    public static void setupAnimAfter(AbstractClientPlayer player, HumanoidModel<?> humanoidModel) {
        ItemStack stackInHand = player.getMainHandItem();
        if (stackInHand.getItem() != CEEItems.LINEMANS_STICK.get())
            return;
        HumanoidArm mainArm = player.getMainArm();
        ModelPart armModel = mainArm == HumanoidArm.RIGHT ? humanoidModel.rightArm : humanoidModel.leftArm;

        armModel.resetPose();
        armModel.xRot = humanoidModel.head.xRot - 0.23f;
        armModel.yRot = humanoidModel.head.yRot - 0.08f;
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderSticks(LevelRenderer levelRenderer, ClientLevel level, PoseStack pose,
                                    MultiBufferSource buffer, Camera camera) {

        Minecraft mc = Minecraft.getInstance();
        if (camera.isDetached() || mc.player == null)
            return;
        ItemRenderer itemRenderer = mc.getItemRenderer();

        LocalPlayer player = mc.player;

        ItemStack mainStackItem = player.getMainHandItem();
        if (mainStackItem.getItem() != CEEItems.LINEMANS_STICK.get())
            return;

        HumanoidArm mainArm = player.getMainArm();

        ItemStack offHandItem = player.getOffhandItem();

        Vec3 playerPos = player.getPosition(AnimationTickHolder.getPartialTicks());

        Vec3 offset = new Vec3(mainArm == HumanoidArm.RIGHT ? -0.3 : 0.3, 0.6, 0);
        offset = VecHelper.rotate(offset, -player.getViewYRot(AnimationTickHolder.getPartialTicks()), Direction.Axis.Y);
        Vec3 base = playerPos.add(offset);

        pose.pushPose();
        pose.translate(
                -camera.getPosition().x() + base.x(),
                -camera.getPosition().y() + base.y(),
                -camera.getPosition().z() + base.z());

        Vec3 hitPos = VecHelper.lerp(AnimationTickHolder.getPartialTicks(),
                LinemansStickClientHandler.prevLinemansStickTarget,
                LinemansStickClientHandler.linemansStickTarget);

        Vec3 diff = hitPos.subtract(base);

        int light = LevelRenderer.getLightColor(level, player.blockPosition());

        double poleLength = diff.length();
        double hypot = Math.hypot(diff.x(), diff.z());
        CachedBuffers.partial(HEAD, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, poleLength - 1.3f, -0.5)
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));

        CachedBuffers.partial(TELESCOPE, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, Mth.clamp((poleLength - 1.3f) / 2, poleLength - 3f, poleLength - 1.3f), -0.5)
                .renderInto(pose, buffer.getBuffer(RenderType.solid()));

        CachedBuffers.partial(BASE, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, Mth.clamp((poleLength - 1.3f) / 4, poleLength - 4.6f, poleLength - 1.3f), -0.5)
                .renderInto(pose, buffer.getBuffer(RenderType.solid()));

        if (offHandItem.is(CEETags.HIDE_ON_LINEMANS_STICK)) {
            BakedModel bakedmodel = itemRenderer.getModel(offHandItem, level, player, 0);
            pose.pushPose();

            PoseTransformStack msr = TransformStack.of(pose)
                    .translate(diff);

            if (WireInteractionHandler.targetedPoint == null ||
                    offHandItem.getItem() instanceof ClampMeterItem)
                msr.rotateY(Mth.PI + (float) Mth.atan2(diff.x(), diff.z()));
            else {
                Vec3 pos1 = WireInteractionHandler.targetedPoint.node1().getPosition(level);
                Vec3 pos2 = WireInteractionHandler.targetedPoint.node2().getPosition(level);

                msr.rotateY(Mth.HALF_PI + (float) Mth.atan2(pos1.x() - pos2.x(), pos1.z() - pos2.z()));
            }

            if (offHandItem.is(CEETags.WIRE_DAMPER_ITEM)) {
                bakedmodel = CEEPartialModels.WIRE_DAMPER_ATTACHMENT.get();
                msr.translate(0.5, 0.5, 0.5);
            } else if (offHandItem.getItem() instanceof BannerItem) {
                msr.translate(0, -1.5f, 0);
            } else if (offHandItem.getItem() instanceof ClampMeterItem) {
                msr
                        .rotateYDegrees(179) // 179 to prevent Z-fighting
                        .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                        .translate(0, -4/16f, 0);

            }

            itemRenderer.render(offHandItem, ItemDisplayContext.NONE, false, pose, buffer, light, OverlayTexture.NO_OVERLAY, bakedmodel);
            pose.popPose();
        }

        pose.popPose();
    }
}
