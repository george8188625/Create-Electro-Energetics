package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import com.george_vi.electroenergetics.simulation.electrical_properties.*;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.optimization.SetVoltageOptimizationEntry;
import com.george_vi.electroenergetics.simulation.optimization.SimpleTopologyOptimizationEntry;
import com.george_vi.electroenergetics.simulation.optimization.StarToDeltaEntry;
import com.george_vi.electroenergetics.simulation.optimization.TopologyOptimizationEntry;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;

public class Network {
    final Set<WrappedIndexedNode> allNodes;
    final CircuitBuilder builder;
    final InfrastructureSavedData sd;
    final Long2DoubleMap voltageSources = new Long2DoubleOpenHashMap();
    final Long2DoubleMap currentSources = new Long2DoubleOpenHashMap();
    final Long2ObjectMap<ElectricalProperties> microTicked = new Long2ObjectOpenHashMap<>();
    Object2IntOpenHashMap<WrappedIndexedNode> nodeIDs;
    SimulationNode[] simulationNodes;
    public SparseMatrix conductanceMatrix;
    public double[] rhsVector;
    public boolean voltageSourcesInMatrix = false;
    List<CoupledProperties> coupledProperties;

    Deque<TopologyOptimizationEntry> optimizations = new ArrayDeque<>();

    public Network(Collection<WrappedIndexedNode> allNodes, CircuitBuilder builder, InfrastructureSavedData sd) {
        this.allNodes = new HashSet<>(allNodes);
        this.builder = builder;
        this.sd = sd;
    }

    public SimulationNode[] mapToSimNodes() {
        simulationNodes = new SimulationNode[allNodes.size()];
        nodeIDs = new Object2IntOpenHashMap<>(allNodes.size(), 0.999f);
        coupledProperties = new ArrayList<>();

        // Assigns low-degree nodes for better conditioning

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
            simulationNode.correspondingNode.localNetworkID = id;
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
                else if (simulationNode.id > adjacentID && connectionProperties instanceof CoupledProperties cp && cp.isPrimary())
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
        for (int i = 0; i < 1000; i++) {
            if (!seriesOptimize() &
                    !starToDeltaOptimize())
                break;
        }
    }

    boolean starToDeltaOptimize() {
        for (WrappedIndexedNode node : allNodes) {
            double groundConductance = node.groundConductance;
            Int2ObjectMap<ElectricalProperties> nodeAdjacency = getAdjacency(node);
            if (groundConductance != 0)
                continue;
            if (nodeAdjacency.size() == 3) {
                if (starToDeltaOptimizeInner(node, nodeAdjacency))
                    return true;
            } else if (nodeAdjacency.size() == 1) {
                if (removeSingleDeadBranch(node, nodeAdjacency))
                    return true;
            }
        }
        return false;
    }

    private boolean removeSingleDeadBranch(WrappedIndexedNode node, Int2ObjectMap<ElectricalProperties> nodeAdjacency) {
        Iterator<Int2ObjectMap.Entry<ElectricalProperties>> it = nodeAdjacency.int2ObjectEntrySet().iterator();
        Int2ObjectMap.Entry<ElectricalProperties> baseId = it.next();
        WrappedIndexedNode base = builder.getNode(baseId.getIntKey());
        // Return false if the connection is non-purely-resistive
        if (!baseId.getValue().isSimpleResistor())
            return false;

        SetVoltageOptimizationEntry e = new SetVoltageOptimizationEntry(base, node);
        overrideAdjacency(base).remove(node.ordinal);
        overrideAdjacency(node).remove(base.ordinal);
        allNodes.remove(node);
        optimizations.push(e);

        return true;

    }

