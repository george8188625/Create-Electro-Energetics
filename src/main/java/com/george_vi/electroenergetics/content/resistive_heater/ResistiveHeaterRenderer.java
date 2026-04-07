package com.george_vi.electroenergetics.content.resistive_heater;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ResistiveHeaterRenderer extends SmartBlockEntityRenderer<ResistiveHeaterBlockEntity> {
    public ResistiveHeaterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ResistiveHeaterBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        float t = blockEntity.smoothHeat.getValue(partialTicks);


        SuperByteBuffer heatingElement = CachedBuffers.partial(CEEPartialModels.RESISTIVE_HEATER_HEATING_ELEMENT, blockEntity.getBlockState());
        heatingElement
                .rotateYCenteredDegrees(blockEntity.getBlockState().getValue(ResistiveHeaterBlock.FACING).toYRot())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        if (t > 0.2) {
            int alpha = (int) Mth.clamp(((t - 0.2f)) * 255, 0, 127);
            t /= 3;
            CachedBuffers.partial(CEEPartialModels.RESISTIVE_HEATER_HEATING_ELEMENT_GLOW, blockEntity.getBlockState())
                    .rotateYCenteredDegrees(blockEntity.getBlockState().getValue(ResistiveHeaterBlock.FACING).toYRot())
                    .color(255, (int) (t * 255), (int) (t * 127), alpha)
                    .disableDiffuse()
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.translucent()));
        }
    }
}
