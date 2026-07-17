package com.george_vi.electroenergetics.simulation.electrical_properties;

/**
 * Used as an opposite-direction connection for more advanced connections such as micro tickers or nonlinear connections.
 */
public final class InvertedElectricalProperties extends ElectricalProperties {
    public ElectricalProperties original;
    public InvertedElectricalProperties(ElectricalProperties original) {
        this.original = original;
    }

    @Override
    public double resistance() {
        return original.resistance();
    }

    @Override
    public double conductance() {
        return original.conductance();
    }

    @Override
    public double voltageSource() {
        return -original.voltageSource();
    }

    @Override
    public double currentSource() {
        return -original.currentSource();
    }

    @Override
    public boolean isVoltageSource() {
        return original.isVoltageSource();
    }

    @Override
    public boolean isCurrentSource() {
        return original.isCurrentSource();
    }

    @Override
    public ElectricalProperties invert() {
        return original;
    }

    @Override
    public byte dissolveMode() {
        return original.dissolveMode();
    }
}
