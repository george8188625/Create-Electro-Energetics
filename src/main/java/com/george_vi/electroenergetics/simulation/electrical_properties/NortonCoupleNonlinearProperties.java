package com.george_vi.electroenergetics.simulation.electrical_properties;

/**
 * Acts as a nonlinear connection for components that are be linearized to a current source + resistor in parallel.
 */
public abstract class NortonCoupleNonlinearProperties extends ElectricalProperties implements ISolverIterationTicker {
    InvertedElectricalProperties inverted = null;
    public double conductance;
    public double currentSource;

    @Override
    public double currentSource() {
        return currentSource;
    }

    @Override
    public boolean isCurrentSource() {
        return currentSource != 0;
    }

    @Override
    public double conductance() {
        return conductance;
    }

    @Override
    public double resistance() {
        return 1 / conductance();
    }

    @Override
    public final ElectricalProperties invert() {
        if (inverted == null)
            return inverted = new InvertedElectricalProperties(this);
        return inverted;
    }

    @Override
    public byte dissolveMode() {
        return DISSOLVE_NONLINEAR;
    }
}
