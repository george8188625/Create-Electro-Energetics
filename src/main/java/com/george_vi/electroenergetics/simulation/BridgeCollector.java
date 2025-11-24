package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.minecraft.core.BlockPos;

public class BridgeCollector {
    final InfrastructureSavedData sd;
    final CircuitBuilder circuitBuilder;

    public BridgeCollector(CircuitBuilder circuitBuilder, InfrastructureSavedData sd) {
        this.sd = sd;
        this.circuitBuilder = circuitBuilder;
    }


    public void bridge(InWorldNode node1, InWorldNode node2, double resistance, double voltageSource, double currentSource) {
        bridge(node1, node2, new ElectricalProperties(resistance, voltageSource, currentSource));
    }

    public void bridge(InWorldNode node1, InWorldNode node2, ElectricalProperties electricalProperties) {
        circuitBuilder.connect(node1, node2, electricalProperties);
    }

    public void ground(InWorldNode node, double conductance) {
        circuitBuilder.ground(node, conductance);
    }

    public void defaultZeroPotential(InWorldNode node, int priority) {
        circuitBuilder.defaultZeroPotential(node, priority);
    }

    public void addInternalNode(int id, BlockPos pos) {
        circuitBuilder.addNode(new InWorldNode(id, pos));
    }

    public void addInternalNode(InWorldNode node) {
        circuitBuilder.addNode(node);
    }

    public InfrastructureSavedData getSD() {
        return sd;
    }

    public CircuitBuilder getCircuitBuilder() {
        return circuitBuilder;
    }

    public double getTimeStep() {
        return 0.05d;
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
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(1000 + i, pos), 10e+11d, voltage, 0);
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
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(1000 + i, pos), 10e+11d, energy <= 0 ? 0 : voltage, 0);
            collector.bridge(new InWorldNode(1000 + i, pos), new InWorldNode(n2, pos), resistance + internalResistance, 0, 0);
            i++;
            collector.defaultZeroPotential(voltage > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 50);
            return this;
        }

        public Builder idealVoltageSource(int n1, int n2, double voltage) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), new ElectricalProperties(10e+11d, voltage, 0, true, false));
            return this;
        }

        public Builder idealCurrentSource(int n1, int n2, double current) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), new ElectricalProperties(10e+11d, 0, current, false, true));
            return this;
        }

        public Builder voltageSourceWithResistance(InWorldNode n1, InWorldNode n2, double resistance, double voltage) {
            collector.addInternalNode(1000 + i, pos);
            collector.bridge(n1, new InWorldNode(1000 + i, pos), 10e+11d, voltage, 0);
            collector.bridge(new InWorldNode(1000 + i, pos), n2, resistance, 0, 0);
            collector.defaultZeroPotential(voltage > 0 ? n1 : n2, 60);
            i++;
            return this;
        }

        public Builder energyLimitedSource(InWorldNode n1, InWorldNode n2, double energy, double voltage) {
            return energyLimitedSource(n1, n2, energy, voltage, 0);
        }

        public Builder energyLimitedSource(InWorldNode n1, InWorldNode n2, double energy, double voltage, double internalResistance) {
            double resistance;
            if (Math.abs(voltage) < 0.001 || energy <= 0.01)
                voltage = 0;
            if (voltage == 0)
                resistance = 0.001;
            else
                resistance = (voltage * voltage) / (4 * energy);

            collector.addInternalNode(1000 + i, pos);
            collector.bridge(n1, new InWorldNode(1000 + i, pos), 10e+11d, energy <= 0 ? 0 : voltage, 0);
            collector.bridge(new InWorldNode(1000 + i, pos), n2, resistance + internalResistance, 0, 0);
            i++;
            collector.defaultZeroPotential(voltage > 0 ? n1 : n2, 50);
            return this;
        }

        public Builder idealVoltageSource(InWorldNode n1, InWorldNode n2, double voltage) {
            collector.bridge(n1, n2, new ElectricalProperties(10e+11d, voltage, 0, true, false));
            collector.defaultZeroPotential(voltage > 0 ? n1 : n2, 100);
            return this;
        }

        public Builder idealCurrentSource(InWorldNode n1, InWorldNode n2, double current) {
            collector.bridge(n1, n2, new ElectricalProperties(10e+11d, 0, current, false, true));
            collector.defaultZeroPotential(current > 0 ? n1 : n2, 100);
            return this;
        }

        public Builder node(int id) {
            collector.addInternalNode(id, pos);
            return this;
        }

        public Builder node(InWorldNode node) {
            collector.addInternalNode(node);
            return this;
        }

        public Builder resistor(int n1, int n2, double resistance) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), resistance, 0, 0);
            return this;
        }

        public Builder resistor(InWorldNode n1, InWorldNode n2, double resistance) {
            collector.bridge(n1, n2, resistance, 0, 0);
            return this;
        }

        public Builder connect(int n1, int n2, ElectricalProperties properties) {
            collector.bridge(new InWorldNode(n1, pos), new InWorldNode(n2, pos), properties);
            collector.defaultZeroPotential((properties.isVoltageSource() ? properties.voltageSource() : properties.currentSource()) > 0 ? new InWorldNode(n1, pos) : new InWorldNode(n2, pos), 100);
            return this;
        }

        public Builder connect(InWorldNode n1, InWorldNode n2, ElectricalProperties properties) {
            collector.bridge(n1, n2, properties);
            collector.defaultZeroPotential((properties.isVoltageSource() ? properties.voltageSource() : properties.currentSource()) > 0 ? n1 : n2, 100);
            return this;
        }

        public void ground(int id, double conductance) {
            collector.ground(new InWorldNode(id, pos), conductance);
        }
    }
}
