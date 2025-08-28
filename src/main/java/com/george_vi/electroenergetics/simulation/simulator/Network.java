package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.content.ground_rod.GroundRodDevice;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;

import java.util.*;

public class Network {
    final List<Node> allNodes;
    final Map<Node, Map<Node, ElectricalProperties>> adjacency;
    final InfrastructureSavedData sd;

    public Network(List<Node> allNodes, Map<Node, Map<Node, ElectricalProperties>> adjacency, InfrastructureSavedData sd) {
        this.allNodes = new ArrayList<>(allNodes);
        this.adjacency = new HashMap<>(adjacency);
        this.sd = sd;
    }

    public Map<Node, SimulationNode> mapToSimNodes() {
        Map<Node, SimulationNode> simulationNodes = new HashMap<>();

        for (Node node : getAllNodes())
            simulationNodes.put(node, new SimulationNode(node));

        for (Map.Entry<Node, SimulationNode> node : simulationNodes.entrySet())
            for (Map.Entry<Node, ElectricalProperties> e : adjacency.get(node.getKey()).entrySet())
                node.getValue().addAdjacentNode(simulationNodes.get(e.getKey()), e.getValue());
        return simulationNodes;
    }

    public void optimize() {
        List<Node> toDissolve = new ArrayList<>();
        for (Node node : getAllNodes()) {
            if (adjacency.get(node).size() == 2 && !(sd.getDevice(node.sourcePos()).simulatedDevice() instanceof GroundRodDevice)) {

                List<ElectricalProperties> connections = getConnections(node).values().stream().toList();
                if (connections.get(0).voltageSource() == 0 && connections.get(1).voltageSource() == 0 && connections.get(0).currentSource() == 0 && connections.get(1).currentSource() == 0)
                    toDissolve.add(node);
            }
        }

        for (Node node : toDissolve) {
            List<Node> connections = getConnections(node).keySet().stream().toList();
            if (connections.size() < 2)
                continue;
            Node prevNode = connections.get(0);
            Node nextNode = connections.get(1);

            ElectricalProperties prevConnection = getConnection(prevNode, node);
            ElectricalProperties nextConnection = getConnection(nextNode, node);

            if (adjacency.get(prevNode).containsKey(nextNode) || adjacency.get(nextNode).containsKey(prevNode))
                continue;

            if (prevConnection instanceof DissolvedProperties dp && !(nextConnection instanceof DissolvedProperties)) {
                List<Node> dissolvedNodes = new ArrayList<>(dp.originalNodes);
                List<Double> dissolvedResistances = new ArrayList<>(dp.originalResistances);

                if (dissolvedNodes.getLast().equals(node)) {
                    dissolvedNodes.add(nextNode);
                    dissolvedResistances.add(nextConnection.resistance());
                } else if (dissolvedNodes.getFirst().equals(node)) {
                    dissolvedNodes.add(0, nextNode);
                    dissolvedResistances.add(0, nextConnection.resistance());
                }

                DissolvedProperties p = new DissolvedProperties(dissolvedNodes, dissolvedResistances);

                adjacency.remove(node);
                adjacency.get(prevNode).remove(node);
                adjacency.get(nextNode).remove(node);
                adjacency.get(prevNode).put(nextNode, p);
                adjacency.get(nextNode).put(prevNode, p);
                allNodes.remove(node);

            } else if (nextConnection instanceof DissolvedProperties dp && !(prevConnection instanceof DissolvedProperties)) {

                List<Node> dissolvedNodes = new ArrayList<>(dp.originalNodes);
                List<Double> dissolvedResistances = new ArrayList<>(dp.originalResistances);

                if (dissolvedNodes.getLast().equals(node)) {
                    dissolvedNodes.add(prevNode);
                    dissolvedResistances.add(prevConnection.resistance());
                } else if (dissolvedNodes.getFirst().equals(node)) {
                    dissolvedNodes.add(0, prevNode);
                    dissolvedResistances.add(0, prevConnection.resistance());
                }

                DissolvedProperties p = new DissolvedProperties(dissolvedNodes, dissolvedResistances);

                adjacency.remove(node);
                adjacency.get(prevNode).remove(node);
                adjacency.get(nextNode).remove(node);
                adjacency.get(prevNode).put(nextNode, p);
                adjacency.get(nextNode).put(prevNode, p);
                allNodes.remove(node);

            } else if (prevConnection instanceof DissolvedProperties dpPrev && nextConnection instanceof DissolvedProperties dpNext) {
                // Merge the two dissolved chains at 'node'
                List<Node> dissolvedNodes = new ArrayList<>();
                List<Double> dissolvedResistances = new ArrayList<>();

                boolean nodeAtEndPrev = dpPrev.originalNodes.getLast().equals(node);
                boolean nodeAtStartPrev = dpPrev.originalNodes.getFirst().equals(node);
                boolean nodeAtEndNext = dpNext.originalNodes.getLast().equals(node);
                boolean nodeAtStartNext = dpNext.originalNodes.getFirst().equals(node);

                if (nodeAtEndPrev && nodeAtStartNext) {
                    // dpPrev ends with node, dpNext starts with node
                    dissolvedNodes.addAll(dpPrev.originalNodes);
                    dissolvedNodes.addAll(dpNext.originalNodes.subList(1, dpNext.originalNodes.size()));

                    dissolvedResistances.addAll(dpPrev.originalResistances);
                    dissolvedResistances.addAll(dpNext.originalResistances);
                } else if (nodeAtStartPrev && nodeAtEndNext) {
                    // dpPrev starts with node, dpNext ends with node
                    dissolvedNodes.addAll(dpNext.originalNodes);
                    dissolvedNodes.addAll(dpPrev.originalNodes.subList(1, dpPrev.originalNodes.size()));

                    dissolvedResistances.addAll(dpNext.originalResistances);
                    dissolvedResistances.addAll(dpPrev.originalResistances);
                } else if (nodeAtEndPrev && nodeAtEndNext) {
                    // Both chains end with node, reverse dpNext
                    dissolvedNodes.addAll(dpPrev.originalNodes);
                    List<Node> reversedNextNodes = dpNext.originalNodes.reversed();
                    dissolvedNodes.addAll(reversedNextNodes.subList(1, reversedNextNodes.size()));

                    dissolvedResistances.addAll(dpPrev.originalResistances);
                    List<Double> reversedNextRes = dpNext.originalResistances.reversed();
                    dissolvedResistances.addAll(reversedNextRes);
                } else if (nodeAtStartPrev && nodeAtStartNext) {
                    // Both chains start with node, reverse dpPrev
                    List<Node> reversedPrevNodes = dpPrev.originalNodes.reversed();
                    dissolvedNodes.addAll(reversedPrevNodes);
                    dissolvedNodes.addAll(dpNext.originalNodes.subList(1, dpNext.originalNodes.size()));

                    List<Double> reversedPrevRes = dpPrev.originalResistances.reversed();
                    dissolvedResistances.addAll(reversedPrevRes);
                    dissolvedResistances.addAll(dpNext.originalResistances);
                } else {
                    continue;
                }

                DissolvedProperties p = new DissolvedProperties(dissolvedNodes, dissolvedResistances);

                adjacency.remove(node);
                adjacency.get(prevNode).remove(node);
                adjacency.get(nextNode).remove(node);
                adjacency.get(prevNode).put(nextNode, p);
                adjacency.get(nextNode).put(prevNode, p);
                allNodes.remove(node);

            } else {

                DissolvedProperties p = new DissolvedProperties(List.of(prevNode, node, nextNode), List.of(prevConnection.resistance(), nextConnection.resistance()));
                adjacency.remove(node);
                adjacency.get(prevNode).remove(node);
                adjacency.get(nextNode).remove(node);
                adjacency.get(prevNode).put(nextNode, p);
                adjacency.get(nextNode).put(prevNode, p);
                allNodes.remove(node);
            }
        }
    }

