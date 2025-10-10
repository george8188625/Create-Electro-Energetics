package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalNodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public class BridgeCollector {
    final List<ElectricalNodeConnection> bridges;
    final List<InWorldNode> nodes;
    final Map<Node, Double> groundConductance;
    final Map<Node, Integer> defaultZeroPotentials;

    public BridgeCollector(List<ElectricalNodeConnection> bridges, List<InWorldNode> nodes, Map<Node, Double> groundConductance, Map<Node, Integer> defaultZeroPotentials) {
        this.bridges = bridges;
        this.nodes = nodes;
        this.groundConductance = groundConductance;
        this.defaultZeroPotentials = defaultZeroPotentials;
    }

    public void bridge(InWorldNode node1, InWorldNode node2, double resistance, double voltageSource, double currentSource) {
        bridge(node1, node2, new ElectricalProperties(resistance, voltageSource, currentSource));
    }

    public void bridge(InWorldNode node1, InWorldNode node2, ElectricalProperties electricalProperties) {
        // Don't Bridge a node to itself, Don't accept any NaN values, that can mess up entire grids
        if (node1.equals(node2) || Double.isNaN(electricalProperties.resistance()) || Double.isNaN(electricalProperties.voltageSource()) || Double.isNaN(electricalProperties.currentSource()))
            return;
        if (electricalProperties.resistance() == 0)
            throw new IllegalArgumentException("Resistance can't be zero!");
        bridges.add(new ElectricalNodeConnection(node1, node2, electricalProperties));
    }

    public void ground(InWorldNode inWorldNode, double conductance) {
        groundConductance.put(inWorldNode, conductance);
    }

    public void defaultZeroPotential(InWorldNode inWorldNode, int priority) {
        defaultZeroPotentials.put(inWorldNode, priority);
    }

    public void addInternalNode(int id, BlockPos pos) {
        nodes.add(new InWorldNode(id, pos));
    }

    public Builder builder(BlockPos pos) {
        return new Builder(this, pos);
    }

    public static class Builder {
        public final BridgeCollector collector;
        final BlockPos pos;
        int i = 0;

        Builder(BridgeCollector collector, BlockPos pos) {
            this.collector = collector;
            this.pos = pos;
        }

        public Builder voltageSourceWithResistance(int n1, int n2, double resistance, double voltage) {
            collector.addInternalNode(1000 + i, pos);
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(1000 + i, pos), 999999999, voltage, 0);
            collector.bridge(new InWorldNode(1000 + i, pos), new InWorldNode(n2, pos), resistance, 0, 0);
            collector.defaultZeroPotential(voltage > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 60);
            i++;
            return this;
        }

        public Builder energyLimitedSource(int n1, int n2, double energy, double voltage) {
            return energyLimitedSource(n1, n2, energy, voltage, 0);
        }

        public Builder energyLimitedSource(int n1, int n2, double energy, double voltage, double internalResistance) {
            double resistance;
            if (voltage == 0 || energy <= 0)
                resistance = 0.01;
            else
                resistance = (voltage * voltage) / (4 * energy);

            collector.addInternalNode(1000 + i, pos);
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(1000 + i, pos), 999999999, energy <= 0 ? 0 : voltage, 0);
            collector.bridge(new InWorldNode(1000 + i, pos), new InWorldNode(n2, pos), resistance + internalResistance, 0, 0);
            i++;
            collector.defaultZeroPotential(voltage > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 50);
            return this;
        }

        public Builder idealVoltageSource(int n1, int n2, double voltage) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), new ElectricalProperties(999999999, voltage, 0, true, false));
            collector.defaultZeroPotential(voltage > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 100);
            return this;
        }

        public Builder idealCurrentSource(int n1, int n2, double current) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), new ElectricalProperties(999999999, 0, current, false, true));
            collector.defaultZeroPotential(current > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 100);
            return this;
        }

        public Builder node(int id) {
            collector.addInternalNode(id, pos);
            return this;
        }

        public Builder resistor(int n1, int n2, double resistance) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), resistance, 0, 0);
            return this;
        }

        public Builder connect(int n1, int n2, ElectricalProperties properties) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), properties);
            collector.defaultZeroPotential((properties.isVoltageSource() ? properties.voltageSource() : properties.currentSource()) > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 100);
            return this;
        }

        public void ground(int id, double conductance) {
            collector.ground(new InWorldNode(id, pos), conductance);
        }
    }
}
