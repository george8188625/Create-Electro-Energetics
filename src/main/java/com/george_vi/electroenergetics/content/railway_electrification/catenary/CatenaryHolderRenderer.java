package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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

        attachedTo = attachedTo.offset(blockEntity.getBlockPos());

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
                .translate(0.5, 20/16f, 0.5)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        Vec3 difference = Vec3.atLowerCornerOf(attachedTo).subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
        Vec3 normalizedDifference = difference.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(difference.x, difference.z)) + 180;

        CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_INSULATOR, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 24/16f, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 0, 1/16f + blockEntity.poleWidth / 32f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        float shortLength = 17 / 16f;
        float longLength = 28 / 16f;

        float horizontalDistance = (float) difference.horizontalDistance();
        boolean isLong = horizontalDistance > shortLength;
        float size = horizontalDistance / (isLong ? longLength : shortLength);
        CachedBuffers.partial(isLong ? CEEPartialModels.CATENARY_HOLDER_LONG_ROD : CEEPartialModels.CATENARY_HOLDER_SHORT_ROD, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 24/16f, 0.5)
                .rotateYDegrees(yaw)
                .scale(0.5f, 0.5f, size)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        float angle = -(float) Math.atan2(2, horizontalDistance);
        // Thick rod
        CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_LONG_ROD, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .rotateX(angle)
                .scaleZ(Mth.sqrt(4f + horizontalDistance * horizontalDistance) / longLength)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        // Bottom insulator
        CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_INSULATOR, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .rotateX(angle)
                .translate(0, 0, 0.25f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        // Holding?? rod
        CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_LONG_ROD, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 15/16f, horizontalDistance / 2)
                .scale(0.5f, 0.5f, (horizontalDistance + 1) / (2 * longLength))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        // Connector
        CachedBuffers.partial(CEEPartialModels.CATENARY_HOLDER_CONNECTOR, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 10/16f, horizontalDistance)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -10/16f, 0.5)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
