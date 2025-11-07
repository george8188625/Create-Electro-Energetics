package com.george_vi.electroenergetics.foundation;

import java.util.Objects;

public class AttachedNode extends Node {
    public final String ownerID;
    public final int id;

    public AttachedNode(int id, String ownerID) {
        this.ownerID = ownerID;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AttachedNode that = (AttachedNode) o;
        return Objects.equals(ownerID, that.ownerID) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerID, id);
    }
}
