package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.DirectionSensitiveNodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SimulationResults {
    Map<Node, Double> voltages;
    Map<DirectionSensitiveNodeConnection, Double> sourceAmps;
    Map<Node, List<Node>> adjacency;
    Map<DirectionSensitiveNodeConnection, ElectricalProperties> connectionProperties;
    InfrastructureSavedData sd;

    public SimulationResults(Map<Node, Double> voltages, Map<DirectionSensitiveNodeConnection, Double> sourceAmps, Map<Node, List<Node>> adjacency,
                             Map<DirectionSensitiveNodeConnection, ElectricalProperties> connectionProperties, InfrastructureSavedData sd) {
        this.voltages = voltages;
        this.sourceAmps = sourceAmps;
        this.adjacency = adjacency;
        this.connectionProperties = connectionProperties;
        this.sd = sd;
    }

    public double getVoltageAt(Node node) {
        Double v = voltages.get(node);
        if (v == null)
            v = sd.getVoltageAt(node);
        return v;
    }

    public double getVoltageAt(BlockPos pos, int id) {
        return getVoltageAt(new Node(id, pos));
    }

    public double getCurrentThrough(Node node1, Node node2) {
        DirectionSensitiveNodeConnection fc = connectionBetween(node1, node2);
        node1 = fc.node1();
        node2 = fc.node2();

        if (sourceAmps.containsKey(new DirectionSensitiveNodeConnection(node1, node2)))
            return sourceAmps.get(new DirectionSensitiveNodeConnection(node1, node2));
        if (sourceAmps.containsKey(new DirectionSensitiveNodeConnection(node2, node1)))
            return -sourceAmps.get(new DirectionSensitiveNodeConnection(node2, node1));
        ElectricalProperties properties = connectionProperties.get(new DirectionSensitiveNodeConnection(node1, node2));
        if (properties == null || properties.resistance() == 0)
            return 0;
        double v1 = getVoltageAt(node1);
        double v2 = getVoltageAt(node2);

        return (v1 - v2) / properties.resistance();
    }

    public double getCurrentThrough(BlockPos pos, int id1, int id2) {
        return getCurrentThrough(new Node(id1, pos), new Node(id2, pos));
    }

    public double getHeatLoss(Node node1, Node node2) {
        double current = getCurrentThrough(node1, node2);
        ElectricalProperties properties = connectionProperties.get(new DirectionSensitiveNodeConnection(node1, node2));
        if (current == 0 || properties == null || properties.resistance() == 0 || properties.currentSource() != 0 || properties.voltageSource() != 0)
            return 0;

        return current * current * properties.resistance();
    }

    DirectionSensitiveNodeConnection connectionBetween(Node node1, Node node2) {

        // the reason this is here, is when a device creates a non-ideal voltage source, it adds a node in the middle.
        // this just returns the real connection, so that the device doesn't have to worry about these nodes.

        List<Node> node1connections = adjacency.get(node1);
        List<Node> node2connections = adjacency.get(node2);
        if (node1connections == null || node2connections == null ||
            node1connections.contains(node2))
            return new DirectionSensitiveNodeConnection(node1, node2);

        List<Node> nodesInTheMiddle = new ArrayList<>();
        for (Node node1connection : node1connections) {
            for (Node node2connection : node2connections) {
                if (node1connection.equals(node2connection))
                    nodesInTheMiddle.add(node1connection);
            }
        }
        if (nodesInTheMiddle.size() == 1)
            return new DirectionSensitiveNodeConnection(node1, nodesInTheMiddle.getFirst());
        return new DirectionSensitiveNodeConnection(node1, node2);
    }

}
