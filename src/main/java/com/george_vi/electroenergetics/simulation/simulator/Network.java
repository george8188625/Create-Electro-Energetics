package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;

public class Network {
    final Set<Node> allNodes;
    final CircuitBuilder builder;
    final Map<Node, Map<Node, ElectricalProperties>> adjacency;
    final InfrastructureSavedData sd;
    // For performance, instead of Couple<Integer>. A single long can hold 2 ints.
    // First -> First 4 bytes >> 32
    // Last -> Last 4 bytes
    final Long2DoubleMap voltageSources = new Long2DoubleOpenHashMap();
    final Long2DoubleMap currentSources = new Long2DoubleOpenHashMap();
    SimulationNode[] simulationNodes;
    public double[][] conductanceMatrix;
    public double[] rhsVector;

    public Network(Set<Node> allNodes, Map<Node, Map<Node, ElectricalProperties>> adjacency, CircuitBuilder builder, InfrastructureSavedData sd) {
        this.allNodes = new HashSet<>(allNodes);
        this.builder = builder;
        this.adjacency = adjacency;
        this.sd = sd;
    }

    public SimulationNode[] mapToSimNodes() {
        simulationNodes = new SimulationNode[allNodes.size()];
        Object2IntOpenHashMap<Node> nodeIDs = new Object2IntOpenHashMap<>(allNodes.size(), 0.999f);

        int id = 0;
        for (Node node : allNodes) {
            SimulationNode simulationNode = new SimulationNode(node);
            simulationNode.id = id;
            simulationNodes[id] = simulationNode;
            nodeIDs.addTo(node, id);
            id++;
        }

        for (int i = 0; i < simulationNodes.length; i++) {
            SimulationNode simulationNode = simulationNodes[i];
            Map<Node, ElectricalProperties> nodeAdjacency = adjacency.get(simulationNode.correspondingNode);
            simulationNode.adjacentIDs = new int[nodeAdjacency.size()];
            simulationNode.adjacentProperties = new ElectricalProperties[nodeAdjacency.size()];
            int j = 0;
            for (Map.Entry<Node, ElectricalProperties> e : nodeAdjacency.entrySet()) {
                Node adjacentNode = e.getKey();
                ElectricalProperties connectionProperties = e.getValue();

                int adjacentID = nodeIDs.getInt(adjacentNode);
                simulationNode.adjacentIDs[j] = adjacentID;
                simulationNode.adjacentProperties[j] = connectionProperties;

                if (connectionProperties.isVoltageSource() && i > adjacentID)
                    voltageSources.put(DataPacker.pack(i, adjacentID), connectionProperties.voltageSource());
                if (connectionProperties.isCurrentSource() && i > adjacentID)
                    currentSources.put(DataPacker.pack(i, adjacentID), connectionProperties.currentSource());

                j++;
            }
        }

        return simulationNodes;
    }

    public void optimize() {
        List<Node> toDissolve = new LinkedList<>();
        for (Node node : getAllNodes()) {
            double groundConductance = builder.getGroundConductance(node);
            if (builder.getAdjacentNodes(node).size() == 2 && groundConductance == 0) {
                List<ElectricalProperties> connections = new ArrayList<>(getConnections(node).values());
                if (!connections.get(0).isVoltageSource() && !connections.get(1).isVoltageSource() && !connections.get(0).isCurrentSource() && !connections.get(1).isCurrentSource())
                    toDissolve.add(node);
            }
        }
        Set<Node> dissolved = new HashSet<>();
        for (Node node : toDissolve) {
            if (dissolved.contains(node))
                continue;

            Set<Node> connections = getConnections(node).keySet();
            if (connections.size() < 2)
                continue;
            Iterator<Node> it = connections.iterator();
            Node prevNode = it.next();
            Node nextNode = it.next();

            LinkedList<Node> nodeChain = new LinkedList<>();
            nodeChain.add(prevNode);
            nodeChain.add(node);
            nodeChain.add(nextNode);
            dissolved.addAll(nodeChain);
            LinkedList<Double> resistanceChain = new LinkedList<>();
            resistanceChain.add(getConnection(prevNode, node).resistance());
            resistanceChain.add(getConnection(nextNode, node).resistance());

            if (adjacency.get(prevNode).containsKey(nextNode))
                continue;

            Node leftNode = prevNode;
            Node prevLeftNode = node;
            while (true) {
                if (dissolved.contains(leftNode) || !toDissolve.contains(leftNode))
                    break;
                Set<Node> leftConnections = getConnections(leftNode).keySet();
                if (leftConnections.size() != 2)
                    break;
                Iterator<Node> leftIt = leftConnections.iterator();
                Node leftPrevNode = leftIt.next();
                Node leftNextNode = leftIt.next();
                Node newLeftNode = (prevLeftNode.equals(leftPrevNode)) ? leftNextNode : leftPrevNode;
                if (adjacency.get(nodeChain.getLast()).containsKey(newLeftNode))
                    break;
                prevLeftNode = leftNode;
                leftNode = newLeftNode;
                nodeChain.addFirst(leftNode);
                dissolved.add(leftNode);
                resistanceChain.addFirst(getConnection(leftNode, prevLeftNode).resistance());
            }

            Node rightNode = nextNode;
            Node prevRightNode = node;
            while (true) {
                if (dissolved.contains(rightNode) || !toDissolve.contains(rightNode))
                    break;
                Set<Node> rightConnections = getConnections(rightNode).keySet();
                if (rightConnections.size() != 2)
                    break;
                Iterator<Node> rightIt = rightConnections.iterator();
                Node rightPrevNode = rightIt.next();
                Node rightNextNode = rightIt.next();
                Node newRightNode = (prevRightNode.equals(rightPrevNode)) ? rightNextNode : rightPrevNode;
                if (adjacency.get(nodeChain.getFirst()).containsKey(newRightNode))
                    break;
                prevRightNode = rightNode;
                rightNode = newRightNode;
                nodeChain.addLast(rightNode);
                dissolved.add(rightNode);
                resistanceChain.addLast(getConnection(rightNode, prevRightNode).resistance());
            }
            DissolvedProperties p = new DissolvedProperties(nodeChain, resistanceChain);
            Map<Node, ElectricalProperties> leftAdjacency = adjacency.get(leftNode);
            Map<Node, ElectricalProperties> rightAdjacency = adjacency.get(rightNode);
            leftAdjacency.remove(prevLeftNode);
            rightAdjacency.remove(prevRightNode);
            leftAdjacency.put(rightNode, p);
            rightAdjacency.put(leftNode, p);

            List<Node> toRemove = nodeChain.subList(1, nodeChain.size() - 1);
            for (Node interiorChainNode : toRemove) {
                Map<Node, ElectricalProperties> adjacency = this.adjacency.get(interiorChainNode);
                if (adjacency != null) {
                    for (Node neighbor : adjacency.keySet()) {
                        Map<Node, ElectricalProperties> neighbourAdjacency = this.adjacency.get(neighbor);
                        if (neighbourAdjacency != null)
                            neighbourAdjacency.remove(interiorChainNode);
                    }
                }
                this.adjacency.remove(interiorChainNode);
                allNodes.remove(interiorChainNode);
            }

        }
    }

