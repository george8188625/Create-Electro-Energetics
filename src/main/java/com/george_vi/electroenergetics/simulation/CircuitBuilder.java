package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.simulator.CoupledProperties;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.MicroTickingElectricalProperties;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.*;

public class CircuitBuilder {
    List<WrappedIndexedNode> allIndexedNodes;
    Object2IntMap<Node> nodeIndexes;
    Int2IntMap defaultZeroPotentials;  // K -> node id, V -> priority
    public Long2ObjectMap<MicroTickingElectricalProperties> microTickers = new Long2ObjectOpenHashMap<>();
    int id = 0;

    public CircuitBuilder(Set<InWorldNode> nodes) {
        allIndexedNodes = new ArrayList<>(nodes.size() * 2);
        nodeIndexes = new Object2IntOpenHashMap<>(nodes.size() * 2);
        nodeIndexes.defaultReturnValue(-1);
        defaultZeroPotentials = new Int2IntOpenHashMap();
        for (Node node : nodes) {
            WrappedIndexedNode indexedNode = new WrappedIndexedNode(node, id);
            allIndexedNodes.add(indexedNode);
            nodeIndexes.put(node, id);
            id++;
        }
    }

    public ElectricalProperties getConnectionProperties(int n1, int n2) {
        checkOutOfBounds(n1);
        checkOutOfBounds(n2);

        WrappedIndexedNode indexedNode1 = allIndexedNodes.get(n1);
        return indexedNode1.adjacency.get(n2);
    }

    public ElectricalProperties getConnectionProperties(WrappedIndexedNode n1, WrappedIndexedNode n2) {
        return n1.adjacency.get(n2.ordinal);
    }

    public ElectricalProperties getConnectionProperties(Node n1, Node n2) {
        int nodeId1 = nodeIndexes.getInt(n1);
        int nodeId2 = nodeIndexes.getInt(n2);
        if (nodeId1 == -1 || nodeId2 == -1)
            return ElectricalProperties.ZERO_CONDUCTANCE;
        return getConnectionProperties(nodeId1, nodeId2);
    }

    public void connect(int n1, int n2, ElectricalProperties properties) {
        checkOutOfBounds(n1);
        checkOutOfBounds(n2);
        if (n1 == n2) {
            if (FMLEnvironment.production)
                return;
            else
                throw new IllegalArgumentException("Tried to create an electrical connection between a single node: " + allIndexedNodes.get(n1).node);
        }
        if (Double.isNaN(properties.resistance()) || Double.isNaN(properties.voltageSource()) || Double.isNaN(properties.currentSource()))
            return;

        WrappedIndexedNode indexedNode1 = allIndexedNodes.get(n1);
        WrappedIndexedNode indexedNode2 = allIndexedNodes.get(n2);
        if (properties instanceof MicroTickingElectricalProperties ep)
            microTickers.put(DataPacker.pack(n1, n2), ep);

        if (properties instanceof CoupledProperties cp && cp.isPrimary()) { // Mark the node so it's solved in the same circuit as the coupled nodes
            WrappedIndexedNode n = getNode(cp.coupledNodes().node1());
            if (n != null)
                indexedNode1.invisibleAdjacency.add(n.ordinal);
        }

        indexedNode1.adjacency.put(n2, properties);
        indexedNode2.adjacency.put(n1, properties.invert());
    }

    public void connect(WrappedIndexedNode n1, WrappedIndexedNode n2, ElectricalProperties properties) {
        if (Double.isNaN(properties.resistance()) || Double.isNaN(properties.voltageSource()) || Double.isNaN(properties.currentSource()))
            return;
        if (properties instanceof MicroTickingElectricalProperties ep)
            microTickers.put(DataPacker.pack(n1.ordinal, n2.ordinal), ep);

        if (properties instanceof CoupledProperties cp && cp.isPrimary()) { // Mark the node so it's solved in the same circuit as the coupled nodes
            WrappedIndexedNode n = getNode(cp.coupledNodes().node1());
            if (n != null)
                n1.invisibleAdjacency.add(n.ordinal);
        }

        n1.adjacency.put(n2.ordinal, properties);
        n2.adjacency.put(n1.ordinal, properties.invert());
    }

    public void connect(Node n1, Node n2, ElectricalProperties properties) {
        int i1 = nodeIndexes.getInt(n1);
        if (i1 == -1)
            i1 = addNode(n1).ordinal;

        int i2 = nodeIndexes.getInt(n2);
        if (i2 == -1)
            i2 = addNode(n2).ordinal;

        connect(i1, i2, properties);
    }

    public void ground(int node, double conductance) {
        checkOutOfBounds(node);

        allIndexedNodes.get(node).groundConductance = conductance;
    }

