package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
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
    final Long2ObjectMap<ElectricalProperties> microTicked = new Long2ObjectOpenHashMap<>();
    Int2ObjectMap<Int2ObjectMap<ElectricalProperties>> adjacencyOverrides = new Int2ObjectOpenHashMap<>();
    Object2IntOpenHashMap<WrappedIndexedNode> nodeIDs;
    SimulationNode[] simulationNodes;
    public SparseMatrix conductanceMatrix;
    public double[] rhsVector;
    public boolean voltageSourcesInMatrix = false;
    List<CoupledProperties> coupledProperties;

    public Network(Collection<WrappedIndexedNode> allNodes, CircuitBuilder builder, InfrastructureSavedData sd) {
        this.allNodes = new HashSet<>(allNodes);
        this.builder = builder;
        this.sd = sd;
    }

    public SimulationNode[] mapToSimNodes() {
        simulationNodes = new SimulationNode[allNodes.size()];
        nodeIDs = new Object2IntOpenHashMap<>(allNodes.size(), 0.999f);
        coupledProperties = new ArrayList<>();

        // Assigns low-degree nodes first, for quicker convergence.
        int id = 0;
        for (WrappedIndexedNode node : allNodes) {
            SimulationNode simulationNode = new SimulationNode(node);
            simulationNodes[id] = simulationNode;
            id++;
        }

        Arrays.sort(simulationNodes, Comparator.comparing(n -> getAdjacency(n.correspondingNode).size()));

        id = 0;
        for (SimulationNode simulationNode : simulationNodes) {
            simulationNode.id = id;
            nodeIDs.addTo(simulationNode.correspondingNode, id);
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
                if (connectionProperties instanceof MicroTickingElectricalProperties)
                    microTicked.put(DataPacker.pack(i, adjacentID), connectionProperties);
                if (simulationNode.id > adjacentID && connectionProperties instanceof CoupledProperties cp && cp.isPrimary())
                    coupledProperties.add(cp);
                else if (!(connectionProperties instanceof MicroTickingInvertedElectricalProperties)) {
                    if ((connectionProperties.isVoltageSource()) && i > adjacentID)
                        voltageSources.put(DataPacker.pack(i, adjacentID), connectionProperties.voltageSource());
                    if (connectionProperties.isCurrentSource() && i > adjacentID)
                        currentSources.put(DataPacker.pack(i, adjacentID), connectionProperties.currentSource());
                }
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
                if (connections.get(0).isSimpleResistor() && connections.get(1).isSimpleResistor())
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
        voltageSourcesInMatrix = false;
        List<LongDoublePair> microVoltageSources = new ArrayList<>();
        List<LongDoublePair> microCurrentSources = new ArrayList<>();
        for (Long2ObjectMap.Entry<ElectricalProperties> entry : microTicked.long2ObjectEntrySet()) {
            if (entry.getValue().isVoltageSource())
                microVoltageSources.add(new LongDoubleImmutablePair(entry.getLongKey(), entry.getValue().voltageSource()));
            if (entry.getValue().isCurrentSource())
                microCurrentSources.add(new LongDoubleImmutablePair(entry.getLongKey(), entry.getValue().currentSource));
        }

        int size = simulationNodes.length + voltageSources.size() + microVoltageSources.size() + (coupledProperties.size() * 2);

        conductanceMatrix = new SparseMatrix(size);

        for (SimulationNode node : simulationNodes) {
            double totalConductance = 0;
            for (int i = 0; i < node.adjacentProperties.length; i++) {
                double conductance = node.adjacentProperties[i].conductance();

                totalConductance += conductance;
                if (conductance == 0)
                    continue;
                conductanceMatrix.set(node.id, node.adjacentIDs[i], -conductance);
                conductanceMatrix.set(node.adjacentIDs[i], node.id, -conductance);
            }
            totalConductance += node.correspondingNode.groundConductance;
            conductanceMatrix.set(node.id, node.id, totalConductance);
//            conductanceMatrix[node.id][node.id] = totalConductance;
        }

        rhsVector = new double[size];

        for (Long2DoubleMap.Entry e : currentSources.long2DoubleEntrySet()) {
            long packedConnection = e.getLongKey();
            double v = e.getDoubleValue();
            rhsVector[DataPacker.unpackFirstI(packedConnection)] = -v;
            rhsVector[DataPacker.unpackSecondI(packedConnection)] = v;
        }

        for (LongDoublePair e : microCurrentSources) {
            long packedConnection = e.firstLong();
            double v = e.secondDouble();
            rhsVector[DataPacker.unpackFirstI(packedConnection)] = -v;
            rhsVector[DataPacker.unpackSecondI(packedConnection)] = v;
        }

        int i = simulationNodes.length;
        for (Long2DoubleMap.Entry e : voltageSources.long2DoubleEntrySet()) {
            long packedConnection = e.getLongKey();
            double v = e.getDoubleValue();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);
            conductanceMatrix.set(i, first, 1d);
            conductanceMatrix.set(i, second, -1d);
            conductanceMatrix.set(first, i, 1d);
            conductanceMatrix.set(second, i, -1d);
            rhsVector[i] = -v;
            i++;
            voltageSourcesInMatrix = true;
        }

        for (LongDoublePair e : microVoltageSources) {
            long packedConnection = e.firstLong();
            double v = e.secondDouble();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);

            conductanceMatrix.set(i, first, 1d);
            conductanceMatrix.set(i, second, -1d);
            conductanceMatrix.set(first, i, 1d);
            conductanceMatrix.set(second, i, -1d);
            rhsVector[i] = -v;
            voltageSourcesInMatrix = true;
            i++;
        }

        for (CoupledProperties cp : coupledProperties) {
            WrappedIndexedNode p1 = builder.getNode(cp.nodes().node1());
            WrappedIndexedNode p2 = builder.getNode(cp.nodes().node2());
            WrappedIndexedNode s1 = builder.getNode(cp.coupledNodes().node1());
            WrappedIndexedNode s2 = builder.getNode(cp.coupledNodes().node2());
            if (p1 == null || p2 == null || s1 == null || s2 == null)
                continue;

            int ip1 = nodeIDs.getInt(p1);
            int ip2 = nodeIDs.getInt(p2);
            int is1 = nodeIDs.getInt(s1);
            int is2 = nodeIDs.getInt(s2);

            // Primary (ROW I1)
            conductanceMatrix.set(ip1, i, +1);
            conductanceMatrix.set(ip2, i, -1);

            // Vp1 - Vp2 - n*(Vs1 - Vs2) = 0
            conductanceMatrix.set(i, ip1, +1);
            conductanceMatrix.set(i, ip2, -1);
            conductanceMatrix.set(i, is1, -cp.ratio());
            conductanceMatrix.set(i, is2, cp.ratio());
            i++;

            // Secondary (ROW I2)
            conductanceMatrix.set(is1, i, +1);
            conductanceMatrix.set(is2, i, -1);

            conductanceMatrix.set(i, i-1, cp.ratio());
            conductanceMatrix.set(i, i, 1);
            i++;
            voltageSourcesInMatrix = true;
        }
    }

    double[] lastMNAResult;
    public void getResults(double[] mnaResult, double[] toFill, int microTickBits, int microTick) {
        lastMNAResult = mnaResult;
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
                        dp.getVoltages(v1, v2, toFill, microTickBits, microTick);
                }
            }
            toFill[(originalNode.ordinal << microTickBits) | microTick] = mnaResult[i];
        }
    }

}
