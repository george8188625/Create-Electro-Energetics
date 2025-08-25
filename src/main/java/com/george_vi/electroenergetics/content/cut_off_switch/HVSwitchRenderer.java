package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class HVSwitchRenderer extends SmartBlockEntityRenderer<HVSwitchBlockEntity> {
    public HVSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HVSwitchBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        float progress = Mth.lerp(partialTicks, blockEntity.prevProgress, blockEntity.progress);

        CachedBuffers.partial(CEEPartialModels.HV_SWITCH_ARM, blockEntity.getBlockState())
                .translate(0.5, 0.8, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                                (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(-1/32f, 0, 0)
                .rotateXDegrees(Mth.lerp(progress, -90, 0))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));

        CachedBuffers.partial(CEEPartialModels.HV_SWITCH_PIVOT, blockEntity.getBlockState())
                .translate(0.5, 0.8, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                        (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(0, Mth.sin(Mth.lerp(progress, Mth.HALF_PI, 0)), Mth.cos(Mth.lerp(progress, Mth.HALF_PI, 0)))
                .rotateXDegrees(Mth.lerp(progress, 120, 0))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));

        CachedBuffers.partial(CEEPartialModels.HV_SWITCH_ARM, blockEntity.getBlockState())
                .translate(0.5, 0.8, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                        (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(-1/32f, Mth.sin(Mth.lerp(progress, Mth.HALF_PI, 0)), Mth.cos(Mth.lerp(progress, Mth.HALF_PI, 0)))
                .rotateXDegrees(Mth.lerp(progress, 70, 0))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));

        BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().relative(blockEntity.getBlockState().getValue(HVSwitchBlock.FACING), 2));
        if (!CEEBlocks.CONNECTOR.has(state) ||
                state.getValue(ConnectorBlock.STYLE) != ConnectorBlock.Style.LONG)
            return;

        CachedBuffers.partial(CEEPartialModels.HV_SWITCH_PIVOT, blockEntity.getBlockState())
                .translate(0.5, 14/16f, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                        (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(0, 0, 2)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));

    }
}
