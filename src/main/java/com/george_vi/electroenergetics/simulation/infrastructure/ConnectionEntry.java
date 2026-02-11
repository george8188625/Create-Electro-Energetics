package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleSupplier;

public class ConnectionEntry {
    private static int cutNodeIds = 0;
    public final Vec3 pos1;
    public final Vec3 pos2;
    public final List<Vec3> points;
    public final WireData wireData;
    public final AABB bb;
    public final boolean isCatenary;
    public List<Pair<Float, AttachedNode>> cuts = new ArrayList<>();
    public DoubleSupplier resistance;

    public ConnectionEntry(Vec3 pos1, Vec3 pos2, List<Vec3> points, WireData wireData, AABB bb) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.points = points;
        this.wireData = wireData;
        this.bb = bb;
        this.isCatenary = false;
        this.resistance = wireData.wireType()::getResistance;
    }

    public ConnectionEntry(Vec3 pos1, Vec3 pos2, List<Vec3> points, WireData wireData, AABB bb, boolean isCatenary) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.points = points;
        this.wireData = wireData;
        this.bb = bb;
        this.isCatenary = isCatenary;
        this.resistance = wireData.wireType()::getResistance;
    }

    public AttachedNode addCut(float point) {
        AttachedNode node = new AttachedNode(cutNodeIds++, "CEECutWire");
        cuts.add(Pair.of(point, node));
        cuts.sort(Comparator.comparing(Pair::getFirst));
        return node;
    }

    public void removeCut(AttachedNode node) {
        cuts.removeIf(p -> p.getSecond().equals(node));
    }
}
