package com.george_vi.electroenergetics.simulation.electrical_properties;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;

import java.util.List;

public class ParallelDissolvedProperties extends ElectricalProperties implements IDissolvedProperties {
    public final List<ElectricalProperties> originalResistances;
    public final WrappedIndexedNode node1;
    public final WrappedIndexedNode node2;

    public ParallelDissolvedProperties(List<ElectricalProperties> originalResistances,
                                       WrappedIndexedNode node1, WrappedIndexedNode node2) {
        super(1, 0, 0);
        this.node1 = node1;
        this.node2 = node2;
        double conductance = originalResistances.stream().mapToDouble(ElectricalProperties::conductance).sum();
        this.resistance = conductance == 0 ? 1e+11d : 1 / conductance;
        this.originalResistances = originalResistances;
    }

    @Override
    public void getVoltages(double v1, double v2, double[] toFill, int microTickBits, int microTick) {
//        for (ElectricalProperties properties : originalResistances) {
//            if (properties instanceof DissolvedProperties dp) {
//                if (dp.originalNodes.getFirst().equals(node1))
//                    dp.getVoltages(v1, v2, toFill, microTickBits, microTick);
//                else
//                    dp.getVoltages(v2, v1, toFill, microTickBits, microTick);
//            }
//        }
    }

    @Override
    public ElectricalProperties invert() {
        return this;
    }
}
