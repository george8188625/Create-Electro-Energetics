package com.george_vi.electroenergetics.foundation;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public class AttachedNode extends Node {
    public final String ownerID;
    public final boolean grounded;

    public AttachedNode(int id, BlockPos pos, String ownerID, boolean grounded) {
        super(id, pos);
        this.ownerID = ownerID;
        this.grounded = grounded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AttachedNode that = (AttachedNode) o;
        return Objects.equals(ownerID, that.ownerID) && Objects.equals(grounded, that.grounded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ownerID, grounded);
    }
}
