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
//        extensionState = 1;

        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.Z)
            yRot += 180;

        if (state.getValue(PantographBlock.DOUBLE)) {
            float rotationFactor = 27;
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARMS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .rotateXDegrees(-90 + extensionState * rotationFactor)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            double armHingePosY = Math.cos((-90 + extensionState * rotationFactor) * Math.PI / 180) * 1.5;
            double armHingePosX = Math.sin((-90 + extensionState * rotationFactor) * Math.PI / 180) * 1.5;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARMS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(83 - extensionState * rotationFactor * 0.8f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 1)
                    .translate(0, Math.cos((-75 + extensionState * 30) * Math.PI / 180) * 1.5 + Math.cos((89 - extensionState * 30) * Math.PI / 180) * 1.5, 0)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.6, 0.6)
                    .translate(0, armHingePosY * 0.2f, armHingePosX * 0.3f)
                    .scaleZ((float) (1.0 - (0.7f + armHingePosX * 0.3f)) * 1.4f)
                    .rotateXDegrees(0)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        } else {
            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .rotateXDegrees(-75 + extensionState * 30)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            double armHingePosY = Math.cos((-75 + extensionState * 30) * Math.PI / 180) * 1.5;
            double armHingePosX = Math.sin((-75 + extensionState * 30) * Math.PI / 180) * 1.5;

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 30)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(9/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 30)
                    .rotateYDegrees(12.5f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(7/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 30)
                    .rotateYDegrees(-12.5f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .translate(0, Math.cos((89 - extensionState * 30) * Math.PI / 180) * 1.5, Math.sin((89 - extensionState * 30) * Math.PI / 180) * 1.5)
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
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        }
    }
}
