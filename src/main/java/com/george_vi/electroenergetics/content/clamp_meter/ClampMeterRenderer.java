package com.george_vi.electroenergetics.content.clamp_meter;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickClientHandler;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClampMeterRenderer extends CustomRenderedItemModelRenderer {
    protected static final PartialModel METER = PartialModel.of(CreateElectroEnergetics.rl("item/clamp_meter"));

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if (((transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND && player.getMainArm() == HumanoidArm.LEFT) ||
                (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && player.getMainArm() == HumanoidArm.RIGHT)) &&
                        player.getUseItem() == stack)
            return;

        renderer.render(model.getOriginalModel(), light);
    }


    @OnlyIn(Dist.CLIENT)
    public static void renderFirstPerson(ClientLevel level, PoseStack pose, MultiBufferSource buffer, Camera camera) {

        Minecraft mc = Minecraft.getInstance();
        if (camera.isDetached() || mc.player == null)
            return;

        LocalPlayer player = mc.player;

        ItemStack mainStackItem = player.getMainHandItem();
        if (mainStackItem.getItem() != CEEItems.CLAMP_METER.get() ||
                player.getUseItem() != mainStackItem ||
                LinemansStickClientHandler.ticksSinceChange <= 0)
            return;

        HumanoidArm mainArm = player.getMainArm();

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
        CachedBuffers.partial(METER, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, poleLength - 0.75f, -0.5)
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));

        pose.popPose();
    }
}
