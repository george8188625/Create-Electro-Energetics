package com.george_vi.electroenergetics.simulation.simulator;

public abstract class MicroTickingElectricalProperties extends ElectricalProperties {
    MicroTickingInvertedElectricalProperties inverted = null;
    public MicroTickingElectricalProperties() {
        super(1, 0, 0);
    }

    public abstract void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2);

    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {

    }

    @Override
    public final boolean isSimpleResistor() {
        return false;
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
    public ElectricalProperties invert() {
        if (inverted == null)
            return inverted = new MicroTickingInvertedElectricalProperties(this);
        else
            return inverted;
    }
}
