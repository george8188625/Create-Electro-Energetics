package com.george_vi.electroenergetics.simulation.electrical_properties;

public final class MicroTickingInvertedElectricalProperties extends ElectricalProperties {
    MicroTickingElectricalProperties original;
    public MicroTickingInvertedElectricalProperties(MicroTickingElectricalProperties original) {
        super(1, 0, 0);
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
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public final boolean isSimpleResistor() {
        return false;
    }

    @Override
    public ElectricalProperties invert() {
        return original;
    }
}
