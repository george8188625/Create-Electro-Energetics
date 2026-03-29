package com.george_vi.electroenergetics.simulation.electrical_properties;

public interface IDissolvedProperties {
    void getVoltages(double v1, double v2, double[] toFill, int microTickBits, int microTick);
}
