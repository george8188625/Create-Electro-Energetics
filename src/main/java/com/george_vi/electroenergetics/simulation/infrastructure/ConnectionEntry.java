package com.george_vi.electroenergetics.simulation.infrastructure;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.DoubleSupplier;

public class ConnectionEntry {
    public final Vec3 pos1;
    public final Vec3 pos2;
    public final List<Vec3> points;
    public final WireData wireData;
    public final AABB bb;
    public final boolean isCatenary;
    public final List<WireSimulationState.CutWireEntry> cuts;
    public DoubleSupplier resistance;
    public AABB dangerousBB;
    public double dangerousDistance = 0;
    public boolean isOvervolted = false;

    public ConnectionEntry(Vec3 pos1, Vec3 pos2, List<Vec3> points, WireData wireData, AABB bb, List<WireSimulationState.CutWireEntry> cuts) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.points = points;
        this.wireData = wireData;
        this.bb = bb;
        this.dangerousBB = bb;
        this.cuts = cuts;
        this.isCatenary = false;
        this.resistance = wireData.wireType()::getResistance;
    }

    public ConnectionEntry(Vec3 pos1, Vec3 pos2, List<Vec3> points, WireData wireData, AABB bb,
                           boolean isCatenary, List<WireSimulationState.CutWireEntry> cuts) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.points = points;
        this.wireData = wireData;
        this.bb = bb;
        this.dangerousBB = bb;
        this.isCatenary = isCatenary;
        this.resistance = wireData.wireType()::getResistance;
        this.cuts = cuts;
    }
}
