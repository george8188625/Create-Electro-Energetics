package com.george_vi.electroenergetics.content.variac;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class VariacRenderer extends KineticBlockEntityRenderer<VariacBlockEntity> {

    public VariacRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(VariacBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        BlockState state = getRenderedBlockState(be);
        RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        int rot = 0;

        Direction facing = state.getValue(VariacBlock.HORIZONTAL_FACING);

        if (facing == Direction.NORTH)
            rot = -135;
        else if (facing == Direction.SOUTH)
            rot = 45;
        else if (facing == Direction.EAST)
            rot = 135;
        else if (facing == Direction.WEST)
            rot = -45;

        CachedBuffers.partial(CEEPartialModels.VARIAC_DIAL, state)
                .center()
                .rotateYDegrees(be.smoothProgress.getValue(partialTicks) * 270 + rot)
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(VariacBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(CEEPartialModels.VARIAC_SHAFT, state, Direction.WEST);
    }
}
