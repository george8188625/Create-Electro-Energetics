package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;

public class ElectricalNodeConnection extends NodeConnection {
    public final ElectricalProperties electricalProperties;

    public ElectricalNodeConnection(Node node1, Node node2, double resistance, double voltageSource) {
        super(node1, node2);
        electricalProperties = new ElectricalProperties(resistance, voltageSource);
    }
}
