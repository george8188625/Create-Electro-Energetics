package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class IndicatorBulbRenderer extends SmartBlockEntityRenderer<IndicatorBulbBlockEntity> {
    public IndicatorBulbRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(IndicatorBulbBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        int side = state.getValue(IndicatorBulbBlock.SIDE);
        TransformStack<PoseTransformStack> msr = TransformStack.of(ms);

        for (boolean opposite : Iterate.falseAndTrue) {
            if (((side + 1) & (opposite ? 2 : 1)) == 0)
                continue;
            float lightStrength = opposite ? blockEntity.secondLight : blockEntity.firstLight;
            DyeColor color = opposite ? blockEntity.secondColor : blockEntity.firstColor;
            ms.pushPose();
            msr.center()
                    .rotateYDegrees(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                    .rotateXDegrees(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(state.getValue(BulbBlock.FACING).getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees((state.getValue(BulbBlock.ROLL) ? 90 : 0) + (opposite ^ state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 180 : 0))
                    .uncenter();

            CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_TUBE, state)
                    .color(color.getTextColor())
                    .light(light)
                    .disableDiffuse()
                    .renderInto(ms, buffer.getBuffer(lightStrength > 0.05f ? RenderTypes.additive() : RenderType.translucent()));


            if (lightStrength > 0.05f) {
                float factor = lightStrength;
                int newColor = ((int)(((color.getMapColor().col >> 16) & 0xFF) * factor) << 16)
                        | ((int)(((color.getMapColor().col >> 8) & 0xFF) * factor) << 8)
                        | ((int)((color.getMapColor().col & 0xFF) * factor));

                CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, state)
                        .color(newColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f, 5/16f, 8/16f)
                        .scale(2, 5, 2)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

                int cubeColor = ((int)(((0xffffff >> 16) & 0xFF) * factor) << 16)
                        | ((int)(((0xffffff >> 8) & 0xFF) * factor) << 8)
                        | ((int)((0xffffff & 0xFF) * factor));

                CachedBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, state)
                        .color(cubeColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f, 5/16f, 8/16f)
                        .scale(1f, 4.3f, 1f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
            }

            ms.popPose();
        }
    }
}
