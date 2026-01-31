package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographRenderer extends SmartBlockEntityRenderer<PantographBlockEntity> {
    public PantographRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PantographBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        float extensionState = Mth.lerp(partialTicks, blockEntity.prevExtensionState, blockEntity.currentExtensionState);

        int color = blockEntity.color.getTextureDiffuseColor();

        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.Z)
            yRot += 180;

        if (state.getValue(PantographBlock.DOUBLE)) {
            float rotationFactor = 27;
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_BASE_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARMS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .rotateXDegrees(-90 + extensionState * rotationFactor)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            float lowerArmRadians = (-90 + extensionState * rotationFactor) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;


            float b = (float) (-0.4f - Math.abs(armHingePosX));
            float a = Mth.sqrt(-b * b + 2f * 2f);


            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARMS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateX((float) Mth.atan2(a, b) - Mth.HALF_PI)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.5625 + a + armHingePosY, 1)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            float springHingePosY = Mth.cos(lowerArmRadians + 0.5f) * 0.425f;
            float springHingePosX = Mth.sin(lowerArmRadians + 0.5f) * 0.425f;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, springHingePosY + 0.375, springHingePosX + 0.5f)
                    .scaleZ((-springHingePosX + 0.5f) * (16/13f))
                    .rotateXDegrees(0)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        } else {
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_BASE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .rotateXDegrees(-75 + extensionState * 30)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
            float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

            double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(12/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .rotateYDegrees(12.5f)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(4/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .rotateYDegrees(-12.5f)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .translate(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.25)
                    .rotateXDegrees(-22 + extensionState * -20)
                    .scale(1, 1, 1 + extensionState / 2)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_ROD, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.1875)
                    .rotateXDegrees(-77 + extensionState * 43)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        }
    }
}