    private ElectricalProperties getConnection(Node node1, Node node2) {
        return getConnections(node1).getOrDefault(node2, null);
    }

    public Pair<Map<Couple<SimulationNode>, Double>, Pair<SparseMatrix<Double>, double[]>> formMatrix(Map<Node, SimulationNode> simulationNodes) {
        List<Node> groundRods = new ArrayList<>();
        Map<Couple<SimulationNode>, Double> voltageSources = new HashMap<>();
        Map<Couple<SimulationNode>, Double> currentSources = new HashMap<>();

        for (Node node : getAllNodes())
            if (sd.getDevice(node.sourcePos()).simulatedDevice() instanceof GroundRodDevice)
                groundRods.add(node);

        for (Map.Entry<Node, SimulationNode> node : simulationNodes.entrySet())
            for (Map.Entry<Node, ElectricalProperties> e : adjacency.get(node.getKey()).entrySet())
                node.getValue().addAdjacentNode(simulationNodes.get(e.getKey()), e.getValue());

        SparseMatrix<Double> conductanceMatrix = SparseMatrix.matrixD();

        int id = 0;
        boolean groundSet = false;
        for (SimulationNode node : simulationNodes.values()) {
            double totalConductance = 0;
            for (SimulationNode adjacentNode : node.getNextNodes()) {
                double R = node.getConnectionProperties(adjacentNode).resistance();
                totalConductance += 1 / node.getConnectionProperties(adjacentNode).resistance();
                if (node.getConnectionProperties(adjacentNode).voltageSource() != 0 && !voltageSources.containsKey(Couple.create(adjacentNode, node)))
                    voltageSources.put(Couple.create(node, adjacentNode), node.getConnectionProperties(adjacentNode).voltageSource());
                if (node.getConnectionProperties(adjacentNode).currentSource() != 0 && !currentSources.containsKey(Couple.create(adjacentNode, node)))
                    currentSources.put(Couple.create(node, adjacentNode), node.getConnectionProperties(adjacentNode).currentSource());
                if ((groundRods.isEmpty() && !groundSet) && (node.getConnectionProperties(adjacentNode).voltageSource() > 0 || node.getConnectionProperties(adjacentNode).currentSource() > 0)) {
                    groundSet = true;
                    totalConductance += 1;
                }
            }

            if (!groundRods.isEmpty() && groundRods.contains(node.correspondingNode))
                totalConductance += 1;
            conductanceMatrix.set(id, id, totalConductance);
            node.id = id++;
        }

        id = 0;
        for (SimulationNode node : simulationNodes.values()) {
            for (SimulationNode adjacentNode : node.getNextNodes()) {
                conductanceMatrix.set(node.id, adjacentNode.id, -1 / node.getConnectionProperties(adjacentNode).resistance());
                conductanceMatrix.set(adjacentNode.id, node.id, -1 / node.getConnectionProperties(adjacentNode).resistance());
            }
        }

        double[] d = new double[conductanceMatrix.size() + voltageSources.size()];


        for (Map.Entry<Couple<SimulationNode>, Double> e : currentSources.entrySet()) {
            Couple<SimulationNode> con = e.getKey();
            double v = e.getValue();
            d[con.getFirst().id] = -v;
            d[con.getSecond().id] = v;
        }

        int i = 0;
        for (Couple<SimulationNode> source : voltageSources.keySet()) {
            conductanceMatrix.set(simulationNodes.size() + i, source.getFirst().id, 1d);
            conductanceMatrix.set(simulationNodes.size() + i, source.getSecond().id, -1d);
            conductanceMatrix.set(source.getFirst().id, simulationNodes.size() + i, 1d);
            conductanceMatrix.set(source.getSecond().id, simulationNodes.size() + i, -1d);
//                conductanceMatrix.set(simulationNodes.size() + i, simulationNodes.size() + i, 0d);
            d[simulationNodes.size() + i] =  - voltageSources.get(source);
            i++;
        }

        return Pair.of(voltageSources, Pair.of(conductanceMatrix, d));
    }

    public List<Node> getAllNodes() {
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
