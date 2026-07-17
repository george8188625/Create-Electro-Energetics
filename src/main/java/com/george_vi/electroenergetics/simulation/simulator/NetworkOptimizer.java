package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.SimulationNode;
import com.george_vi.electroenergetics.simulation.electrical_properties.*;
import com.george_vi.electroenergetics.simulation.optimization.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.*;

public class NetworkOptimizer {

    final Network network;
    final CircuitBuilder builder;

    public byte optimizationPass = ElectricalProperties.NORMAL_OPTIMIZATION;

    public NetworkOptimizer(Network network) {
        this.network = network;
        this.builder = network.builder;
    }

    private boolean starToDeltaOptimize() {
        for (SimulationNode node : network.allNodes) {
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

    private boolean removeSingleDeadBranch(SimulationNode node, Int2ObjectMap<ElectricalProperties> nodeAdjacency) {
        Iterator<Int2ObjectMap.Entry<ElectricalProperties>> it = nodeAdjacency.int2ObjectEntrySet().iterator();
        Int2ObjectMap.Entry<ElectricalProperties> baseId = it.next();
        SimulationNode base = builder.getNode(baseId.getIntKey());
        // Return false if the connection is non-purely-resistive
        if (!baseId.getValue().isSimpleResistor())
            return false;

        SetVoltageOptimizationEntry e = new SetVoltageOptimizationEntry(base.ordinal, node.ordinal);
        overrideAdjacency(base).remove(node.ordinal);
        overrideAdjacency(node).remove(base.ordinal);
        network.allNodes.remove(node);
        network.optimizations.push(e);

        return true;
    }

    private boolean starToDeltaOptimizeInner(SimulationNode node, Int2ObjectMap<ElectricalProperties> nodeAdjacency) {
        Iterator<Int2ObjectMap.Entry<ElectricalProperties>> it = nodeAdjacency.int2ObjectEntrySet().iterator();
        Int2ObjectMap.Entry<ElectricalProperties> a = it.next();
        Int2ObjectMap.Entry<ElectricalProperties> b = it.next();
        Int2ObjectMap.Entry<ElectricalProperties> c = it.next();
        // Return false if the star connections are non-purely-resistive
        if (!a.getValue().isSimpleResistor() || !b.getValue().isSimpleResistor() || !c.getValue().isSimpleResistor())
            return false;
        SimulationNode na = builder.getNode(a.getIntKey());
        SimulationNode nb = builder.getNode(b.getIntKey());
        SimulationNode nc = builder.getNode(c.getIntKey());


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
            ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(new ElectricalProperties[] {pab, existingAB}, na.ordinal, nb.ordinal);
            adjacencyA.put(nb.ordinal, pdp);
            adjacencyB.put(na.ordinal, pdp);
        }

        ElectricalProperties pbc = ElectricalProperties.resistor(e.calculateRBC());
        if (existingBC == null) {
            adjacencyB.put(nc.ordinal, pbc);
            adjacencyC.put(nb.ordinal, pbc);
        } else {
            ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(new ElectricalProperties[] {pbc, existingBC}, nb.ordinal, nc.ordinal);
            adjacencyB.put(nc.ordinal, pdp);
            adjacencyC.put(nb.ordinal, pdp);
        }

        ElectricalProperties pca = ElectricalProperties.resistor(e.calculateRCA());
        if (existingCA == null) {
            adjacencyC.put(na.ordinal, pca);
            adjacencyA.put(nc.ordinal, pca);
        } else {
            ParallelDissolvedProperties pdp = new ParallelDissolvedProperties(new ElectricalProperties[] {pca, existingCA}, nc.ordinal, na.ordinal);
            adjacencyC.put(na.ordinal, pdp);
            adjacencyA.put(nc.ordinal, pdp);
        }

        // Remove the central node
        adjacencyA.remove(node.ordinal);
        adjacencyB.remove(node.ordinal);
        adjacencyC.remove(node.ordinal);
        overrideAdjacency(node).clear();
        network.allNodes.remove(node);
        network.optimizations.push(e);
        return true;

    }


    private static final byte TO_DISSOLVE = 1;
    private static final byte DISSOLVED = 2;
    boolean seriesOptimize() {
        boolean result = false;

        List<SimulationNode> toDissolve = new ArrayList<>();
        for (SimulationNode node : network.allNodes) {
            node.dissolveState = 0;
            double groundConductance = node.groundConductance;
            Int2ObjectMap<ElectricalProperties> nodeAdjacency = getAdjacency(node);
            if (nodeAdjacency.size() == 2 && groundConductance <= 0) {
                Iterator<ElectricalProperties> it = nodeAdjacency.values().iterator();
                if (it.next().canDissolve(optimizationPass) && it.next().canDissolve(optimizationPass)) {
                    toDissolve.add(node);
                    node.dissolveState = TO_DISSOLVE;
                }
            }
        }

        for (SimulationNode node : toDissolve) {
            if (node.dissolveState != TO_DISSOLVE)
                continue;

            IntSet connections = getAdjacency(node).keySet();
            if (connections.size() < 2)
                continue;

            IntIterator it = connections.intIterator();
            SimulationNode prevNode = builder.getNode(it.nextInt());
            SimulationNode nextNode = builder.getNode(it.nextInt());

            Deque<SimulationNode> nodeChain = new ArrayDeque<>();
            nodeChain.add(prevNode);
            nodeChain.add(node);
            nodeChain.add(nextNode);
            Deque<ElectricalProperties> resistanceChain = new ArrayDeque<>();
            resistanceChain.add(getAdjacency(prevNode).get(node.ordinal));
            resistanceChain.add(getAdjacency(node).get(nextNode.ordinal));
            if (!resistanceChain.getFirst().canDissolve(optimizationPass) ||
                    !resistanceChain.getLast().canDissolve(optimizationPass))
                continue;
            node.dissolveState = DISSOLVED;

            boolean purelyResistive =
                    resistanceChain.getFirst().isSimpleResistor() &&
                            resistanceChain.getLast().isSimpleResistor();

            // If 2 connections are merged into one and there is another connection in place,
            // create a new connection that contains the old & new connections.
            // also it can be assumed the series chain ends here, as a non-series connection appeared.
            ElectricalProperties propertiesInPlace = getAdjacency(prevNode).get(nextNode.ordinal);
            if (propertiesInPlace != null) {
                nextNode.dissolveState = DISSOLVED;
                prevNode.dissolveState = DISSOLVED;

                // If that connection is a transformer, and it is the only one, completely dissolve it.
                if (propertiesInPlace instanceof CoupledProperties cp) {
                    result = optimizeCoupledProperties(node, cp, prevNode, nextNode, resistanceChain) | result;
                    continue;
                }

                if (!propertiesInPlace.canDissolve(optimizationPass))
                    continue;
                Int2ObjectMap<ElectricalProperties> prevAdjacency = overrideAdjacency(prevNode);
                Int2ObjectMap<ElectricalProperties> nextAdjacency = overrideAdjacency(nextNode);
                prevAdjacency.remove(node.ordinal);
                nextAdjacency.remove(node.ordinal);
                overrideAdjacency(node).clear();
                network.allNodes.remove(node);
                IDissolvedProperties dp = purelyResistive ? new DissolvedProperties(nodeChain, resistanceChain) :
                        new AdvancedDissolvedProperties(nodeChain, resistanceChain);
                IDissolvedProperties pdp = purelyResistive && propertiesInPlace.isSimpleResistor() ?
                        new ParallelDissolvedProperties(
                                new ElectricalProperties[] {(ElectricalProperties)dp, propertiesInPlace},
                                prevNode.ordinal, nextNode.ordinal) :
                        new AdvancedParallelDissolvedProperties(
                                new ElectricalProperties[] {(ElectricalProperties)dp, propertiesInPlace},
                                prevNode.ordinal, nextNode.ordinal);
                prevAdjacency.put(nextNode.ordinal, (ElectricalProperties) pdp);
                nextAdjacency.put(prevNode.ordinal, ((ElectricalProperties) pdp).invert());

                network.optimizations.push(new SimpleTopologyOptimizationEntry(dp, prevNode.ordinal, nextNode.ordinal));
                result = true;
                continue;
            }

            SimulationNode leftNode = prevNode;
            SimulationNode prevLeftNode = node;
            while (true) {
                if (leftNode.dissolveState != TO_DISSOLVE)
                    break;
                IntSet leftConnections = getAdjacency(leftNode).keySet();
                if (leftConnections.size() != 2)
                    break;
                IntIterator leftIt = leftConnections.iterator();
                SimulationNode leftPrevNode = builder.getNode(leftIt.nextInt());
                SimulationNode leftNextNode = builder.getNode(leftIt.nextInt());
                SimulationNode newLeftNode = (prevLeftNode.equals(leftPrevNode)) ? leftNextNode : leftPrevNode;
                if (getAdjacency(nodeChain.getLast()).containsKey(newLeftNode.ordinal))
                    break;
                ElectricalProperties p = getAdjacency(newLeftNode).get(leftNode.ordinal);
                if (!p.canDissolve(optimizationPass))
                    break;
                prevLeftNode = leftNode;
                leftNode = newLeftNode;
                nodeChain.addFirst(leftNode);
                leftNode.dissolveState = DISSOLVED;
                resistanceChain.addFirst(p);
                if (!p.isSimpleResistor())
                    purelyResistive = false;
            }

            SimulationNode rightNode = nextNode;
            SimulationNode prevRightNode = node;
            while (true) {
                if (rightNode.dissolveState != TO_DISSOLVE)
                    break;
                IntSet rightConnections = getAdjacency(rightNode).keySet();
                if (rightConnections.size() != 2)
                    break;
                IntIterator rightIt = rightConnections.iterator();
                SimulationNode rightPrevNode = builder.getNode(rightIt.nextInt());
                SimulationNode rightNextNode = builder.getNode(rightIt.nextInt());
                SimulationNode newRightNode = (prevRightNode.equals(rightPrevNode)) ? rightNextNode : rightPrevNode;
                if (getAdjacency(nodeChain.getFirst()).containsKey(newRightNode.ordinal))
                    break;
                ElectricalProperties p = getAdjacency(rightNode).get(newRightNode.ordinal);
                if (!p.canDissolve(optimizationPass))
                    break;
                prevRightNode = rightNode;
                rightNode = newRightNode;
                nodeChain.addLast(rightNode);
                rightNode.dissolveState = DISSOLVED;
                resistanceChain.addLast(p);
                if (!p.isSimpleResistor())
                    purelyResistive = false;
            }
            result = true;
            IDissolvedProperties p = purelyResistive ? new DissolvedProperties(nodeChain, resistanceChain) :
                    new AdvancedDissolvedProperties(nodeChain, resistanceChain);
            Int2ObjectMap<ElectricalProperties> leftAdjacency = overrideAdjacency(leftNode);
            Int2ObjectMap<ElectricalProperties> rightAdjacency = overrideAdjacency(rightNode);
            leftAdjacency.remove(prevLeftNode.ordinal);
            rightAdjacency.remove(prevRightNode.ordinal);
            leftAdjacency.put(rightNode.ordinal, ((ElectricalProperties) p));
            rightAdjacency.put(leftNode.ordinal, ((ElectricalProperties) p).invert());

            // remove the nodes in the middle of the node chain
            int i = 0;
            for (SimulationNode toRemove : nodeChain) {
                if (i == 0 || i == nodeChain.size() - 1) {
                    i++;
                    continue;
                }
                for (int neighbor : getAdjacency(toRemove).keySet())
                    overrideAdjacency(builder.getNode(neighbor)).remove(toRemove.ordinal);
                overrideAdjacency(toRemove).clear();
                network.allNodes.remove(toRemove);
                i++;
            }

            network.optimizations.push(new SimpleTopologyOptimizationEntry(p, nodeChain.getFirst().ordinal, nodeChain.getLast().ordinal));
        }

        return result;
    }

    private boolean optimizeCoupledProperties(SimulationNode node, CoupledProperties cp, SimulationNode prevNode, SimulationNode nextNode, Deque<ElectricalProperties> resistanceChain) {
        int prevDegree = getAdjacency(prevNode).size();
        int nextDegree = getAdjacency(nextNode).size();
        if (!(prevDegree == 2 || nextDegree == 2))
            return false;

        SimulationNode primaryLeft;
        SimulationNode primaryRight;
        if (cp.nodes().node1().equals(prevNode.node)) {
            // n1 -- prev -- left
            primaryLeft = builder.getNode(cp.coupledNodes().node1());
            // n2 -- next -- right
            primaryRight = builder.getNode(cp.coupledNodes().node2());

        } else {
            // n1 -- next -- right
            primaryLeft = builder.getNode(cp.coupledNodes().node2());
            // n2 -- prev -- left
            primaryRight = builder.getNode(cp.coupledNodes().node1());
        }

        ElectricalProperties leftProperties = resistanceChain.getFirst();
        ElectricalProperties rightProperties = resistanceChain.getLast();

        byte mode = prevDegree == 2 ?
                nextDegree == 2 ?
                CoupledPropertiesOptimizationEntry.MODE_NO_BRANCH :
                CoupledPropertiesOptimizationEntry.MODE_RIGHT_BRANCH :
                CoupledPropertiesOptimizationEntry.MODE_LEFT_BRANCH;

        if (!leftProperties.canDissolve(optimizationPass) || !rightProperties.canDissolve(optimizationPass)) {
            // can't dissolve
            return false;
        } else if (leftProperties.isSimpleResistor() && rightProperties.isSimpleResistor()) {
                // just simple resistor
                double leftResistance = resistanceChain.getFirst().resistance();
                double rightResistance = resistanceChain.getLast().resistance();

                double ratio = cp.isPrimary() ? cp.ratio() : 1 / cp.ratio();
                double secondaryResistance = leftResistance + rightResistance;
                double replacementResistance = secondaryResistance / ratio / ratio;

                network.optimizations.push(new CoupledPropertiesOptimizationEntry(
                        mode,
                        prevNode.ordinal, node.ordinal, nextNode.ordinal,
                        primaryLeft.ordinal, primaryRight.ordinal,
                        replacementResistance,
                        leftResistance,
                        rightResistance,
                        ratio));

                ElectricalProperties newResistance = ElectricalProperties.resistor(replacementResistance);

                overrideAdjacency(primaryLeft).put(primaryRight.ordinal, newResistance);
                overrideAdjacency(primaryRight).put(primaryLeft.ordinal, newResistance);
        } else  {
                double ratio = cp.isPrimary() ? cp.ratio() : 1 / cp.ratio();

            AdvancedCoupledDissolvedProperties replacement = new AdvancedCoupledDissolvedProperties(ratio,
                        List.of(prevNode, node, nextNode), List.of(leftProperties, rightProperties));

                network.optimizations.push(new AdvancedCoupledPropertiesOptimizationEntry(
                        mode,
                        prevNode.ordinal, node.ordinal, nextNode.ordinal,
                        primaryLeft.ordinal, primaryRight.ordinal,
                        replacement,
                        ratio));

                overrideAdjacency(primaryLeft).put(primaryRight.ordinal, replacement);
                overrideAdjacency(primaryRight).put(primaryLeft.ordinal, replacement.invert());
        }

        if (prevDegree == 2)
            network.allNodes.remove(prevNode);
        else {
            Int2ObjectMap<ElectricalProperties> adjacency = overrideAdjacency(prevNode);
            adjacency.remove(node.ordinal);
            adjacency.remove(nextNode.ordinal);
        }
        network.allNodes.remove(node);
        if (nextDegree == 2)
            network.allNodes.remove(nextNode);
        else {
            Int2ObjectMap<ElectricalProperties> adjacency = overrideAdjacency(nextNode);
            adjacency.remove(node.ordinal);
            adjacency.remove(prevNode.ordinal);
        }

        return true;
    }

    private static Int2ObjectMap<ElectricalProperties> getAdjacency(SimulationNode node) {
        return node.localAdjacencyOverride == null ? node.adjacency : node.localAdjacencyOverride;
    }

    private static Int2ObjectMap<ElectricalProperties> overrideAdjacency(SimulationNode node) {
        if (node.localAdjacencyOverride == null) {
            node.localAdjacencyOverride = new Int2ObjectArrayMap<>(node.adjacency.size());
            node.localAdjacencyOverride.putAll(node.adjacency);
        }

        return node.localAdjacencyOverride;
    }

    /**
     * @return {@code true} if couldn't optimize further
     */
    public boolean runOptimizationPass() {
        return !seriesOptimize() &
                !starToDeltaOptimize();
    }
}
