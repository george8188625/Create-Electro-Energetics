package com.george_vi.electroenergetics.simulation.electrical_properties;

/**
 * MicroTickingElectricalProperties must not access any blocks, block-entities, entities, levels, etc.
 * This is because the electrical simulation is run off thread.
 */
public abstract class MicroTickingElectricalProperties extends ElectricalProperties {
    InvertedElectricalProperties inverted = null;
    public double resistance;
    public double currentSource;
    public double voltageSource;

    public MicroTickingElectricalProperties() {
    }

    public abstract void tick(double[] allVoltages, int microTick, int totalMicroTicks, int n1, int n2);

    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int totalMicroTicks) {

    }

    @Override
    public final boolean isSimpleResistor() {
        return false;
    }

    @Override
    public double resistance() {
        return resistance;
    }

    @Override
    public double currentSource() {
        return currentSource;
    }

    @Override
    public boolean isCurrentSource() {
        return currentSource != 0;
    }

    @Override
    public double voltageSource() {
        return voltageSource;
    }

    @Override
    public boolean isVoltageSource() {
        return voltageSource != 0;
    }

    @Override
    public final ElectricalProperties invert() {
        if (inverted == null)
            return inverted = new InvertedElectricalProperties(this);
        return inverted;
    }
}
