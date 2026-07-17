package com.george_vi.electroenergetics.simulation.electrical_properties;

public abstract class ElectricalProperties {

    public static ElectricalProperties resistor(double resistance) {
        if (resistance == 1e+11d)
            return ResistorProperties.ZERO_CONDUCTANCE;
        if (resistance == 0.001)
            return ResistorProperties.MILlI;
        if (resistance == 0.01)
            return ResistorProperties.TEN_MILlI;
        if (resistance == 0.1)
            return ResistorProperties.HUNDRED_MILlI;
        return new ResistorProperties(resistance);
    }

    /**
     * @param resistance series resistance
     * @param voltage voltage
     * @return the Norton equivalent of a voltage source with resistance
     */
    public static ElectricalProperties fromThevenin(double resistance, double voltage) {
        return new NortonProperties(resistance, voltage / resistance);
    }

    public ElectricalProperties invert() {
        return this;
    }

    public abstract double resistance();

    public double conductance() {
        double resistance = resistance();
        if (resistance > 1e+10d)
            return 0;
        return 1 / resistance;
    }

    public double voltageSource() {
        return 0;
    }

    public boolean isVoltageSource() {
        return false;
    }

    public double currentSource() {
        return 0;
    }

    public boolean isCurrentSource() {
        return false;
    }

    public double gMin() {
        return 0;
    }

    public boolean isSimpleResistor() {
        return false;
    }

    public static final byte CANT_DISSOLVE = 0;
    public static final byte DISSOLVE = 1;
    public static final byte DISSOLVE_NONLINEAR = 2;

    public byte dissolveMode() {
        return DISSOLVE;
    }

    public static final byte NORMAL_OPTIMIZATION = 0;
    public static final byte NONLINEAR_OPTIMIZATION = 1;

    public boolean canDissolve(int optimizationPass) {
        return switch (dissolveMode()) {
            case CANT_DISSOLVE -> false;
            case DISSOLVE -> true;
            case DISSOLVE_NONLINEAR -> optimizationPass == NONLINEAR_OPTIMIZATION;
            default -> throw new IllegalStateException("Unexpected dissolveMode value: " + dissolveMode());
        };
    }
}
