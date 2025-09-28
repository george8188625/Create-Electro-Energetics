package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;

public class ElectricalNodeConnection extends NodeConnection {
    public final ElectricalProperties electricalProperties;

    public ElectricalNodeConnection(Node node1, Node node2, double resistance, double voltageSource, double currentSource) {
        this(node1, node2, new ElectricalProperties(resistance, voltageSource, currentSource));
    }

    public ElectricalNodeConnection(Node node1, Node node2, ElectricalProperties properties) {
        super(node1, node2);
        electricalProperties = properties;
    }
}
