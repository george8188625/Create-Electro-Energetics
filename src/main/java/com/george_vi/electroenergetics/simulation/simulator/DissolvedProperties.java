package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;

import java.util.*;

public class DissolvedProperties extends ElectricalProperties {
    public final List<WrappedIndexedNode> originalNodes;
    public final List<Double> originalResistances;

    public DissolvedProperties(LinkedList<WrappedIndexedNode> originalNodes, List<Double> originalResistances) {
        super(originalResistances.stream().mapToDouble(d -> d).sum(), 0, 0);
        this.originalNodes = originalNodes;
        this.originalResistances = originalResistances;
    }

    public Map<WrappedIndexedNode, Double> getVoltages(double v1, double v2) {
        WrappedIndexedNode startNode = originalNodes.get(0);
        Map<WrappedIndexedNode, Double> voltages = new Object2DoubleArrayMap<>(originalNodes.size());
        voltages.put(startNode, v1);

        double totalResistance = originalResistances.stream().mapToDouble(c -> c).sum();
        double current = (v1 - v2) / totalResistance;

        double currentVoltage = v1;
        for (int i = 0; i < originalResistances.size(); i++) {
            WrappedIndexedNode prevNode = originalNodes.get(i);
            WrappedIndexedNode nextNode = originalNodes.get(i + 1);

            double voltageDrop = current * originalResistances.get(i);
            currentVoltage = currentVoltage - voltageDrop;

            voltages.put(nextNode, currentVoltage);
        }

        return voltages;
    }

    @Override
    public ElectricalProperties invert() {
        return this;
    }

    public Collection<WrappedIndexedNode> getMiddleNodes() {
        return originalNodes.subList(1, originalNodes.size() - 1);
    }
}