    private boolean starToDeltaOptimizeInner(WrappedIndexedNode node, Int2ObjectMap<ElectricalProperties> nodeAdjacency) {
        Iterator<Int2ObjectMap.Entry<ElectricalProperties>> it = nodeAdjacency.int2ObjectEntrySet().iterator();
        Int2ObjectMap.Entry<ElectricalProperties> a = it.next();
        Int2ObjectMap.Entry<ElectricalProperties> b = it.next();
        Int2ObjectMap.Entry<ElectricalProperties> c = it.next();
        // Return false if the star connections are non-purely-resistive
        if (!a.getValue().isSimpleResistor() || !b.getValue().isSimpleResistor() || !c.getValue().isSimpleResistor())
            return false;
        WrappedIndexedNode na = builder.getNode(a.getIntKey());
        WrappedIndexedNode nb = builder.getNode(b.getIntKey());
        WrappedIndexedNode nc = builder.getNode(c.getIntKey());


        StarToDeltaEntry e = new StarToDeltaEntry(a.getValue(), b.getValue(), c.getValue(),
                na, nb, nc,
                node);

        Int2ObjectMap<ElectricalProperties> adjacencyA = overrideAdjacency(na);
        Int2ObjectMap<ElectricalProperties> adjacencyB = overrideAdjacency(nb);
        Int2ObjectMap<ElectricalProperties> adjacencyC = overrideAdjacency(nc);
        ElectricalProperties existingAB = adjacencyA.get(nb.ordinal);
        ElectricalProperties existingBC = adjacencyB.get(nc.ordinal);
        ElectricalProperties existingCA = adjacencyC.get(na.ordinal);

        // Return false if it would create a parallel connection to a non-purely-resistive connection
        if ((existingAB != null && !existingAB.isSimpleResistor()) ||
                (existingBC != null && !existingBC.isSimpleResistor()) ||
                (existingCA != null && !existingCA.isSimpleResistor())) {
            return false;
        }

        // Create / modify delta connections
        // If the connection doesn't exist, create new one.
        // If one already exists, create a new connection that contains the old & new connections.
        ElectricalProperties pab = ElectricalProperties.resistor(e.calculateRAB());
        if (existingAB == null) {
            adjacencyA.put(nb.ordinal, pab);
            adjacencyB.put(na.ordinal, pab);
        } else {
            ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(List.of(pab, existingAB), na, nb);
            adjacencyA.put(nb.ordinal, pdp);
            adjacencyB.put(na.ordinal, pdp);
        }

        ElectricalProperties pbc = ElectricalProperties.resistor(e.calculateRBC());
        if (existingBC == null) {
            adjacencyB.put(nc.ordinal, pbc);
            adjacencyC.put(nb.ordinal, pbc);
        } else {
            ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(List.of(pbc, existingBC), nb, nc);
            adjacencyB.put(nc.ordinal, pdp);
            adjacencyC.put(nb.ordinal, pdp);
        }

        ElectricalProperties pca = ElectricalProperties.resistor(e.calculateRCA());
        if (existingCA == null) {
            adjacencyC.put(na.ordinal, pca);
            adjacencyA.put(nc.ordinal, pca);
        } else {
            ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(List.of(pca, existingCA), nc, na);
            adjacencyC.put(na.ordinal, pdp);
            adjacencyA.put(nc.ordinal, pdp);
        }

        // Remove the central node
        adjacencyA.remove(node.ordinal);
        adjacencyB.remove(node.ordinal);
        adjacencyC.remove(node.ordinal);
        overrideAdjacency(node).clear();
        allNodes.remove(node);

        optimizations.push(e);
        return true;

    }

