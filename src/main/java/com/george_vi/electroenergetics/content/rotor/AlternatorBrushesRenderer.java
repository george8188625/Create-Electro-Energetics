package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class AlternatorBrushesRenderer extends KineticBlockEntityRenderer<AlternatorBrushesBlockEntity> {
    public AlternatorBrushesRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(AlternatorBrushesBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(CEEPartialModels.ALTERNATOR_BRUSHES_SHAFT, state);
    }
}
