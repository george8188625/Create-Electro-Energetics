package com.george_vi.electroenergetics.content.railway_electrification.third_rail;

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

public class RailContactShoeRenderer extends SmartBlockEntityRenderer<RailContactShoeBlockEntity> {
    public RailContactShoeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RailContactShoeBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
        BlockState state = blockEntity.getBlockState();

        float distanceY = Mth.lerp(partialTicks, blockEntity.prevDistanceY, blockEntity.distanceY);
        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            yRot += 180;

        CachedBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_CONTACT, state)
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0.5f, -distanceY, 0.5f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        CachedBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_HINGES, state)
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0.5f, -distanceY, 0.5f)
                .rotateX((float) -Mth.atan2(-distanceY - 0.25f, 0.5f) + Mth.PI)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
    }
}
