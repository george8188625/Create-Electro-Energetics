package com.george_vi.electroenergetics.simulation.electrical_properties;

/**
 * Ticks every Newton iteration.
 * Use this on {@link ElectricalProperties} and set the resistance + current source accordingly.
 */
public interface ISolverIterationTicker {
    void tick(double v1, double v2, boolean first);
}
