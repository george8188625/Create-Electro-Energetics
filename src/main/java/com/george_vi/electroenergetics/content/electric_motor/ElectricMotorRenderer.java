package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricMotorRenderer extends KineticBlockEntityRenderer<ElectricMotorBlockEntity> {
    public ElectricMotorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ElectricMotorBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(CEEPartialModels.ELECTRIC_MOTOR_SHAFT, state);
    }
}
