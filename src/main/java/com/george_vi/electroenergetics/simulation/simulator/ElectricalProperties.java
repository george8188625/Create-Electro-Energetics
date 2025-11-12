package com.george_vi.electroenergetics.simulation.simulator;

import java.util.Objects;

public class ElectricalProperties {
    private final double resistance;
    private final double voltageSource;
    private final double currentSource;
    private final boolean isForcedVoltageSource;
    private final boolean isForcedCurrentSource;

    public ElectricalProperties(double resistance, double voltageSource, double currentSource) {
        this(resistance, voltageSource, currentSource, false, false);
    }

    public ElectricalProperties(double resistance, double voltageSource, double currentSource, boolean isForcedVoltageSource, boolean isForcedCurrentSource) {
        if (resistance == 0)
            throw new IllegalArgumentException("Resistance can't be zero!");
        this.resistance = resistance;
        this.voltageSource = voltageSource;
        this.currentSource = currentSource;
        this.isForcedVoltageSource = isForcedVoltageSource;
        this.isForcedCurrentSource = isForcedCurrentSource;
    }

    public static ElectricalProperties resistor(double resistance) {
        return new ElectricalProperties(resistance, 0, 0);
    }

    public ElectricalProperties invert() {
        return new ElectricalProperties(resistance, -voltageSource, -currentSource, isForcedVoltageSource, isForcedCurrentSource);
    }

    @Override
    public String toString() {
        return "{" +
                "resistance=" + resistance +
                ", voltageSource=" + voltageSource +
                ", currentSource=" + currentSource +
                ", forceVoltageSource=" + isForcedVoltageSource +
                ", forceCurrentSource=" + isForcedCurrentSource +
                '}';
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
        return currentSource != 0 || isForcedCurrentSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ElectricalProperties) obj;
        return Double.doubleToLongBits(this.resistance) == Double.doubleToLongBits(that.resistance) &&
                Double.doubleToLongBits(this.voltageSource) == Double.doubleToLongBits(that.voltageSource) &&
                Double.doubleToLongBits(this.currentSource) == Double.doubleToLongBits(that.currentSource) &&
                this.isForcedCurrentSource == that.isForcedCurrentSource && this.isForcedVoltageSource == that.isForcedVoltageSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resistance, voltageSource, currentSource, isForcedCurrentSource, isForcedVoltageSource);
    }


    public ElectricalProperties add(ElectricalProperties properties) {
        double addedConductance = (properties.conductance() + conductance());
        return new ElectricalProperties(addedConductance == 0 ? 1e+11d : 1 / addedConductance, properties.voltageSource() + voltageSource(), properties.currentSource() + currentSource(), properties.isForcedVoltageSource || isForcedVoltageSource, properties.isForcedCurrentSource || isForcedCurrentSource);
    }
}
