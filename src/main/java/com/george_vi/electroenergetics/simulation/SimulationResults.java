package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.simulator.DirectionalNodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SimulationResults {
    Object2DoubleMap<Node> voltages;
    Object2DoubleMap<DirectionalNodeConnection> sourceAmps;
    CircuitBuilder circuitBuilder;
    InfrastructureSavedData sd;

    public SimulationResults(Object2DoubleMap<Node> voltages, Object2DoubleMap<DirectionalNodeConnection> sourceAmps, CircuitBuilder circuitBuilder, InfrastructureSavedData sd) {
        this.voltages = voltages;
        this.sourceAmps = sourceAmps;
        this.circuitBuilder = circuitBuilder;
        this.sd = sd;
    }

    public InfrastructureSavedData getInfrastructure() {
        return sd;
    }

    public double getVoltageAt(Node node) {
        return voltages.getOrDefault(node, 0d);
    }

    public double getVoltageAt(BlockPos pos, int id) {
        return getVoltageAt(new InWorldNode(id, pos));
    }

    public double getCurrentThrough(Node node1, Node node2) {
        DirectionalNodeConnection fc = connectionBetween(node1, node2);
        node1 = fc.node1();
        node2 = fc.node2();

        if (sourceAmps.containsKey(new DirectionalNodeConnection(node1, node2)))
            return sourceAmps.getDouble(new DirectionalNodeConnection(node1, node2));
        if (sourceAmps.containsKey(new DirectionalNodeConnection(node2, node1)))
            return -sourceAmps.getDouble(new DirectionalNodeConnection(node2, node1));
        ElectricalProperties properties = circuitBuilder.getConnectionProperties(node1, node2);
        if (properties == null || properties.resistance() == 0)
            return 0;
        double v1 = getVoltageAt(node1);
        double v2 = getVoltageAt(node2);

        return (v1 - v2) / properties.resistance();
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

        Set<Node> node1connections = circuitBuilder.getAdjacentNodes(node1).keySet();
        Set<Node> node2connections = circuitBuilder.getAdjacentNodes(node2).keySet();
        if (node1connections.contains(node2))
            return new DirectionalNodeConnection(node1, node2);

        List<Node> nodesInTheMiddle = new ArrayList<>();
        for (Node node1connection : node1connections) {
            for (Node node2connection : node2connections) {
                if (node1connection.equals(node2connection))
                    nodesInTheMiddle.add(node1connection);
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
        return getVoltageAt(n1) - getVoltageAt(n2);
    }
}