    private ElectricalProperties getConnection(Node node1, Node node2) {
        return getConnections(node1).getOrDefault(node2, null);
    }

    public void formMatrix() {
        Map<Node, Double> groundRods = builder.getGroundRods();
        conductanceMatrix = new double[simulationNodes.length + voltageSources.size()][simulationNodes.length + voltageSources.size()];

        for (SimulationNode node : simulationNodes) {
            double totalConductance = 0;
            for (int i = 0; i < node.adjacentProperties.length; i++) {
                double conductance = node.adjacentProperties[i].conductance();

                totalConductance += conductance;
                conductanceMatrix[node.id][node.adjacentIDs[i]] = -conductance;
                conductanceMatrix[node.adjacentIDs[i]][node.id] = -conductance;
            }
            totalConductance += groundRods.getOrDefault(node.correspondingNode, 0d);
            conductanceMatrix[node.id][node.id] = totalConductance;
        }

        rhsVector = new double[simulationNodes.length + voltageSources.size()];

        for (Map.Entry<Long, Double> e : currentSources.entrySet()) {
            long packedConnection = e.getKey();
            double v = e.getValue();
            rhsVector[DataPacker.unpackFirstI(packedConnection)] = -v;
            rhsVector[DataPacker.unpackSecondI(packedConnection)] = v;
        }

        int i = 0;
        for (Map.Entry<Long, Double> e : voltageSources.entrySet()) {
            long packedConnection = e.getKey();
            double v = e.getValue();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);
            conductanceMatrix[simulationNodes.length + i][first] = 1d;
            conductanceMatrix[simulationNodes.length + i][second] = -1d;
            conductanceMatrix[first][simulationNodes.length + i] = 1d;
            conductanceMatrix[second][simulationNodes.length + i] = -1d;
            rhsVector[simulationNodes.length + i] = -v;
            i++;
        }
    }

    public Set<Node> getAllNodes() {
        return allNodes;
    }

    public Map<Node, ElectricalProperties> getConnections(Node node) {
        return adjacency.getOrDefault(node, Collections.emptyMap());
    }

    public Object2DoubleMap<Node> getResults(double[] mnaResult) {
        Object2DoubleMap<Node> result = new Object2DoubleOpenHashMap<>(mnaResult.length);
        for (int i = 0; i < simulationNodes.length; i++) {
            SimulationNode simulationNode = simulationNodes[i];
            Node originalNode = simulationNode.correspondingNode;

            for (int j = 0; j < simulationNode.adjacentIDs.length; j++) {
                int adjacentID = simulationNode.adjacentIDs[j];
                SimulationNode adjacentSimulationNode = simulationNodes[adjacentID];
                ElectricalProperties properties = simulationNode.adjacentProperties[j];

                if (properties instanceof DissolvedProperties dp) {
                    double v2 = mnaResult[i];
                    double v1 = mnaResult[adjacentID];

                    if (dp.originalNodes.getFirst().equals(adjacentSimulationNode.correspondingNode))
                        result.putAll(dp.getVoltages(v1, v2));
                }
            }
            result.putIfAbsent(originalNode, mnaResult[i]);
        }

        return result;
    }

}
