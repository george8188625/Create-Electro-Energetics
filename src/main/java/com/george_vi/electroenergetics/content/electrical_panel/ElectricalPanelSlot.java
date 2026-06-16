package com.george_vi.electroenergetics.content.electrical_panel;


import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum ElectricalPanelSlot implements StringRepresentable {
    FULL_SLOT(new AABB(2/16f, 2/16f, 9/16f, 14/16f, 14/16f, 15/16f), 0, 4 / 16f),
    HALF_UPPER(new AABB(2/16f, 8/16f, 9/16f, 14/16f, 14/16f, 15/16f), 0, 4 / 16f),
    HALF_LOWER(new AABB(2/16f, 2/16f, 9/16f, 14/16f, 8/16f, 15/16f), 6/16f, 4 / 16f),
    HALF_LEFT(new AABB(8/16f, 2/16f, 9/16f, 14/16f, 14/16f, 15/16f), 6/16f, 4 / 16f),
    HALF_RIGHT(new AABB(2/16f, 2/16f, 9/16f, 8/16f, 14/16f, 15/16f), 0, 4 / 16f),
    THIRD_LEFT(new AABB(10/16f, 2/16f, 9/16f, 14/16f, 14/16f, 15/16f), 8/16f, 3 / 16f),
    THIRD_CENTERED(new AABB(6/16f, 2/16f, 9/16f, 10/16f, 14/16f, 15/16f), 4/16f, 3 / 16f),
    THIRD_RIGHT(new AABB(2/16f, 2/16f, 9/16f, 6/16f, 14/16f, 15/16f), 0, 3 / 16f);

    public final AABB shape;
    public final Vec3 center;
    public final double leftOffset;
    public final float nodeSize;

    ElectricalPanelSlot(AABB shape, double leftOffset, float nodeSize) {
        this.shape = shape;
        this.leftOffset = leftOffset;
        center = shape.getCenter();
        this.nodeSize = nodeSize;
    }

    public ElectricalPanelLayoutType layoutType() {
        // It's not passed in the constructor to not create a cross-reference during construction.
        return switch (this) {
            case FULL_SLOT -> ElectricalPanelLayoutType.FULL;
            case HALF_UPPER, HALF_LOWER -> ElectricalPanelLayoutType.HALF_HORIZONTAL;
            case HALF_LEFT, HALF_RIGHT -> ElectricalPanelLayoutType.HALF_VERTICAL;
            case THIRD_LEFT, THIRD_CENTERED, THIRD_RIGHT -> ElectricalPanelLayoutType.THIRD;
        };

    }

    public static ElectricalPanelSlot byId(String id) {
        for (ElectricalPanelSlot type : values())
            if (type.getSerializedName().equals(id))
                return type;
        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return Lang.asId(name());
    }
}
