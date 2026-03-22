package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class WrappedIndexedNode {
    public final Node node;
    public final int ordinal;
    public Int2ObjectArrayMap<ElectricalProperties> adjacency = new Int2ObjectArrayMap<>(8);
    /**
     * invisibleAdjacency marks the node as "connected" to other nodes, but doesn't create an electrical connection.
     * Used to make sure the both circuits are solved within one network. Use with care.
     */
    public IntList invisibleAdjacency = new IntArrayList(8);
    public double groundConductance = 0d;

    public WrappedIndexedNode(Node node, int ordinal) {
        if (node == null)
            throw new IllegalArgumentException("node can't be null!");
        this.node = node;
        this.ordinal = ordinal;
    }

    @Override
    public int hashCode() {
        return ordinal * 31 + ordinal;
    }
}
