package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public enum PanelAttachmentMode {
    FULL_SINGLE(2, 4 / 16f),
    FULL_DOUBLE(4, 4 / 16f),
    FULL_TRIPLE(6, 4 / 16f),
    FULL_QUAD(8, 4 / 16f),
    HALF(2, 4 / 16f),
    HALF_ONLY_HORIZONTAL(2, 4 / 16f),
    HALF_ONLY_VERTICAL(2, 4 / 16f),
    THIRD(2, 3 / 16f),
    ;

    public final int nodes;
    public final float nodeWidth;

    PanelAttachmentMode(int nodes, float nodeWidth) {
        this.nodes = nodes;
        this.nodeWidth = nodeWidth;
    }

    public ElectricalPanelSlot getSlot(Direction facing, Vec3 clickPosition) {
        Vec3 rotatedClickPos = VecHelper.rotateCentered(clickPosition, facing.toYRot() + 180, Direction.Axis.Y);
        double x = rotatedClickPos.x;
        double y = rotatedClickPos.y;

        return switch (this) {
            case FULL_SINGLE, FULL_DOUBLE, FULL_TRIPLE, FULL_QUAD -> ElectricalPanelSlot.FULL_SLOT;
            case HALF -> {
                if (x > (1 - y))
                    yield x > y ? ElectricalPanelSlot.HALF_LEFT : ElectricalPanelSlot.HALF_UPPER;
                yield x > y ? ElectricalPanelSlot.HALF_LOWER : ElectricalPanelSlot.HALF_RIGHT;
            }
            case HALF_ONLY_HORIZONTAL -> y > 0.5 ? ElectricalPanelSlot.HALF_UPPER : ElectricalPanelSlot.HALF_LOWER;
            case HALF_ONLY_VERTICAL -> x > 0.5 ? ElectricalPanelSlot.HALF_LEFT : ElectricalPanelSlot.HALF_RIGHT;
            case THIRD -> {
                if (x > 10 / 16f)
                    yield ElectricalPanelSlot.THIRD_LEFT;
                else if (x > 6 / 16f)
                    yield ElectricalPanelSlot.THIRD_CENTERED;
                yield ElectricalPanelSlot.THIRD_RIGHT;
            }
        };
    }

    public InWorldNode[] getNodesFor(int attachmentIndex, BlockPos pos, ElectricalPanelLayoutType layout) {
        if (attachmentIndex >= layout.slots.length)
            throw new IllegalArgumentException("attachmentIndex: " + attachmentIndex + " is too large for layout " + layout.getSerializedName() + '!');

        return switch (this) {
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
            case HALF, HALF_ONLY_HORIZONTAL, HALF_ONLY_VERTICAL -> {
                if (layout == ElectricalPanelLayoutType.HALF_HORIZONTAL) {
                    if (attachmentIndex == 1)
                        yield new InWorldNode[] {new InWorldNode(22, pos), new InWorldNode(23, pos)};
                    yield new InWorldNode[] {new InWorldNode(20, pos), new InWorldNode(21, pos)};
                }
                if (attachmentIndex == 1)
                    yield new InWorldNode[] {new InWorldNode(20, pos), new InWorldNode(22, pos)};
                yield new InWorldNode[] {new InWorldNode(21, pos), new InWorldNode(23, pos)};
            }
            case THIRD -> {
                if (attachmentIndex == 2)
                    yield new InWorldNode[] {new InWorldNode(7, pos), new InWorldNode(10, pos)};
                if (attachmentIndex == 1)
                    yield new InWorldNode[] {new InWorldNode(8, pos), new InWorldNode(11, pos)};
                yield new InWorldNode[] {new InWorldNode(9, pos), new InWorldNode(12, pos)};
            }
        };
    }

    public boolean isCompatible(ElectricalPanelLayoutType layout) {
        if (layout == ElectricalPanelLayoutType.NONE)
            return true;

        return switch (this) {
            case FULL_SINGLE, FULL_DOUBLE, FULL_QUAD, FULL_TRIPLE -> layout == ElectricalPanelLayoutType.FULL;
            case HALF -> layout == ElectricalPanelLayoutType.HALF_HORIZONTAL || layout == ElectricalPanelLayoutType.HALF_VERTICAL;
            case HALF_ONLY_HORIZONTAL -> layout == ElectricalPanelLayoutType.HALF_HORIZONTAL;
            case HALF_ONLY_VERTICAL -> layout == ElectricalPanelLayoutType.HALF_VERTICAL;
            case THIRD -> layout == ElectricalPanelLayoutType.THIRD;
        };
    }
}
