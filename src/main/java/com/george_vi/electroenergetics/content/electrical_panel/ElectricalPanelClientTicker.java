package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

public class ElectricalPanelClientTicker {

    public static float prevCoverAlpha;
    public static float coverAlpha;

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ItemStack heldItemStack = mc.player.getMainHandItem();

        // Cover See Through:
        float coverAlphaTarget = heldItemStack.is(CEETags.ELECTRICAL_PANEL_SEE_THROUGH) ? 0 : 1;

        if (!Float.isFinite(coverAlpha))
            coverAlpha = 1;

        prevCoverAlpha = coverAlpha;
        coverAlpha = LerpedFloat.Chaser.LINEAR.chase(coverAlpha, 0.3, coverAlphaTarget);

        if (!heldItemStack.is(CEETags.ELECTRICAL_PANEL_ATTACHMENT))
            return;

        Level level = mc.level;
        BlockPos targetPos = result.getBlockPos();
        BlockState targetState = level.getBlockState(targetPos);

        if (!CEEBlocks.ELECTRICAL_PANEL.has(targetState) ||
                result.getDirection() != targetState.getValue(ElectricalPanelBlock.FACING) ||
                !(level.getBlockEntity(targetPos) instanceof ElectricalPanelBlockEntity be))
            return;

        Direction facing = targetState.getValue(ElectricalPanelBlock.FACING);

        PanelAttachmentType attachmentType = PanelAttachmentType.getForItem(heldItemStack);
        if (attachmentType == null)
            return;

        ElectricalPanelLayoutType layout = be.getLayoutType();

        PanelAttachmentMode mode = attachmentType.mode;

        Vec3 localClickPos = result.getLocation().subtract(Vec3.atLowerCornerOf(targetPos));
        ElectricalPanelSlot slot = null;
        if (be.getAttachments().length == 0)
            slot = mode.getSlot(facing, localClickPos);
        if (be.getAttachments().length == 1 && be.getAttachments()[0] != null) {
            slot = null;
        } else if (layout == ElectricalPanelLayoutType.HALF_HORIZONTAL) {
            slot = PanelAttachmentMode.HALF_ONLY_HORIZONTAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.HALF_VERTICAL) {
            slot = PanelAttachmentMode.HALF_ONLY_VERTICAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.THIRD) {
            slot = PanelAttachmentMode.THIRD.getSlot(facing, localClickPos);
        }

        if (slot == null || !attachmentType.mode.isCompatible(slot.layoutType()))
            return;
        else if (be.getAttachments().length != 0) {
            int slotIndex = layout.getIndexOfSlot(slot);
            if (slotIndex == -1)
                return;
            if (be.getAttachments()[slotIndex] != null)
                return;
        }

        AABB shape = slot.shape;

        Vec3 lowerCorner = VecHelper.rotateCentered(shape.getMinPosition(), -facing.toYRot() + 180, Direction.Axis.Y);
        Vec3 higherCorner = VecHelper.rotateCentered(shape.getMaxPosition(), -facing.toYRot() + 180, Direction.Axis.Y);

        shape = new AABB(
                lowerCorner.add(Vec3.atLowerCornerOf(targetPos)),
                higherCorner.add(Vec3.atLowerCornerOf(targetPos)));

        Outliner.getInstance().showAABB("CEEPanelPlacementOutline", shape, 3)
                .disableLineNormals();

    }

    @OnlyIn(Dist.CLIENT)
    public static void renderHighlightBlock(RenderHighlightEvent.Block event, BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;
        Level level = mc.level;
        BlockPos pos = event.getTarget().getBlockPos();
        PoseStack ms = event.getPoseStack();
        Vec3 localPos = Vec3.atLowerCornerOf(pos);
        Vec3 camPos = event.getCamera().getPosition();
        Direction facing = state.getValue(ElectricalPanelBlock.FACING);
        if (event.getTarget().getDirection() != facing || !(level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be))
            return;


        ElectricalPanelLayoutType layout = be.getLayoutType();

        Vec3 localClickPos = event.getTarget().getLocation().subtract(localPos);

        ElectricalPanelSlot slot = null;
        if (be.getAttachments().length == 0)
            return;
        if (be.getAttachments().length == 1 && be.getAttachments()[0] != null) {
            slot = ElectricalPanelSlot.FULL_SLOT;
        } else if (layout == ElectricalPanelLayoutType.HALF_HORIZONTAL) {
            slot = PanelAttachmentMode.HALF_ONLY_HORIZONTAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.HALF_VERTICAL) {
            slot = PanelAttachmentMode.HALF_ONLY_VERTICAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.THIRD) {
            slot = PanelAttachmentMode.THIRD.getSlot(facing, localClickPos);
        }


        if (slot == null)
            return;
        else if (be.getAttachments().length != 0) {
            int slotIndex = layout.getIndexOfSlot(slot);
            if (slotIndex == -1)
                return;
            if (be.getAttachments()[slotIndex] == null)
                return;
        }

        Vec3 rotatedClickPos = VecHelper.rotateCentered(localClickPos, facing.toYRot() + 180, Direction.Axis.Y);
        double x = rotatedClickPos.x;
        double y = rotatedClickPos.y;

        if (x < 2/16f || x > 14/16f || y < 2/16f || y > 14/16f)
            return;

        ms.pushPose();
        ms.translate(localPos.x - camPos.x, localPos.y - camPos.y, localPos.z - camPos.z);
        AABB shape = slot.shape;

        Vec3 lowerCorner = VecHelper.rotateCentered(shape.getMinPosition(), -facing.toYRot() + 180, Direction.Axis.Y);
        Vec3 higherCorner = VecHelper.rotateCentered(shape.getMaxPosition(), -facing.toYRot() + 180, Direction.Axis.Y);

        shape = new AABB(lowerCorner, higherCorner);

        VertexConsumer vb = event.getMultiBufferSource()
                .getBuffer(RenderType.lines());
        TrackBlockOutline.renderShape(Shapes.create(shape), ms, vb, null);

        ms.popPose();

        event.setCanceled(true);

    }
}
