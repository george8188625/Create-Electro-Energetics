package com.george_vi.electroenergetics.content.electrical_panel;

import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ElectricalPanelLayoutType implements StringRepresentable {
    NONE(new ElectricalPanelSlot[0]),
    FULL(new ElectricalPanelSlot[] {ElectricalPanelSlot.FULL_SLOT}),
    HALF_HORIZONTAL(new ElectricalPanelSlot[] {ElectricalPanelSlot.HALF_UPPER, ElectricalPanelSlot.HALF_LOWER}),
    HALF_VERTICAL(new ElectricalPanelSlot[] {ElectricalPanelSlot.HALF_LEFT, ElectricalPanelSlot.HALF_RIGHT}),
    THIRD(new ElectricalPanelSlot[] {ElectricalPanelSlot.THIRD_LEFT, ElectricalPanelSlot.THIRD_CENTERED, ElectricalPanelSlot.THIRD_RIGHT});

    public final ElectricalPanelSlot[] slots;

    ElectricalPanelLayoutType(ElectricalPanelSlot[] slots) {
        this.slots = slots;
    }

    public static ElectricalPanelLayoutType byIdOrNone(String id) {
        for (ElectricalPanelLayoutType type : values())
            if (type.getSerializedName().equals(id))
                return type;
        return NONE;
    }

    public int getIndexOfSlot(ElectricalPanelSlot slot) {
        for (int i = 0; i < slots.length; i++)
            if (slots[i] == slot)
                return i;

        return -1;
    }

    @Override
    public @NotNull String getSerializedName() {
        return Lang.asId(name());
    }
}
