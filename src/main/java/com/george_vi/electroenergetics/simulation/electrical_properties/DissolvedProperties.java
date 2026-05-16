package com.george_vi.electroenergetics.simulation.electrical_properties;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;

import java.util.ArrayList;
import java.util.List;

public class DissolvedProperties extends ElectricalProperties implements IDissolvedProperties {
    public final List<WrappedIndexedNode> originalNodes;
    public final List<ElectricalProperties> originalResistances;

    public DissolvedProperties(List<WrappedIndexedNode> originalNodes, List<ElectricalProperties> originalResistances) {
        super(originalResistances.stream().mapToDouble(ElectricalProperties::resistance).sum(), 0, 0);
        if (originalResistances.size() > 3) {
            this.originalResistances = new ArrayList<>(originalResistances);
            this.originalNodes = new ArrayList<>(originalNodes);
        } else {
            this.originalResistances = originalResistances;
            this.originalNodes = originalNodes;
        }
    }

    @Override
    public void getVoltages(double v1, double v2, double[] toFill, int microTickBits, int microTick) {
        double totalResistance = resistance;
        double current = (v1 - v2) / totalResistance;
        double currentVoltage = v1;
        for (int i = 0; i < originalResistances.size(); i++) {
            WrappedIndexedNode nextNode = originalNodes.get(i + 1);

            ElectricalProperties properties = originalResistances.get(i);
            double voltageDrop = current * properties.resistance;

            currentVoltage = currentVoltage - voltageDrop;

            toFill[(nextNode.ordinal << microTickBits) | microTick] = currentVoltage;
        }
    }

    @Override
    public ElectricalProperties invert() {
        return this;
    }
}
