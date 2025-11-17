package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.nodes.Node;

public class SimulationNode {
    final Node correspondingNode;
    int id;
    public int[] adjacentIDs;
    public ElectricalProperties[] adjacentProperties;

    public SimulationNode(Node correspondingNode) {
        this.correspondingNode = correspondingNode;
    }


    @Override
    public String toString() {
        return "N" + id;
    }
}
