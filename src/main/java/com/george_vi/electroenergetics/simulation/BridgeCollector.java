package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.simulation.simulator.ElectricalNodeConnection;
import net.minecraft.core.BlockPos;

import java.util.List;

public class BridgeCollector {
    final List<ElectricalNodeConnection> bridges;
    final List<Node> nodes;

    public BridgeCollector(List<ElectricalNodeConnection> bridges, List<Node> nodes) {
        this.bridges = bridges;
        this.nodes = nodes;
    }

    public void bridge(Node node1, Node node2, double resistance, double voltageSource) {
        if (node1.equals(node2))
            return;
        if (resistance == 0)
            throw new IllegalArgumentException("Resistance can't be zero!");
        bridges.add(new ElectricalNodeConnection(node1, node2, resistance, voltageSource));
    }

    public void addInternalNode(int id, BlockPos pos) {
        nodes.add(new Node(id, pos));
    }

    public Builder builder(BlockPos pos) {
        return new Builder(this, pos);
    }

    public static class Builder {
        final BridgeCollector collector;
        final BlockPos pos;
        int i = 0;

        Builder(BridgeCollector collector, BlockPos pos) {
            this.collector = collector;
            this.pos = pos;
        }

        public Builder voltageSourceWithResistance(int n1, int n2, double resistance, double voltage) {
            collector.addInternalNode(1000 + i, pos);
            collector.bridge(new Node(n1, pos), new Node(1000 + i, pos), 99999999, voltage);
            collector.bridge(new Node(1000 + i, pos), new Node(n2, pos), resistance, 0);
            i++;
            return this;
        }

        public Builder energyLimitedSource(int n1, int n2, double energy, double voltage) {
            double resistance;
            if (voltage == 0 || energy < 0)
                resistance = 0.01;
            else
                resistance = (voltage * voltage) / (4 * energy);

            collector.addInternalNode(1000 + i, pos);
            collector.bridge(new Node(n1, pos), new Node(1000 + i, pos), 99999, energy <= 0 ? 0 : voltage);
            collector.bridge(new Node(1000 + i, pos), new Node(n2, pos), resistance + 0.5, 0);
            i++;
            return this;
        }

        public Builder idealVoltageSource(int n1, int n2, double voltage) {
            collector.bridge(new Node(n1, pos), new Node(n2, pos), 99999999, voltage);
            return this;
        }

        public Builder node(int id) {
            collector.addInternalNode(id, pos);
            return this;
        }

        public Builder resistor(int n1, int n2, double resistance) {
            collector.bridge(new Node(n1, pos), new Node(n2, pos), resistance, 0);
            return this;
        }
    }
}
