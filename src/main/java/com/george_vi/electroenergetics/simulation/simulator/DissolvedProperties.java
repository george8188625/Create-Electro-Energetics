package com.george_vi.electroenergetics.simulation.simulator;

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

    public void getVoltages(double v1, double v2, double[] toFill, int microTickBits, int microTick) {
        double totalResistance = originalResistances.stream().mapToDouble(c -> c).sum();
        double current = (v1 - v2) / totalResistance;

        double currentVoltage = v1;
        for (int i = 0; i < originalResistances.size(); i++) {
            WrappedIndexedNode nextNode = originalNodes.get(i + 1);

            double voltageDrop = current * originalResistances.get(i);
            currentVoltage = currentVoltage - voltageDrop;

            toFill[(nextNode.ordinal << microTickBits) | microTick] = currentVoltage;

        }
    }

    @Override
    public ElectricalProperties invert() {
        return this;
    }
}
