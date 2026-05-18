package com.george_vi.electroenergetics.content.frequency_meter;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FrequencyMeterRenderer extends SmartBlockEntityRenderer<FrequencyMeterBlockEntity> {
    public FrequencyMeterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FrequencyMeterBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        float r = blockEntity.smoothFrequency.getValue(partialTicks);

        r *= -6.75f ;

        CachedBuffers.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
                .light(light)
                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(FrequencyMeterBlock.FACING).toYRot())
                .translate(0.5f, 0.5f, 7.5f/16f)
                .rotateZDegrees(r + 135)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

    }
}