    public void ground(WrappedIndexedNode node, double conductance) {
        node.groundConductance = conductance;
    }

    public void ground(Node node, double conductance) {
        ground(nodeIndexes.getInt(node), conductance);
    }

    public void defaultZeroPotential(int node, int priority) {
        checkOutOfBounds(node);
        defaultZeroPotentials.put(node, priority);
    }

    public void defaultZeroPotential(WrappedIndexedNode node, int priority) {
        defaultZeroPotentials.put(node.ordinal, priority);
    }

    public void defaultZeroPotential(Node node, int priority) {
        defaultZeroPotential(nodeIndexes.getInt(node), priority);
    }

    public WrappedIndexedNode addNode(Node node) {
        WrappedIndexedNode indexedNode = new WrappedIndexedNode(node, id);
        allIndexedNodes.add(indexedNode);
        nodeIndexes.put(node, id);
        id++;
        return indexedNode;
    }

    List<Set<WrappedIndexedNode>> allNetworks;
    public List<Set<WrappedIndexedNode>> dfs() {
        List<Set<WrappedIndexedNode>> allNetworks = new ArrayList<>(allIndexedNodes.size());
        boolean[] visited = new boolean[allIndexedNodes.size()];
        for (int i = 0; i < allIndexedNodes.size(); i++) {
            if (visited[i])
                continue;
            visited[i] = true;
            WrappedIndexedNode node = allIndexedNodes.get(i);
            Set<WrappedIndexedNode> networkNodes = new HashSet<>();
            networkNodes.add(node);
            dfsInner(node, visited, networkNodes, false);
            allNetworks.add(networkNodes);
        }
        NetworksLoop:
        for (Set<WrappedIndexedNode> networkNodes : allNetworks) {
            WrappedIndexedNode highestPriorityGround = null;
            int highestPriority = Integer.MIN_VALUE;

            for (WrappedIndexedNode node : networkNodes) {
                if (node.groundConductance != 0d)
                    continue NetworksLoop;

                int priority = defaultZeroPotentials.get(node.ordinal);
                if (priority == highestPriority) {
                  if (highestPriorityGround == null || highestPriorityGround.node.hashCode() > node.node.hashCode())
                      highestPriorityGround = node;
                } else if (priority > highestPriority) {
                    highestPriorityGround = node;
                    highestPriority = priority;
                }
            }
            if (highestPriorityGround != null)
                highestPriorityGround.groundConductance = 1000d;
        }
        // So wasteful, I know...
        allNetworks.clear();
        Arrays.fill(visited, false);
        for (int i = 0; i < allIndexedNodes.size(); i++) {
            if (visited[i])
                continue;
            visited[i] = true;
            WrappedIndexedNode node = allIndexedNodes.get(i);
            Set<WrappedIndexedNode> networkNodes = new HashSet<>();
            networkNodes.add(node);
            dfsInner(node, visited, networkNodes, true);
            allNetworks.add(networkNodes);
        }
        this.allNetworks = allNetworks;
        return allNetworks;
    }

    private void dfsInner(WrappedIndexedNode startNode, boolean[] visited, Set<WrappedIndexedNode> networkNodes, boolean invis) {
        Deque<WrappedIndexedNode> stack = new ArrayDeque<>();
        stack.push(startNode);
        while (!stack.isEmpty()) {
            WrappedIndexedNode node = stack.pop();
            for (int i : node.adjacency.keySet()) {
                if (visited[i])
                    continue;
                visited[i] = true;
                WrappedIndexedNode adjacentNode = allIndexedNodes.get(i);
                networkNodes.add(adjacentNode);
                stack.push(adjacentNode);
            }

            if (invis)
                for (int i : node.invisibleAdjacency) {
                    if (visited[i])
                        continue;
                    visited[i] = true;
                    WrappedIndexedNode adjacentNode = allIndexedNodes.get(i);
                    networkNodes.add(adjacentNode);
                    stack.push(adjacentNode);
                }
        }
    }

    private void checkOutOfBounds(int id) {
        if (id >= allIndexedNodes.size())
            throw new IllegalArgumentException("(Indexed) node of ID: " + id + " doesn't exist");
    }

    public List<WrappedIndexedNode> allNodes() {
        return allIndexedNodes;
    }

    public WrappedIndexedNode getNode(int id) {
        checkOutOfBounds(id);
        return allIndexedNodes.get(id);
    }

    public WrappedIndexedNode getNode(Node node) {
        int i = nodeIndexes.getInt(node);
        if (i == -1)
            return null;
        return allIndexedNodes.get(i);
    }
}
