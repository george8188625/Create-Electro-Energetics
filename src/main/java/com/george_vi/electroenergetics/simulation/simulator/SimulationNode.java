package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;

public class SimulationNode {
    final WrappedIndexedNode correspondingNode;
    int id;
    public int[] adjacentIDs;
    public ElectricalProperties[] adjacentProperties;

    public SimulationNode(WrappedIndexedNode correspondingNode) {
        this.correspondingNode = correspondingNode;
    }


    @Override
    public String toString() {
        return "N" + id;
    }
}
