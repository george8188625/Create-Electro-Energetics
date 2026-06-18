package com.george_vi.electroenergetics.content.electrical_panel;


import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum ElectricalPanelSlot implements StringRepresentable {
    FULL_SLOT(new AABB(2/16f, 2/16f, 9/16f, 14/16f, 14/16f, 15/16f), 0, 0, 4 / 16f, false, 48),

    HALF_UPPER(new AABB(2/16f, 8/16f, 9/16f, 14/16f, 14/16f, 15/16f), 0, 0, 4 / 16f, true, 24),
    HALF_LOWER(new AABB(2/16f, 2/16f, 9/16f, 14/16f, 8/16f, 15/16f), 6/16f, 0, 4 / 16f, true, 24),
    HALF_LEFT(new AABB(8/16f, 2/16f, 9/16f, 14/16f, 14/16f, 15/16f), 6/16f, 0, 4 / 16f, false, 24),
    HALF_RIGHT(new AABB(2/16f, 2/16f, 9/16f, 8/16f, 14/16f, 15/16f), 0, 0, 4 / 16f, false, 24),

    THIRD_LEFT(new AABB(10/16f, 2/16f, 9/16f, 14/16f, 14/16f, 15/16f), 8/16f, 0, 3 / 16f, false, 14),
    THIRD_CENTERED(new AABB(6/16f, 2/16f, 9/16f, 10/16f, 14/16f, 15/16f), 4/16f, 0, 3 / 16f, false, 14),
    THIRD_RIGHT(new AABB(2/16f, 2/16f, 9/16f, 6/16f, 14/16f, 15/16f), 0, 0, 3 / 16f, false, 14),

    THIRD_LEFT_TOP(new AABB(10/16f, 9/16f, 9/16f, 14/16f, 15/16f, 15/16f), 8/16f, 4/16f, 2 / 16f, false, 14),
    THIRD_CENTERED_TOP(new AABB(6/16f, 9/16f, 9/16f, 10/16f, 15/16f, 15/16f), 4/16f, 4/16f, 2 / 16f, false, 14),
    THIRD_RIGHT_TOP(new AABB(2/16f, 9/16f, 9/16f, 6/16f, 15/16f, 15/16f), 0, 4/16f, 2 / 16f, false, 14),
    THIRD_LEFT_BOTTOM(new AABB(10/16f, 1/16f, 9/16f, 14/16f, 7/16f, 15/16f), 8/16f, -4/16f, 2 / 16f, false, 14),
    THIRD_CENTERED_BOTTOM(new AABB(6/16f, 1/16f, 9/16f, 10/16f, 7/16f, 15/16f), 4/16f, -4/16f, 2 / 16f, false, 14),
    THIRD_RIGHT_BOTTOM(new AABB(2/16f, 1/16f, 9/16f, 6/16f, 7/16f, 15/16f), 0, -4/16f, 2 / 16f, false, 14),
    ;

    public final AABB shape;
    public final Vec3 center;
    public final double leftOffset;
    public final double topOffset;
    public final float nodeSize;
    public final boolean isHorizontal;
    public final int fullWidth;

    ElectricalPanelSlot(AABB shape, double leftOffset, double topOffset, float nodeSize, boolean isHorizontal, int fullWidth) {
        this.shape = shape;
        this.leftOffset = leftOffset;
        center = shape.getCenter();
        this.topOffset = topOffset;
        this.nodeSize = nodeSize;
        this.isHorizontal = isHorizontal;
        this.fullWidth = fullWidth;
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

    public boolean isSixth() {
        return ordinal() >= THIRD_LEFT_TOP.ordinal() && ordinal() <= THIRD_RIGHT_BOTTOM.ordinal();
    }
}
