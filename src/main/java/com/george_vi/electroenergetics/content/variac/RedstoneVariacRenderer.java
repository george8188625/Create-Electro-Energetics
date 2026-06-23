package com.george_vi.electroenergetics.content.variac;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class RedstoneVariacRenderer extends SafeBlockEntityRenderer<RedstoneVariacBlockEntity> {

    public RedstoneVariacRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    protected void renderSafe(RedstoneVariacBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        int rot = 0;

        Direction facing = be.getBlockState().getValue(VariacBlock.HORIZONTAL_FACING);

        if (facing == Direction.NORTH)
            rot = -135;
        else if (facing == Direction.SOUTH)
            rot = 45;
        else if (facing == Direction.EAST)
            rot = 135;
        else if (facing == Direction.WEST)
            rot = -45;

        float progress = be.smoothProgress.getValue(partialTicks);
        CachedBuffers.partial(CEEPartialModels.VARIAC_DIAL, be.getBlockState())
                .center()
                .rotateYDegrees(progress * 270 + rot)
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        if (progress < 0.01)
            return;

        CachedBuffers.partial(CEEPartialModels.REDSTONE_VARIAC_REDSTONE, be.getBlockState())
                .center()
                .rotateYDegrees(-facing.toYRot() + 180)
                .uncenter()
                .light(light)
                .color(Color.mixColors(0x650101, 0xCD0000, progress))
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
