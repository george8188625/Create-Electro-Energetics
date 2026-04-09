package com.george_vi.electroenergetics.content.synchroscope;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class SynchroscopeRenderer extends SmartBlockEntityRenderer<SynchroscopeBlockEntity> {
    public SynchroscopeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SynchroscopeBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
//        float r = AngleHelper.angleLerp(Mth.clamp((blockEntity.counter + partialTicks - 1) / blockEntity.tickLength, 0, 1.1f), blockEntity.prevPhaseOffset, blockEntity.phaseOffset);
        float r = blockEntity.smoothPhase.getValue(partialTicks);
//        float r = AngleHelper.angleLerp(partialTicks, blockEntity.prevPhaseOffset, blockEntity.phaseOffset);
        CachedBuffers.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
                .light(light)
                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(SynchroscopeBlock.FACING).toYRot())
                .translate(0.5f, 0.5f, 7.5f/16f)
                .rotateZDegrees(r)

                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

    }
}
