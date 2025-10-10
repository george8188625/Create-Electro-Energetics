package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;

import java.util.*;

public class CircuitBuilder {
    final Map<Node, Map<Node, ElectricalProperties>> adjacency = new HashMap<>();
    final Set<Node> allNodes = new HashSet<>();
    final Map<Node, Double> groundConductance = new HashMap<>();
    final Map<Node, Integer> defaultZeroPotentials = new HashMap<>();
    boolean frozen;
    public <T extends Node> CircuitBuilder(List<T> allNodes) {
        this.allNodes.addAll(allNodes);
    }

    public CircuitBuilder() {

    }

    public ElectricalProperties getConnectionProperties(Node node1, Node node2) {
        Map<Node, ElectricalProperties> connections = adjacency.get(node1);
        return connections == null ? null : connections.get(node2);
    }

    public double getGroundConductance(Node node) {
        return groundConductance.getOrDefault(node, 0d);
    }
    public Map<Node, Double> getGroundRods() {
        return groundConductance;
    }

    public Set<Node> getAllNodes() {
        return allNodes;
    }

    public void connect(Node node1, Node node2, ElectricalProperties properties) {
        checkFrozen();
        if (node1.equals(node2))
            throw new IllegalArgumentException("Tried to create an electrical connection between two nodes: " + node1 + ", " + node2);
        adjacency.computeIfAbsent(node1, n -> new HashMap<>()).compute(node2, (n, p) -> p == null ? properties : p.add(properties));
        adjacency.computeIfAbsent(node2, n -> new HashMap<>()).compute(node1, (n, p) -> p == null ? properties.invert() : p.add(properties.invert()));
    }

    public void ground(Node node, double conductance) {
        checkFrozen();
        groundConductance.put(node, conductance);
    }

    public void defaultZeroPotential(Node node, int priority) {
        checkFrozen();
        defaultZeroPotentials.put(node, priority);
    }

    public void addNode(Node node) {
        checkFrozen();
        allNodes.add(node);
    }

    public List<Set<Node>> dfs() {
        frozen = true;
        List<Set<Node>> networks = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        for (Node node : allNodes) {
            if (!visited.contains(node)) {
                Set<Node> component = new HashSet<>();
                dfsInner(node, visited, component);
                networks.add(component);
            }
        }
        NetworksLoop:
        for (Set<Node> networkNodes : networks) {
            Node highestPriorityGround = null;
            int highestPriority = Integer.MIN_VALUE;

            for (Node node : networkNodes) {
                if (groundConductance.getOrDefault(node, 0d) != 0d)
                    continue NetworksLoop;

                int priority = defaultZeroPotentials.getOrDefault(node, 0);
                if (priority > highestPriority) {
                    highestPriorityGround = node;
                    highestPriority = priority;
                }
            }

            groundConductance.put(highestPriorityGround, 1d);
        }
        return networks;
    }

    private void dfsInner(Node current, Set<Node> visited, Set<Node> component) {
        visited.add(current);
        component.add(current);
        Map<Node, ElectricalProperties> currentAdjacency = adjacency.get(current);
        if (currentAdjacency == null)
            return;
        for (Node neighbor : currentAdjacency.keySet()) {
            if (!visited.contains(neighbor)) {
                dfsInner(neighbor, visited, component);
            }
        }
    }

    public Map<Node, ElectricalProperties> getAdjacentNodes(Node node) {
        return adjacency.getOrDefault(node, Collections.emptyMap());
    }

    public boolean isFrozen() {
        return frozen;
    }

    private void checkFrozen() {
        if (frozen)
            throw new IllegalStateException("Tried to modify a frozen circuit!");
    }
}