    boolean seriesOptimize() {
        boolean result = false;

        List<WrappedIndexedNode> toDissolve = new ArrayList<>();
        for (WrappedIndexedNode node : allNodes) {
            node.isDissolved = false;
            double groundConductance = node.groundConductance;
            Int2ObjectMap<ElectricalProperties> nodeAdjacency = getAdjacency(node);
            if (nodeAdjacency.size() == 2 && groundConductance == 0) {
                Iterator<ElectricalProperties> it = nodeAdjacency.values().iterator();
                if (it.next().isSimpleResistor() && it.next().isSimpleResistor())
                    toDissolve.add(node);
            }
        }

        for (WrappedIndexedNode node : toDissolve) {
            if (node.isDissolved)
                continue;

            IntSet connections = getAdjacency(node).keySet();
            if (connections.size() < 2)
                continue;

            IntIterator it = connections.intIterator();
            WrappedIndexedNode prevNode = builder.getNode(it.nextInt());
            WrappedIndexedNode nextNode = builder.getNode(it.nextInt());

            LinkedList<WrappedIndexedNode> nodeChain = new LinkedList<>();
            nodeChain.add(prevNode);
            nodeChain.add(node);
            nodeChain.add(nextNode);
            prevNode.isDissolved = true;
            node.isDissolved = true;
            nextNode.isDissolved = true;
            LinkedList<ElectricalProperties> resistanceChain = new LinkedList<>();
            resistanceChain.add(getAdjacency(prevNode).get(node.ordinal));
            resistanceChain.add(getAdjacency(nextNode).get(node.ordinal));

            // If 2 connections are merged into one and there is another connection in place,
            // create a new connection that contains the old & new connections.
            // also it can be assumed the series chain ends here, as a non-series connection appeared.
            ElectricalProperties propertiesInPlace = getAdjacency(prevNode).get(nextNode.ordinal);
            if (propertiesInPlace != null) {
                if (!propertiesInPlace.isSimpleResistor())
                    continue;
                Int2ObjectMap<ElectricalProperties> prevAdjacency = overrideAdjacency(prevNode);
                Int2ObjectMap<ElectricalProperties> nextAdjacency = overrideAdjacency(nextNode);
                prevAdjacency.remove(node.ordinal);
                nextAdjacency.remove(node.ordinal);
                overrideAdjacency(node).clear();
                allNodes.remove(node);

                DissolvedProperties dp = new DissolvedProperties(nodeChain, resistanceChain);
                ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(List.of(dp, propertiesInPlace), prevNode, nextNode);
                prevAdjacency.put(nextNode.ordinal, pdp);
                nextAdjacency.put(prevNode.ordinal, pdp);

                optimizations.push(new SimpleTopologyOptimizationEntry(dp, prevNode, nextNode));
                continue;
            }

            WrappedIndexedNode leftNode = prevNode;
            WrappedIndexedNode prevLeftNode = node;
            while (true) {
                if (leftNode.isDissolved || !toDissolve.contains(leftNode))
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
                leftNode.isDissolved = true;
                resistanceChain.addFirst(getAdjacency(leftNode).get(prevLeftNode.ordinal));
            }

            WrappedIndexedNode rightNode = nextNode;
            WrappedIndexedNode prevRightNode = node;
            while (true) {
                if (rightNode.isDissolved || !toDissolve.contains(rightNode))
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
                rightNode.isDissolved = true;
                resistanceChain.addLast(getAdjacency(rightNode).get(prevRightNode.ordinal));
            }
            result = true;
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

            optimizations.push(new SimpleTopologyOptimizationEntry(p, nodeChain.getFirst(), nodeChain.getLast()));
        }

        return result;
    }
    private Int2ObjectMap<ElectricalProperties> getAdjacency(WrappedIndexedNode node) {
        return node.localAdjacencyOverride == null ? node.adjacency : node.localAdjacencyOverride;
    }

    private Int2ObjectMap<ElectricalProperties> overrideAdjacency(WrappedIndexedNode node) {
        if (node.localAdjacencyOverride == null) {
            node.localAdjacencyOverride = new Int2ObjectArrayMap<>(node.adjacency.size());
            node.localAdjacencyOverride.putAll(node.adjacency);
        }

        return node.localAdjacencyOverride;
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
            rhsVector[DataPacker.unpackFirstI(packedConnection)] += -v;
            rhsVector[DataPacker.unpackSecondI(packedConnection)] += v;
        }

        for (LongDoublePair e : microCurrentSources) {
            long packedConnection = e.firstLong();
            double v = e.secondDouble();
            rhsVector[DataPacker.unpackFirstI(packedConnection)] += -v;
            rhsVector[DataPacker.unpackSecondI(packedConnection)] += v;
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
    public void getResults(double[] mnaResult, double[] toFill, int microTick, int totalMicroTicks) {
        lastMNAResult = mnaResult;

        for (int i = 0; i < simulationNodes.length; i++) {
            SimulationNode simulationNode = simulationNodes[i];
            WrappedIndexedNode originalNode = simulationNode.correspondingNode;

            toFill[originalNode.ordinal * totalMicroTicks + microTick] = mnaResult[i];
        }

        for (TopologyOptimizationEntry entry : optimizations) {
            if (entry instanceof SimpleTopologyOptimizationEntry properties) {
                double v1 = toFill[properties.node1().ordinal * totalMicroTicks + microTick];
                double v2 = toFill[properties.node2().ordinal * totalMicroTicks + microTick];
                properties.properties().getVoltages(v1, v2, toFill, microTick, totalMicroTicks);
            } else if (entry instanceof StarToDeltaEntry delta) {
                double va = toFill[delta.na.ordinal * totalMicroTicks + microTick];
                double vb = toFill[delta.nb.ordinal * totalMicroTicks + microTick];
                double vc = toFill[delta.nc.ordinal * totalMicroTicks + microTick];
                toFill[delta.centralNode.ordinal * totalMicroTicks + microTick] = delta.calculateCenter(va, vb, vc);
            } else if (entry instanceof SetVoltageOptimizationEntry setV) {
                double vBase = toFill[setV.base().ordinal * totalMicroTicks + microTick];
                toFill[setV.dead().ordinal * totalMicroTicks + microTick] = vBase;
            }

        }

    }

}
