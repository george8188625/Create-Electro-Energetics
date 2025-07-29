package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationNode {
    final Node correspondingNode;
    int id;
    final Map<SimulationNode, ElectricalProperties> connections = new HashMap<>();

    public SimulationNode(Node correspondingNode) {
        this.correspondingNode = correspondingNode;
    }

    public void addAdjacentNode(SimulationNode node, ElectricalProperties connectionProperties) {
        connections.put(node, connectionProperties);
    }

    public List<SimulationNode> getNextNodes() {
        return connections.keySet().stream().toList();
    }

    public ElectricalProperties getConnectionProperties(SimulationNode node) {
        return connections.get(node);
    }

    @Override
    public String toString() {
        return "SimulationNode{id=" + id + "}";
    }
}
