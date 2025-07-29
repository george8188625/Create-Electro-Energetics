package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DissolvedProperties extends ElectricalProperties {
    final List<Node> originalNodes;
    final List<Double> originalResistances;

    public DissolvedProperties(List<Node> originalNodes, List<Double> originalResistances) {
        super(originalResistances.stream().mapToDouble(d -> d).sum(), 0);
        this.originalNodes = originalNodes;
        this.originalResistances = originalResistances;
    }

    public Map<Node, Double> getVoltages(double v1, double v2) {
        Node startNode = originalNodes.get(0);
        Map<Node, Double> voltages = new HashMap<>();
        voltages.put(startNode, v1);

        double totalResistance = originalResistances.stream().mapToDouble(c -> c).sum();
        double current = (v1 - v2) / totalResistance;

        double currentVoltage = v1;
        for (int i = 0; i < originalResistances.size(); i++) {
            Node prevNode = originalNodes.get(i);
            Node nextNode = originalNodes.get(i + 1);

            double voltageDrop = current * originalResistances.get(i);
            currentVoltage = currentVoltage - voltageDrop;

            voltages.put(nextNode, currentVoltage);
        }

        return voltages;
    }
}
