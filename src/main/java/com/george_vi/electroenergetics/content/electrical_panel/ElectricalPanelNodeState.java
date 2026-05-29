package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum ElectricalPanelNodeState implements StringRepresentable {
    NONE(new NodeConfigurator.Builder().simple(Direction.NORTH)),

    TALL_SINGLE(ofNodes(new int[]{3, 16})),
    TALL_DOUBLE(ofNodes(new int[]{1, 5, 14, 18})),
    TALL_TRIPLE(ofNodes(new int[]{1, 3, 5, 14, 16, 18})),
    TALL_QUAD(ofNodes(new int[]{0, 2, 4, 6, 13, 15, 17, 19})),

    HALF_UPPER(ofNodes(new int[]{20, 21})),
    HALF_LOWER(ofNodes(new int[]{22, 23})),
    HALF_LEFT(ofNodes(new int[]{21, 23})),
    HALF_RIGHT(ofNodes(new int[]{20, 22})),
    HALF_FULL(ofNodes(new int[]{20, 21, 22, 23})),

    TRIPLE_LEFT(ofNodes(new int[]{9, 12})),
    TRIPLE_LEFT_MIDDLE(ofNodes(new int[]{9, 8, 11, 12})),
    TRIPLE_MIDDLE(ofNodes(new int[]{8, 11})),
    TRIPLE_RIGHT(ofNodes(new int[]{7, 10})),
    TRIPLE_RIGHT_MIDDLE(ofNodes(new int[]{7, 8, 10, 11})),
    TRIPLE_SIDES(ofNodes(new int[]{7, 9, 10, 12})),
    TRIPLE_FULL(ofNodes(new int[]{7, 8, 9, 10, 11, 12})),

    ;

    final NodeConfigurator nodeConfigurator;

    ElectricalPanelNodeState(NodeConfigurator nodeConfigurator) {
        this.nodeConfigurator = nodeConfigurator;
    }

    public static ElectricalPanelNodeState getState(PanelAttachment[] attachments, ElectricalPanelLayoutType layout) {
        return switch (layout) {
            case NONE -> NONE;
            case FULL -> {
                if (attachments[0] == null)
                    yield NONE;
                if (attachments[0].type.mode == PanelAttachmentMode.FULL_SINGLE)
                    yield TALL_SINGLE;
                if (attachments[0].type.mode == PanelAttachmentMode.FULL_DOUBLE)
                    yield TALL_DOUBLE;
                if (attachments[0].type.mode == PanelAttachmentMode.FULL_TRIPLE)
                    yield TALL_TRIPLE;
                if (attachments[0].type.mode == PanelAttachmentMode.FULL_QUAD)
                    yield TALL_QUAD;
                yield NONE;
            }
            case HALF_VERTICAL -> {
                if (attachments[0] == null)
                    yield attachments[1] == null ? NONE : HALF_RIGHT;
                yield attachments[1] == null ? HALF_LEFT : HALF_FULL;
            }
            case HALF_HORIZONTAL -> {
                if (attachments[0] == null)
                    yield attachments[1] == null ? NONE : HALF_LOWER;
                yield attachments[1] == null ? HALF_UPPER : HALF_FULL;
            }
            case THIRD -> {
                if (attachments[0] == null) {
                    if (attachments[1] == null)
                        yield attachments[2] == null ? NONE : TRIPLE_RIGHT;
                    yield attachments[2] == null ? TRIPLE_MIDDLE : TRIPLE_RIGHT_MIDDLE;
                }

                if (attachments[1] == null)
                    yield attachments[2] == null ? TRIPLE_LEFT : TRIPLE_SIDES;
                yield attachments[2] == null ? TRIPLE_LEFT_MIDDLE : TRIPLE_FULL;
            }
        };
    }

    private static NodeConfigurator ofNodes(int[] ids) {
        Vec3[] combinedNodes = new Vec3[] {
                new Vec3(3.5f/16f,  13/16f, 14/16f),
                new Vec3(4/16f,     13/16f, 14/16f),
                new Vec3(6.5f/16f,  13/16f, 14/16f),
                new Vec3(8/16f,     13/16f, 14/16f),
                new Vec3(9.5f/16f,  13/16f, 14/16f),
                new Vec3(12/16f,    13/16f, 14/16f),
                new Vec3(12.5f/16f, 13/16f, 14/16f),
                new Vec3(4/16f,     12/16f, 14/16f),
                new Vec3(8/16f,     12/16f, 14/16f),
                new Vec3(12/16f,    12/16f, 14/16f),
                new Vec3(4/16f,     4/16f,  14/16f),
                new Vec3(8/16f,     4/16f,  14/16f),
                new Vec3(12/16f,    4/16f,  14/16f),
                new Vec3(3.5f/16f,  3/16f,  14/16f),
                new Vec3(4/16f,     3/16f,  14/16f),
                new Vec3(6.5f/16f,  3/16f,  14/16f),
                new Vec3(8/16f,     3/16f,  14/16f),
                new Vec3(9.5f/16f,  3/16f,  14/16f),
                new Vec3(12/16f,    3/16f,  14/16f),
                new Vec3(12.5f/16f, 3/16f,  14/16f),
                new Vec3(5/16f,     11/16f, 14/16f),
                new Vec3(11/16f,    11/16f, 14/16f),
                new Vec3(5/16f,     5/16f,  14/16f),
                new Vec3(11/16f,    5/16f,  14/16f)
        };

        NodeConfigurator.Builder builder = new NodeConfigurator.Builder();

        for (int id : ids)
            builder.add(id, combinedNodes[id]);

        return builder.simple(Direction.NORTH);
    }

    @Override
    public @NotNull String getSerializedName() {
        return Lang.asId(name());
    }
}
