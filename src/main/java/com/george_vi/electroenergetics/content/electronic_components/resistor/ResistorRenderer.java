package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import java.util.Map;

public class ResistorRenderer extends SmartBlockEntityRenderer<ResistorBlockEntity> {
    public ResistorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ResistorBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        double resistance = blockEntity.indexToResistance(blockEntity.resistance.value);
        if (resistance > 1000000 || resistance < 0.1)
            return;

        int[] digitColor = {
                0x101010, // black
                0x8B4513, // brown
                0xEE1010, // red
                0xFF7F00, // orange
                0xFFFF00, // yellow
                0x00FF00, // green
                0x3030EE, // blue
                0x8B00FF, // violet
                0x808080, // grey
                0xFFFFFF  // white
        };

        Map<Integer, Integer> multiplierColor = Map.of(
               -2, 0xC0C0C0, // silver
               -1, 0xD6CB84, // gold
               0, 0x101010, // black
               1, 0x8B4513, // brown
               2, 0xEE1010, // red
               3, 0xFF7F00, // orange
               4, 0xFFFF00, // yellow
               5, 0x00FF00, // green
               6, 0x3030EE  // blue
        );

        int exponent = 0;
        while (resistance >= 100) {
            resistance = resistance / 10;
            exponent++;
        }
        while (resistance < 10) {
            resistance = resistance * 10;
            exponent--;
        }

        int digits = (int) Math.round(resistance);
        int d1 = (int) Math.floor(digits / 10d);
        int d2 = digits % 10;

        boolean roll = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.ROLL);
        Direction facing = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.FACING);

        CachedBuffers.partial(CEEPartialModels.RESISTOR_STRIP, blockEntity.getBlockState())
                .center()
                .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(roll ? 90 : 0)
                .uncenter()
                .color(digitColor[d1])
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        CachedBuffers.partial(CEEPartialModels.RESISTOR_STRIP, blockEntity.getBlockState())
                .center()
                .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(roll ? 90 : 0)
                .uncenter()
                .translate(-2/16f, 0, 0)
                .color(digitColor[d2])
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        CachedBuffers.partial(CEEPartialModels.RESISTOR_STRIP, blockEntity.getBlockState())
                .center()
                .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(roll ? 90 : 0)
                .uncenter()
                .color(multiplierColor.get(exponent))
                .translate(-4/16f, 0, 0)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
