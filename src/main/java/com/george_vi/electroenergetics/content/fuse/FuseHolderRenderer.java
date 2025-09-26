package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.foundation.DirectionalRolledDeviceBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class FuseHolderRenderer extends SmartBlockEntityRenderer<FuseHolderBlockEntity> {
    public FuseHolderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FuseHolderBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Pair<FuseHoldable, CompoundTag> firstFuse = blockEntity.firstFuse;
        Pair<FuseHoldable, CompoundTag> secondFuse = blockEntity.secondFuse;

        boolean roll = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.ROLL);
        Direction facing = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.FACING);

        if (firstFuse != null) {
            ms.pushPose();
            TransformStack.of(ms)
                    .center()
                    .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                    .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees(roll ? 90 : 0)
                    .uncenter()
                    .translate(5.5/16f, 0, 0);
            firstFuse.getFirst().render(firstFuse.getSecond(), ms, buffer, light);
            ms.popPose();
        }

        if (secondFuse != null) {
            ms.pushPose();
            TransformStack.of(ms)
                    .center()
                    .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                    .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees(roll ? 90 : 0)
                    .uncenter()
                    .translate(10.5/16f, 0, 0);
            secondFuse.getFirst().render(secondFuse.getSecond(), ms, buffer, light);
            ms.popPose();
        }

    }
}
