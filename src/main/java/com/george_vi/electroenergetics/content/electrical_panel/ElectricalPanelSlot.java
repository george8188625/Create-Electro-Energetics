package com.george_vi.electroenergetics.content.electrical_panel;


import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum ElectricalPanelSlot implements StringRepresentable {
    FULL_SLOT(createShape(2, 2, 14, 14), 0, 0, 4 / 16f, false, 48),

    HALF_UPPER(createShape(2, 8, 14, 14), 0, 0, 4 / 16f, true, 24),
    HALF_LOWER(createShape(2, 2, 14, 8), 6/16f, 0, 4 / 16f, true, 24),
    HALF_LEFT(createShape(8, 2, 14, 14), 6/16f, 0, 4 / 16f, false, 24),
    HALF_RIGHT(createShape(2, 2, 8, 14), 0, 0, 4 / 16f, false, 24),

    THIRD_LEFT(createShape(10, 2, 14, 14), 8/16f, 0, 3 / 16f, false, 14),
    THIRD_CENTERED(createShape(6, 2, 10, 14), 4/16f, 0, 3 / 16f, false, 14),
    THIRD_RIGHT(createShape(2, 2, 6, 14), 0, 0, 3 / 16f, false, 14),

    THIRD_LEFT_TOP(createShape(10, 9, 14, 15), 8/16f, 4/16f, 2 / 16f, false, 14),
    THIRD_CENTERED_TOP(createShape(6, 9, 10, 15), 4/16f, 4/16f, 2 / 16f, false, 14),
    THIRD_RIGHT_TOP(createShape(2, 9, 6, 15), 0, 4/16f, 2 / 16f, false, 14),
    THIRD_LEFT_BOTTOM(createShape(10, 1, 14, 7), 8/16f, -4/16f, 2 / 16f, false, 14),
    THIRD_CENTERED_BOTTOM(createShape(6, 1, 10, 7), 4/16f, -4/16f, 2 / 16f, false, 14),
    THIRD_RIGHT_BOTTOM(createShape(2, 1, 6, 7), 0, -4/16f, 2 / 16f, false, 14),

    QUARTER_CENTER(createShape(5, 5, 11, 11), 0, 0, 2 / 16f, false, 24),
    QUARTER_LEFT_UPPER(createShape(8, 8, 14, 14), 3/16f, 3/16f, 2 / 16f, false, 24),
    QUARTER_LEFT_LOWER(createShape(8, 2, 14, 8), 3/16f, -3/16f, 2 / 16f, false, 24),
    QUARTER_RIGHT_UPPER(createShape(2, 8, 8, 14), -3/16f, 3/16f, 2 / 16f, false, 24),
    QUARTER_RIGHT_LOWER(createShape(2, 2, 8, 8), -3/16f, -3/16f, 2 / 16f, false, 24),
    ;

    private static @NotNull AABB createShape(int minX, int minY, int maxX, int maxY) {
        return new AABB(minX / 16f, minY / 16f, 9 / 16f, maxX / 16f, maxY / 16f, 15 / 16f);
    }

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
