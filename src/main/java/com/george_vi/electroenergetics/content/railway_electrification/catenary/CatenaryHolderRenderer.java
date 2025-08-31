package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class CatenaryHolderRenderer extends SmartBlockEntityRenderer<CatenaryHolderBlockEntity> {
    public CatenaryHolderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CatenaryHolderBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockPos attachedTo = blockEntity.getAttachedTo();
        if (blockEntity.poleWidth == 0 || attachedTo == null)
            return;

        SuperByteBuffer grip;
        if (blockEntity.poleWidth == 4)
            grip = CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_MOUNT_4, blockEntity.getBlockState());
        else if (blockEntity.poleWidth == 6)
            grip = CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_MOUNT_6, blockEntity.getBlockState());
        else if (blockEntity.poleWidth == 8)
            grip = CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_MOUNT_8, blockEntity.getBlockState());
        else
            grip = CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_MOUNT_10, blockEntity.getBlockState());

        grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 12/16f, 0.5)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        Vec3 difference = Vec3.atLowerCornerOf(attachedTo).subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
        Vec3 normalizedDifference = difference.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(difference.x, difference.z)) + 180;

        CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_INSULATOR, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 1, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 0, 1/16f + blockEntity.poleWidth / 32f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        float shortLength = 17 / 16f;
        float longLength = 28 / 16f;

        float horizontalDistance = (float) difference.horizontalDistance() - 9 / 16f - blockEntity.poleWidth / 32f;
        boolean isLong = horizontalDistance > shortLength;
        float size = horizontalDistance / (isLong ? longLength : shortLength);
        CachedBuffers.partial(isLong ? CEEPartialModels.CATENARY_HOLDER_LONG_ROD : CEEPartialModels.CATENARY_HOLDER_SHORT_ROD, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 1, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 0, 9 / 16f + blockEntity.poleWidth / 32f)
                .scaleZ(size)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        if (horizontalDistance > 1.5) {
            horizontalDistance -= 0.5f;
            isLong = horizontalDistance > shortLength;
            size = horizontalDistance / (isLong ? longLength : shortLength);
            float distance = (float) difference.add(0, 0.7, 0).length() - 1.4f;
            float lowerSize = distance / (isLong ? longLength : shortLength);

            CachedBuffers.partial(isLong ? CEEPartialModels.CATENARY_HOLDER_LONG_ROD : CEEPartialModels.CATENARY_HOLDER_SHORT_ROD, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, -2 / 16f, 0.5)
                    .rotateYDegrees(yaw)
//                    .translate(0, 0, -5/16f + blockEntity.poleWidth / 32f)
                    .rotateXDegrees(-27)
                    .translate(0, 0, 1.5f)
                    .rotateXDegrees(((1 / horizontalDistance) / 0.35f) * 8 + 27)
                    .scaleZ(lowerSize)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_INSULATOR, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, -2 / 16f, 0.5)
                    .rotateYDegrees(yaw)
                    .rotateXDegrees(-27)
                    .translate(0, 0, 1 / 16f + blockEntity.poleWidth / 32f)
                    .scale(0.95f, 0.95f, 1f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_LONG_ROD, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, -2 / 16f, 0.5)
                    .rotateYDegrees(yaw)
                    .rotateXDegrees(-27)
                    .translate(0, 0, 9 / 16f + blockEntity.poleWidth / 32f)
                    .scale(0.95f, 0.95f, 0.88f + (0.5f / (blockEntity.poleWidth + 0.01f)))
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, -4/16f, 0.5)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        } else {
            CachedBuffers.partial(isLong ? CEEPartialModels.CATENARY_HOLDER_LONG_ROD : CEEPartialModels.CATENARY_HOLDER_SHORT_ROD, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, 2 / 16f, 0.5)
                    .rotateYDegrees(yaw)
                    .translate(0, 0, 9 / 16f + blockEntity.poleWidth / 32f)
                    .scaleZ(size)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_INSULATOR, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, 2 / 16f, 0.5)
                    .rotateYDegrees(yaw)
                    .translate(0, 0, 1/16f + blockEntity.poleWidth / 32f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, 0, 0.5)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }

    }
}
