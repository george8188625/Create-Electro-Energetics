package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;

public class Network {
    final Set<WrappedIndexedNode> allNodes;
    final CircuitBuilder builder;
    final InfrastructureSavedData sd;
    // For performance, instead of Couple<Integer>. A single long can hold 2 ints.
    // First -> First 4 bytes >> 32
    // Last -> Last 4 bytes
    final Long2DoubleMap voltageSources = new Long2DoubleOpenHashMap();
    final Long2DoubleMap currentSources = new Long2DoubleOpenHashMap();
    Int2ObjectMap<Int2ObjectMap<ElectricalProperties>> adjacencyOverrides = new Int2ObjectOpenHashMap<>();
    SimulationNode[] simulationNodes;
    public double[][] conductanceMatrix;
    public double[] rhsVector;

    public Network(Set<WrappedIndexedNode> allNodes, CircuitBuilder builder, InfrastructureSavedData sd) {
        this.allNodes = new HashSet<>(allNodes);
        this.builder = builder;
        this.sd = sd;
    }

    public SimulationNode[] mapToSimNodes() {
        simulationNodes = new SimulationNode[allNodes.size()];
        Object2IntOpenHashMap<WrappedIndexedNode> nodeIDs = new Object2IntOpenHashMap<>(allNodes.size(), 0.999f);

        int id = 0;
        for (WrappedIndexedNode node : allNodes) {
            SimulationNode simulationNode = new SimulationNode(node);
            simulationNode.id = id;
            simulationNodes[id] = simulationNode;
            nodeIDs.addTo(node, id);
            id++;
        }

        for (int i = 0; i < simulationNodes.length; i++) {
            SimulationNode simulationNode = simulationNodes[i];
            Int2ObjectMap<ElectricalProperties> nodeAdjacency = getAdjacency(simulationNode.correspondingNode);
            simulationNode.adjacentIDs = new int[nodeAdjacency.size()];
            simulationNode.adjacentProperties = new ElectricalProperties[nodeAdjacency.size()];
            int j = 0;
            for (Int2ObjectMap.Entry<ElectricalProperties> e : nodeAdjacency.int2ObjectEntrySet()) {
                WrappedIndexedNode adjacentNode = builder.getNode(e.getIntKey());
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
        List<WrappedIndexedNode> toDissolve = new LinkedList<>();
        for (WrappedIndexedNode node : allNodes) {
            double groundConductance = node.groundConductance;
            if (getAdjacency(node).size() == 2 && groundConductance == 0) {
                List<ElectricalProperties> connections = new ArrayList<>(getAdjacency(node).values());
                if (!connections.get(0).isVoltageSource() && !connections.get(1).isVoltageSource() && !connections.get(0).isCurrentSource() && !connections.get(1).isCurrentSource())
                    toDissolve.add(node);
            }
        }
        Set<WrappedIndexedNode> dissolved = new HashSet<>();
        for (WrappedIndexedNode node : toDissolve) {
            if (dissolved.contains(node))
                continue;

            IntSet connections = getAdjacency(node).keySet();
            if (connections.size() < 2)
                continue;
            IntIterator it = connections.iterator();
            WrappedIndexedNode prevNode = builder.getNode(it.nextInt());
            WrappedIndexedNode nextNode = builder.getNode(it.nextInt());

            LinkedList<WrappedIndexedNode> nodeChain = new LinkedList<>();
            nodeChain.add(prevNode);
            nodeChain.add(node);
            nodeChain.add(nextNode);
            dissolved.addAll(nodeChain);
            LinkedList<Double> resistanceChain = new LinkedList<>();
            resistanceChain.add(getAdjacency(prevNode).get(node.ordinal).resistance());
            resistanceChain.add(getAdjacency(nextNode).get(node.ordinal).resistance());

            if (getAdjacency(prevNode).containsKey(nextNode.ordinal))
                continue;

            WrappedIndexedNode leftNode = prevNode;
            WrappedIndexedNode prevLeftNode = node;
            while (true) {
                if (dissolved.contains(leftNode) || !toDissolve.contains(leftNode))
                    break;
                IntSet leftConnections = getAdjacency(leftNode).keySet();
                if (leftConnections.size() != 2)
                    break;
                IntIterator leftIt = leftConnections.iterator();
                WrappedIndexedNode leftPrevNode = builder.getNode(leftIt.nextInt());
                WrappedIndexedNode leftNextNode = builder.getNode(leftIt.nextInt());
                WrappedIndexedNode newLeftNode = (prevLeftNode.equals(leftPrevNode)) ? leftNextNode : leftPrevNode;
                if (getAdjacency(nodeChain.getLast()).containsKey(newLeftNode.ordinal))
                    break;
                prevLeftNode = leftNode;
                leftNode = newLeftNode;
                nodeChain.addFirst(leftNode);
                dissolved.add(leftNode);
                resistanceChain.addFirst(getAdjacency(leftNode).get(prevLeftNode.ordinal).resistance());
            }

            WrappedIndexedNode rightNode = nextNode;
            WrappedIndexedNode prevRightNode = node;
            while (true) {
                if (dissolved.contains(rightNode) || !toDissolve.contains(rightNode))
                    break;
                IntSet rightConnections = getAdjacency(rightNode).keySet();
                if (rightConnections.size() != 2)
                    break;
                IntIterator rightIt = rightConnections.iterator();
                WrappedIndexedNode rightPrevNode = builder.getNode(rightIt.nextInt());
                WrappedIndexedNode rightNextNode = builder.getNode(rightIt.nextInt());
                WrappedIndexedNode newRightNode = (prevRightNode.equals(rightPrevNode)) ? rightNextNode : rightPrevNode;
                if (getAdjacency(nodeChain.getFirst()).containsKey(newRightNode.ordinal))
                    break;
                prevRightNode = rightNode;
                rightNode = newRightNode;
                nodeChain.addLast(rightNode);
                dissolved.add(rightNode);
                resistanceChain.addLast(getAdjacency(rightNode).get(prevRightNode.ordinal).resistance());
            }
            DissolvedProperties p = new DissolvedProperties(nodeChain, resistanceChain);
            Int2ObjectMap<ElectricalProperties> leftAdjacency = overrideAdjacency(leftNode);
            Int2ObjectMap<ElectricalProperties> rightAdjacency = overrideAdjacency(rightNode);
            leftAdjacency.remove(prevLeftNode.ordinal);
            rightAdjacency.remove(prevRightNode.ordinal);
            leftAdjacency.put(rightNode.ordinal, p);
            rightAdjacency.put(leftNode.ordinal, p);

            List<WrappedIndexedNode> toRemove = nodeChain.subList(1, nodeChain.size() - 1);
            for (WrappedIndexedNode interiorChainNode : toRemove) {
                for (int neighbor : getAdjacency(interiorChainNode).keySet())
                    overrideAdjacency(builder.getNode(neighbor)).remove(interiorChainNode.ordinal);
                overrideAdjacency(interiorChainNode).clear();
                allNodes.remove(interiorChainNode);
            }

        }
    }
    private Int2ObjectMap<ElectricalProperties> getAdjacency(WrappedIndexedNode node) {
        return adjacencyOverrides.getOrDefault(node.ordinal, node.adjacency);
    }
    private Int2ObjectMap<ElectricalProperties> overrideAdjacency(WrappedIndexedNode node) {
        return adjacencyOverrides.computeIfAbsent(node.ordinal, k -> {
            Int2ObjectArrayMap<ElectricalProperties> r = new Int2ObjectArrayMap<>();
            r.putAll(node.adjacency);
            return r;
        });
    }

    public void formMatrix() {
        conductanceMatrix = new double[simulationNodes.length + voltageSources.size()][simulationNodes.length + voltageSources.size()];

        for (SimulationNode node : simulationNodes) {
            double totalConductance = 0;
            for (int i = 0; i < node.adjacentProperties.length; i++) {
                double conductance = node.adjacentProperties[i].conductance();

                totalConductance += conductance;
                conductanceMatrix[node.id][node.adjacentIDs[i]] = -conductance;
                conductanceMatrix[node.adjacentIDs[i]][node.id] = -conductance;
            }
            totalConductance += node.correspondingNode.groundConductance;
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

    public Object2DoubleMap<WrappedIndexedNode> getResults(double[] mnaResult) {
        Object2DoubleMap<WrappedIndexedNode> result = new Object2DoubleOpenHashMap<>(mnaResult.length);
        for (int i = 0; i < simulationNodes.length; i++) {
            SimulationNode simulationNode = simulationNodes[i];
            WrappedIndexedNode originalNode = simulationNode.correspondingNode;

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
