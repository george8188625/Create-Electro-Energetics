package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public class WrappedIndexedNode {
    public final Node node;
    public final int ordinal;
    public Int2ObjectArrayMap<ElectricalProperties> adjacency = new Int2ObjectArrayMap<>(6);
    public double groundConductance = 0d;

    public WrappedIndexedNode(Node node, int ordinal) {
        this.node = node;
        this.ordinal = ordinal;
    }
}
