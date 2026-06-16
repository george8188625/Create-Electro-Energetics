package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ElectricalPanelRenderer extends SmartBlockEntityRenderer<ElectricalPanelBlockEntity> {
    public ElectricalPanelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ElectricalPanelBlockEntity blockEntity, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        PanelAttachment[] attachments = blockEntity.getAttachments();
        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;
            ms.pushPose();
            attachment.render(blockEntity, partialTicks, ms, buffer, light, overlay);
            ms.popPose();
            if (attachment.label != null) {
                ms.pushPose();
                attachment.renderLabel(blockEntity, partialTicks, ms, buffer, light, overlay);
                ms.popPose();
            }
        }


        float coverAlpha = Mth.lerp(partialTicks, ElectricalPanelClientTicker.prevCoverAlpha, ElectricalPanelClientTicker.coverAlpha);

        if (coverAlpha != 0) {
            PartialModel panelCover = CEEPartialModels.PANEL_COVER;
            if (blockEntity.getBlockState().getBlock() instanceof ElectricalPanelBlock b && b.color != null)
                panelCover = CEEPartialModels.DYED_ELECTRICAL_PANEL_COVERS[b.color.ordinal()];

            CachedBuffers.partial(panelCover, blockEntity.getBlockState())
                    .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(ElectricalPanelBlock.FACING).toYRot() + 180)
                    .color(255, 255, 255, (int) (coverAlpha * 255))
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(coverAlpha == 1 ? RenderType.solid() : RenderType.translucent()));
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
