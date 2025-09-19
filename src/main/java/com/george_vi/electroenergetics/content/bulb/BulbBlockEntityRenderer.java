package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BulbBlockEntityRenderer extends SmartBlockEntityRenderer<BulbBlockEntity> {
    public BulbBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BulbBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        if (CEEBlocks.BULB.has(state))
            CachedBuffers.partial(CEEPartialModels.BULB_FILAMENT, state)
                    .light(blockEntity.light > 0.05 ? LightTexture.FULL_BRIGHT : light)
                    .color((int) (blockEntity.light * 233) + 22, (int) (blockEntity.light * 233) + 22, (int) (blockEntity.light * 233) + 22, 255)
                    .center()
                    .rotateYDegrees(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                    .rotateXDegrees(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(state.getValue(BulbBlock.FACING).getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees(state.getValue(BulbBlock.ROLL) ? 90 : 0)
                    .uncenter()
                    .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));
        else
            CachedBuffers.partial(CEEPartialModels.BULB_BROKEN_FILAMENT, state)
                    .light(light)
                    .color(22, 22, 22, 255)
                    .center()
                    .rotateYDegrees(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                    .rotateXDegrees(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(state.getValue(BulbBlock.FACING).getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees(state.getValue(BulbBlock.ROLL) ? 90 : 0)
                    .uncenter()
                    .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));
    }
}
