package com.george_vi.electroenergetics.foundation.nodes;

import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;

public class PositionedAttachedNode extends AttachedNode implements Position {
    final double x;
    final double y;
    final double z;

    public PositionedAttachedNode(int id, String ownerID, double x, double y, double z) {
        super(id, ownerID);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PositionedAttachedNode(int id, String ownerID, Position pos) {
        super(id, ownerID);
        this.x = pos.x();
        this.y = pos.y();
        this.z = pos.z();
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    public Vec3 position() {
        return new Vec3(x, y, z);
    }
}
