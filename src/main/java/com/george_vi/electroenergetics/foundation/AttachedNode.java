package com.george_vi.electroenergetics.foundation;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public class AttachedNode extends Node {
    public final String ownerID;

    public AttachedNode(int id, String ownerID) {
        super(id, new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        this.ownerID = ownerID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AttachedNode that = (AttachedNode) o;
        return Objects.equals(ownerID, that.ownerID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ownerID);
    }
}
