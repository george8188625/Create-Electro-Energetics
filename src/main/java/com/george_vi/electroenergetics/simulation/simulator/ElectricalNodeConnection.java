package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.NodeConnection;

import java.util.Objects;

public class ElectricalNodeConnection extends NodeConnection {
    public final ElectricalProperties electricalProperties;

    public ElectricalNodeConnection(InWorldNode node1, InWorldNode node2, double resistance, double voltageSource, double currentSource) {
        this(node1, node2, new ElectricalProperties(resistance, voltageSource, currentSource));
    }

    public ElectricalNodeConnection(InWorldNode node1, InWorldNode node2, ElectricalProperties properties) {
        super(node1, node2);
        electricalProperties = properties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1(), node2());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeConnection that = (NodeConnection) o;
        return Objects.equals(node1(), that.node1()) && Objects.equals(node2(), that.node2());
    }
}
