package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public enum PanelAttachmentMode {
    FULL_NONE(0, new ElectricalPanelSlot[] {ElectricalPanelSlot.FULL_SLOT}),
    FULL_SINGLE(2, new ElectricalPanelSlot[] {ElectricalPanelSlot.FULL_SLOT}),
    FULL_DOUBLE(4, new ElectricalPanelSlot[] {ElectricalPanelSlot.FULL_SLOT}),
    FULL_TRIPLE(6, new ElectricalPanelSlot[] {ElectricalPanelSlot.FULL_SLOT}),
    FULL_QUAD(8, new ElectricalPanelSlot[] {ElectricalPanelSlot.FULL_SLOT}),
    QUARTER_NONE(0, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.QUARTER_CENTER,
            ElectricalPanelSlot.QUARTER_LEFT_LOWER,
            ElectricalPanelSlot.QUARTER_LEFT_UPPER,
            ElectricalPanelSlot.QUARTER_RIGHT_LOWER,
            ElectricalPanelSlot.QUARTER_RIGHT_UPPER
    }),
    HALF_NONE(0, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT
    }),
    HALF(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT
    }),
    FULL_OR_HALF_NONE(0, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.FULL_SLOT,
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT
    }),
    FULL_OR_HALF(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.FULL_SLOT,
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT
    }),
    HALF_OR_THIRD(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT,
            ElectricalPanelSlot.THIRD_RIGHT,
            ElectricalPanelSlot.THIRD_CENTERED,
            ElectricalPanelSlot.THIRD_LEFT
    }),
    HALF_OR_THIRD_NONE(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT,
            ElectricalPanelSlot.THIRD_RIGHT,
            ElectricalPanelSlot.THIRD_CENTERED,
            ElectricalPanelSlot.THIRD_LEFT
    }),
    HALF_OR_THIRD_OR_SMOL_NONE(0, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT,
            ElectricalPanelSlot.THIRD_RIGHT,
            ElectricalPanelSlot.THIRD_CENTERED,
            ElectricalPanelSlot.THIRD_LEFT,

            ElectricalPanelSlot.THIRD_RIGHT_TOP,
            ElectricalPanelSlot.THIRD_CENTERED_TOP,
            ElectricalPanelSlot.THIRD_LEFT_TOP,
            ElectricalPanelSlot.THIRD_RIGHT_BOTTOM,
            ElectricalPanelSlot.THIRD_CENTERED_BOTTOM,
            ElectricalPanelSlot.THIRD_LEFT_BOTTOM
    }),
    HALF_OR_THIRD_OR_SMOL(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_LOWER,
            ElectricalPanelSlot.HALF_RIGHT,
            ElectricalPanelSlot.THIRD_RIGHT,
            ElectricalPanelSlot.THIRD_CENTERED,
            ElectricalPanelSlot.THIRD_LEFT,

            ElectricalPanelSlot.THIRD_RIGHT_TOP,
            ElectricalPanelSlot.THIRD_CENTERED_TOP,
            ElectricalPanelSlot.THIRD_LEFT_TOP,
            ElectricalPanelSlot.THIRD_RIGHT_BOTTOM,
            ElectricalPanelSlot.THIRD_CENTERED_BOTTOM,
            ElectricalPanelSlot.THIRD_LEFT_BOTTOM
    }),
    HALF_ONLY_HORIZONTAL(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_UPPER,
            ElectricalPanelSlot.HALF_LOWER,
    }),
    HALF_ONLY_VERTICAL(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.HALF_LEFT,
            ElectricalPanelSlot.HALF_RIGHT
    }),
    THIRD(2, new ElectricalPanelSlot[] {
            ElectricalPanelSlot.THIRD_RIGHT,
            ElectricalPanelSlot.THIRD_CENTERED,
            ElectricalPanelSlot.THIRD_LEFT
    }),
    ;

    public final int nodes;
    public final ElectricalPanelSlot[] possibleSlots;

    PanelAttachmentMode(int nodes, ElectricalPanelSlot[] possibleSlots) {
        this.nodes = nodes;
        this.possibleSlots = possibleSlots;
    }

    @Nullable
    public ElectricalPanelSlot getSlot(Direction facing, Vec3 clickPosition, PanelAttachment[] existingAttachments) {
        Vec3 rotatedClickPos = VecHelper.rotateCentered(clickPosition, facing.toYRot() + 180, Direction.Axis.Y);
        double x = rotatedClickPos.x;
        double y = rotatedClickPos.y;
        if (existingAttachments[ElectricalPanelSlot.FULL_SLOT.ordinal()] != null)
            return null;

        ElectricalPanelSlot closestSlot = null;
        double closestDistanceSqr = Double.MAX_VALUE;
        SlotLoop:
        for (ElectricalPanelSlot possibleSlot : possibleSlots) {
            for (PanelAttachment attachment : existingAttachments)
                if (attachment != null && attachment.slot.shape.intersects(possibleSlot.shape))
                    continue SlotLoop;

            if (possibleSlot.shape.minX < x && possibleSlot.shape.maxX > x &&
                    possibleSlot.shape.minY < y && possibleSlot.shape.maxY > y) {

                double distanceSqr = (x - possibleSlot.center.x) * (x - possibleSlot.center.x) + (y - possibleSlot.center.y) * (y - possibleSlot.center.y);
                if (distanceSqr < closestDistanceSqr) {
                    closestDistanceSqr = distanceSqr;
                    closestSlot = possibleSlot;
                }
            }
        }
        return closestSlot;
    }

    public InWorldNode[] getNodesFor(BlockPos pos, ElectricalPanelSlot slot) {

        return switch (this) {
            case FULL_NONE, FULL_OR_HALF_NONE, HALF_NONE, HALF_OR_THIRD_NONE, HALF_OR_THIRD_OR_SMOL_NONE
                    -> new InWorldNode[]{};
            case FULL_SINGLE -> new InWorldNode[] {
                    new InWorldNode(3, pos), new InWorldNode(16, pos)};
            case FULL_DOUBLE -> new InWorldNode[] {
                    new InWorldNode(1, pos), new InWorldNode(5, pos),
                    new InWorldNode(14, pos), new InWorldNode(18, pos)};
            case FULL_TRIPLE -> new InWorldNode[] {
                    new InWorldNode(1, pos), new InWorldNode(3, pos),
                    new InWorldNode(5, pos), new InWorldNode(14, pos),
                    new InWorldNode(16, pos),new InWorldNode(18, pos)};
            case FULL_QUAD -> new InWorldNode[] {
                    new InWorldNode(0, pos), new InWorldNode(2, pos),
                    new InWorldNode(4, pos), new InWorldNode(6, pos),
                    new InWorldNode(13, pos), new InWorldNode(15, pos),
                    new InWorldNode(17, pos), new InWorldNode(19, pos)};
            default -> {
                if (slot == ElectricalPanelSlot.HALF_LOWER)
                    yield new InWorldNode[]{new InWorldNode(22, pos), new InWorldNode(23, pos)};
                else if (slot == ElectricalPanelSlot.HALF_UPPER)
                    yield new InWorldNode[]{new InWorldNode(20, pos), new InWorldNode(21, pos)};
                else if (slot == ElectricalPanelSlot.HALF_RIGHT)
                    yield new InWorldNode[]{new InWorldNode(20, pos), new InWorldNode(22, pos)};
                else if (slot == ElectricalPanelSlot.HALF_LEFT)
                    yield new InWorldNode[]{new InWorldNode(21, pos), new InWorldNode(23, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_RIGHT)
                    yield new InWorldNode[]{new InWorldNode(7, pos), new InWorldNode(10, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_CENTERED)
                    yield new InWorldNode[]{new InWorldNode(8, pos), new InWorldNode(11, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_LEFT)
                    yield new InWorldNode[]{new InWorldNode(9, pos), new InWorldNode(12, pos)};

                else if (slot == ElectricalPanelSlot.THIRD_RIGHT_BOTTOM)
                    yield new InWorldNode[]{new InWorldNode(131, pos), new InWorldNode(31, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_CENTERED_BOTTOM)
                    yield new InWorldNode[]{new InWorldNode(135, pos), new InWorldNode(35, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_LEFT_BOTTOM)
                    yield new InWorldNode[]{new InWorldNode(139, pos), new InWorldNode(39, pos)};

                else if (slot == ElectricalPanelSlot.THIRD_RIGHT_TOP)
                    yield new InWorldNode[]{new InWorldNode(291, pos), new InWorldNode(191, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_CENTERED_TOP)
                    yield new InWorldNode[]{new InWorldNode(295, pos), new InWorldNode(195, pos)};
                else if (slot == ElectricalPanelSlot.THIRD_LEFT_TOP)
                    yield new InWorldNode[]{new InWorldNode(299, pos), new InWorldNode(199, pos)};

                yield new InWorldNode[]{};
            }
        };
    }
}
