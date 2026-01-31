package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;


public class SimulationResults {
    double[] voltages;
    Object2DoubleMap<DirectionalNodeConnection> sourceAmps;
    CircuitBuilder circuitBuilder;
    InfrastructureSavedData sd;
    final int microTicks;
    final int microTickBits;


    public SimulationResults(double[] voltages, int microTicks, int microTickBits, Object2DoubleMap<DirectionalNodeConnection> sourceAmps, CircuitBuilder circuitBuilder, InfrastructureSavedData sd) {
        this.voltages = voltages;
        this.sourceAmps = sourceAmps;
        this.circuitBuilder = circuitBuilder;
        this.sd = sd;
        this.microTicks = microTicks;
        this.microTickBits = microTickBits;
    }

    public InfrastructureSavedData getInfrastructure() {
        return sd;
    }

    public double getVoltageAt(Node node) {
        int nodeID = circuitBuilder.nodeIndexes.getInt(node);
        if (nodeID == -1)
            return 0;
        if (microTickBits == 0)
            return voltages[nodeID];

        int i = nodeID << microTickBits;
        double rms = 0;
        for (int j = 0; j < microTicks; j++)
            rms += voltages[i|j] * voltages[i|j];
        rms /= microTicks;
        rms = Math.sqrt(rms);
        return rms;
    }

    public double getVoltageAt(BlockPos pos, int id) {
        return getVoltageAt(new InWorldNode(id, pos));
    }

    public double getCurrentThrough(Node node1, Node node2) {
        DirectionalNodeConnection fc = connectionBetween(node1, node2);
        node1 = fc.node1();
        node2 = fc.node2();

        if (sourceAmps.containsKey(fc))
            return sourceAmps.getDouble(fc);
        if (sourceAmps.containsKey(fc.invert()))
            return -sourceAmps.getDouble(fc.invert());
        ElectricalProperties properties = circuitBuilder.getConnectionProperties(node1, node2);
        if (properties == null || properties.resistance() == 0)
            return 0;
        return getVoltageAt(node1, node2) / properties.resistance();
    }

    public double getCurrentThrough(BlockPos pos, int id1, int id2) {
        return getCurrentThrough(new InWorldNode(id1, pos), new InWorldNode(id2, pos));
    }
    public double getHeatLoss(BlockPos pos, int id1, int id2) {
        return getHeatLoss(new InWorldNode(id1, pos), new InWorldNode(id2, pos));
    }

    public double getHeatLoss(Node node1, Node node2) {
        double current = getCurrentThrough(node1, node2);
        ElectricalProperties properties = circuitBuilder.getConnectionProperties(node1, node2);
        if (current == 0 || properties == null || properties.resistance() == 0 || properties.currentSource() != 0 || properties.voltageSource() != 0)
            return 0;

        return current * current * properties.resistance();
    }

    DirectionalNodeConnection connectionBetween(Node node1, Node node2) {

        // the reason this is here, is when a device creates a non-ideal voltage source, it adds a node in the middle.
        // this just returns the real connection, so that the device doesn't have to worry about these nodes.
        int nodeId1 = circuitBuilder.nodeIndexes.getInt(node1);
        int nodeId2 = circuitBuilder.nodeIndexes.getInt(node2);
        if (nodeId1 == -1 || nodeId2 == -1)
            return new DirectionalNodeConnection(node1, node2);

        WrappedIndexedNode indexedNode1 = circuitBuilder.getNode(nodeId1);
        WrappedIndexedNode indexedNode2 = circuitBuilder.getNode(nodeId2);
        if (indexedNode1.adjacency.containsKey(indexedNode2.ordinal))
            return new DirectionalNodeConnection(node1, node2);

        IntSet node1connections = indexedNode1.adjacency.keySet();
        IntSet node2connections = indexedNode2.adjacency.keySet();

        List<Node> nodesInTheMiddle = new ArrayList<>();
        for (int node1connection : node1connections) {
            for (int node2connection : node2connections) {
                if (node1connection == node2connection)
                    nodesInTheMiddle.add(circuitBuilder.allIndexedNodes.get(node1connection).node);
            }
        }
        if (nodesInTheMiddle.size() == 1)
            return new DirectionalNodeConnection(node1, nodesInTheMiddle.getFirst());
        return new DirectionalNodeConnection(node1, node2);
    }

    public double getVoltageAt(BlockPos pos, int n1, int n2) {
        return getVoltageAt(new InWorldNode(n1, pos), new InWorldNode(n2, pos));
    }

    public double getVoltageAt(Node n1, Node n2) {
        int nodeId1 = circuitBuilder.nodeIndexes.getInt(n1);
        int nodeId2 = circuitBuilder.nodeIndexes.getInt(n2);
        if (nodeId1 == -1 || nodeId2 == -1)
            return 0;
        int id1 = nodeId1 << microTickBits;
        int id2 = nodeId2 << microTickBits;
        if (microTickBits == 0)
            return voltages[id1] - voltages[id2];
        double rms = 0;
        for (int j = 0; j < microTicks; j++)
            rms += (voltages[id1|j] - voltages[id2|j]) * (voltages[id1|j] - voltages[id2|j]);
        rms /= microTicks;
        rms = Math.sqrt(rms);
        return rms;
    }

    public double[] getVoltages(Node n1) {
        int nodeID = circuitBuilder.nodeIndexes.getInt(n1);
        if (nodeID == -1)
            return new double[0];
        int id = nodeID << microTickBits;
        double[] r = new double[microTicks];
        for (int j = 0; j < microTicks; j++)
            r[j] = voltages[id|j];
        return r;
    }
}
