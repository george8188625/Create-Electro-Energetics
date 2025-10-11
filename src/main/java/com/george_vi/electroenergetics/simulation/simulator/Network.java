package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.content.ground_rod.GroundRodDevice;
import com.george_vi.electroenergetics.foundation.AttachedNode;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;

import java.util.*;

public class Network {
    final Set<Node> allNodes;
    final CircuitBuilder builder;
    final Map<Node, Map<Node, ElectricalProperties>> adjacency;
    final InfrastructureSavedData sd;
    final Map<Couple<SimulationNode>, Double> voltageSources = new HashMap<>();
    final Map<Couple<SimulationNode>, Double> currentSources = new HashMap<>();

    public Network(Set<Node> allNodes, Map<Node, Map<Node, ElectricalProperties>> adjacency, CircuitBuilder builder, InfrastructureSavedData sd) {
        this.allNodes = new HashSet<>(allNodes);
        this.builder = builder;
        this.adjacency = adjacency;
        this.sd = sd;
    }

    public Map<Node, SimulationNode> mapToSimNodes() {
        Map<Node, SimulationNode> simulationNodes = new HashMap<>();

        for (Node node : getAllNodes())
            simulationNodes.put(node, new SimulationNode(node));

        for (Map.Entry<Node, SimulationNode> e : simulationNodes.entrySet()) {
            Node node = e.getKey();
            SimulationNode simulationNode = e.getValue();
            for (Map.Entry<Node, ElectricalProperties> e2 : adjacency.get(node).entrySet()) {
                ElectricalProperties connectionProperties = e2.getValue();
                Node adjacentNode = e2.getKey();
                SimulationNode adjacentSimulationNode = simulationNodes.get(adjacentNode);
                if (connectionProperties.isVoltageSource() && !voltageSources.containsKey(Couple.create(adjacentSimulationNode, simulationNode)))
                    voltageSources.put(Couple.create(simulationNode, adjacentSimulationNode), connectionProperties.voltageSource());
                if (connectionProperties.isCurrentSource() && !currentSources.containsKey(Couple.create(adjacentSimulationNode, simulationNode)))
                    currentSources.put(Couple.create(simulationNode, adjacentSimulationNode), connectionProperties.currentSource());
                simulationNode.addAdjacentNode(adjacentSimulationNode, connectionProperties);
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

    public Pair<Map<Couple<SimulationNode>, Double>, Pair<double[][], double[]>> formMatrix(Map<Node, SimulationNode> simulationNodes) {
        Map<Node, Double> groundRods = builder.getGroundRods();
        double[][] conductanceMatrix = new double[simulationNodes.size() + voltageSources.size()][simulationNodes.size() + voltageSources.size()];

        int id = 0;
        for (SimulationNode node : simulationNodes.values()) {
            double totalConductance = 0;
            for (SimulationNode adjacentNode : node.getNextNodes()) {
                ElectricalProperties connectionProperties = node.getConnectionProperties(adjacentNode);
                totalConductance += connectionProperties.conductance();
            }
            totalConductance += groundRods.getOrDefault(node.correspondingNode, 0d);
            conductanceMatrix[id][id] = totalConductance;
            node.id = id++;
        }

        for (SimulationNode node : simulationNodes.values()) {
            for (SimulationNode adjacentNode : node.getNextNodes()) {
                double conductance = node.getConnectionProperties(adjacentNode).conductance();
                conductanceMatrix[node.id][adjacentNode.id] = -conductance;
                conductanceMatrix[adjacentNode.id][node.id] = -conductance;
            }
        }

        double[] d = new double[simulationNodes.size() + voltageSources.size()];

        for (Map.Entry<Couple<SimulationNode>, Double> e : currentSources.entrySet()) {
            Couple<SimulationNode> con = e.getKey();
            double v = e.getValue();
            d[con.getFirst().id] = -v;
            d[con.getSecond().id] = v;
        }

        int i = 0;
        for (Couple<SimulationNode> source : voltageSources.keySet()) {
            conductanceMatrix[simulationNodes.size() + i][source.getFirst().id] = 1d;
            conductanceMatrix[simulationNodes.size() + i][source.getSecond().id] = -1d;
            conductanceMatrix[source.getFirst().id][simulationNodes.size() + i] = 1d;
            conductanceMatrix[source.getSecond().id][simulationNodes.size() + i] = -1d;
            d[simulationNodes.size() + i] =  - voltageSources.get(source);
            i++;
        }
        return Pair.of(voltageSources, Pair.of(conductanceMatrix, d));
    }

    public Set<Node> getAllNodes() {
        return allNodes;
    }

    public Map<Node, ElectricalProperties> getConnections(Node node) {
        return adjacency.getOrDefault(node, Collections.emptyMap());
    }

    public Map<Node, Double> getResults(double[] mnaResult, Map<Node, SimulationNode> simulationNodes) {
        Map<Node, Double> result = new HashMap<>(mnaResult.length);

        for (Map.Entry<Node, SimulationNode> e : simulationNodes.entrySet()) {
            Node originalNode = e.getKey();
            SimulationNode simNode = e.getValue();
            for (Map.Entry<Node, ElectricalProperties> connectedE : getConnections(originalNode).entrySet()) {
                Node connectedNode = connectedE.getKey();
                SimulationNode connectedSimNode = simulationNodes.get(connectedNode);
                ElectricalProperties properties = connectedE.getValue();

                if (properties instanceof DissolvedProperties dp) {
                    double v2 = mnaResult[simNode.id];
                    double v1 = mnaResult[connectedSimNode.id];

                    if (dp.originalNodes.getFirst().equals(connectedNode))
                        result.putAll(dp.getVoltages(v1, v2));
                }
            }
        }

        for (Map.Entry<Node, SimulationNode> e : simulationNodes.entrySet()) {
            Node originalNode = e.getKey();
            SimulationNode simNode = e.getValue();
            if (!result.containsKey(originalNode))
                result.put(originalNode, mnaResult[simNode.id]);
        }

        return result;
    }
}
