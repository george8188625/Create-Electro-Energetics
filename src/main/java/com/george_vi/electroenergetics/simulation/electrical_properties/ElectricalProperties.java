package com.george_vi.electroenergetics.simulation.electrical_properties;

import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Objects;

public class ElectricalProperties {
    public static final ElectricalProperties ZERO_CONDUCTANCE = new ElectricalProperties(1e+11d, 0, 0, false);
    public static final ElectricalProperties MILlI = new ElectricalProperties(0.001d, 0, 0, false);
    public static final ElectricalProperties TEN_MILlI = new ElectricalProperties(0.01d, 0, 0, false);
    public static final ElectricalProperties HUNDRED_MILlI = new ElectricalProperties(0.1d, 0, 0, false);
    public static final ElectricalProperties INFINITE_CONDUCTANCE = new ElectricalProperties(1e+11d, 0, 0, true);

    public double resistance;
    public double voltageSource;
    public double currentSource;
    public boolean isForcedVoltageSource;

    public ElectricalProperties(double resistance, double voltageSource, double currentSource) {
        this(resistance, voltageSource, currentSource, false);
    }

    public ElectricalProperties(double resistance, double voltageSource, double currentSource, boolean isForcedVoltageSource) {
        if (resistance == 0)
            if (FMLEnvironment.production)
                resistance = 0.001;
            else
                throw new IllegalArgumentException("Resistance can't be zero!");
        this.resistance = resistance;
        this.voltageSource = voltageSource;
        this.currentSource = currentSource;
        this.isForcedVoltageSource = isForcedVoltageSource;
    }

    public static ElectricalProperties resistor(double resistance) {
        if (resistance == 1e+11d)
            return ZERO_CONDUCTANCE;
        if (resistance == 0.001)
            return MILlI;
        if (resistance == 0.01)
            return TEN_MILlI;
        if (resistance == 0.1)
            return HUNDRED_MILlI;
        return new ElectricalProperties(resistance, 0, 0);
    }


    /**
     * @param resistance series resistance
     * @param voltage voltage
     * @return the Norton equivalent of a voltage source with resistance
     */
    public static ElectricalProperties fromThevenin(double resistance, double voltage) {
        return new ElectricalProperties(resistance, 0, voltage / resistance);
    }

    public ElectricalProperties invert() {
        if (isSimpleResistor())
            return this;
        return new ElectricalProperties(resistance, voltageSource == 0 ? 0 : -voltageSource, currentSource == 0 ? 0 : -currentSource, isForcedVoltageSource);
    }

    @Override
    public String toString() {
        return "{ " +
                (conductance() != 0 ? "R=" + resistance() + " " : "") +
                (isVoltageSource() ? "Vs=" + voltageSource() + " " : "") +
                (isCurrentSource() ? "Is=" + currentSource() + " }" : "}");
    }

    public double resistance() {
        return resistance;
    }

    public double conductance() {
        if (resistance > 1e+10d)
            return 0;
        return 1 / resistance;
    }

    public double voltageSource() {
        return voltageSource;
    }

    public boolean isVoltageSource() {
        return voltageSource != 0 || isForcedVoltageSource;
    }

    public double currentSource() {
        return currentSource;
    }

    public boolean isCurrentSource() {
        return currentSource != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ElectricalProperties) obj;
        return Double.doubleToLongBits(this.resistance) == Double.doubleToLongBits(that.resistance) &&
                Double.doubleToLongBits(this.voltageSource) == Double.doubleToLongBits(that.voltageSource) &&
                Double.doubleToLongBits(this.currentSource) == Double.doubleToLongBits(that.currentSource) &&
                this.isForcedVoltageSource == that.isForcedVoltageSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resistance, voltageSource, currentSource, isForcedVoltageSource);
    }

    public boolean isSimpleResistor() {
        return !isCurrentSource() && !isVoltageSource();
    }
}
