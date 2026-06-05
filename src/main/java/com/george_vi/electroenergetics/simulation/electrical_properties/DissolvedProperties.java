package com.george_vi.electroenergetics.simulation.electrical_properties;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;

import java.util.List;

public class DissolvedProperties extends ElectricalProperties implements IDissolvedProperties {
    private final int[] originalNodeIDs;
    private final double[] originalResistances;

    public DissolvedProperties(List<WrappedIndexedNode> originalNodes, List<ElectricalProperties> originalResistances) {
        super(originalResistances.stream().mapToDouble(ElectricalProperties::resistance).sum(), 0, 0);
        this.originalNodeIDs = new int[originalNodes.size()];
        for (int i = 0; i < originalNodes.size(); i++)
            this.originalNodeIDs[i] = originalNodes.get(i).ordinal;

        this.originalResistances = new double[originalResistances.size()];
        for (int i = 0; i < originalResistances.size(); i++)
            this.originalResistances[i] = originalResistances.get(i).resistance;
    }

    @Override
    public void getVoltages(double v1, double v2, double[] toFill, int microTickBits, int microTick) {
        double totalResistance = resistance;
        double current = (v1 - v2) / totalResistance;
        double currentVoltage = v1;
        for (int i = 0; i < originalResistances.length; i++) {
            int nextNodeID = originalNodeIDs[i + 1];

            double resistance = originalResistances[i];
            double voltageDrop = current * resistance;

            currentVoltage = currentVoltage - voltageDrop;

            toFill[(nextNodeID << microTickBits) | microTick] = currentVoltage;
        }
    }

    @Override
    public ElectricalProperties invert() {
        return this;
    }
}
