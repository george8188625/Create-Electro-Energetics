package com.george_vi.electroenergetics.simulation.simulator;

import java.util.Objects;

public class ElectricalProperties {
    private final double resistance;
    private final double voltageSource;
    private final double currentSource;

    public ElectricalProperties(double resistance, double voltageSource, double currentSource) {
        if (resistance == 0)
            resistance = 0.01d;
        this.resistance = resistance;
        this.voltageSource = voltageSource;
        this.currentSource = currentSource;
    }

    public static ElectricalProperties resistor(double resistance) {
        return new ElectricalProperties(resistance, 0, 0);
    }

    public ElectricalProperties invert() {
        return new ElectricalProperties(resistance, -voltageSource, -currentSource);
    }

    @Override
    public String toString() {
        return "{" +
                "resistance=" + resistance +
                ", voltageSource=" + voltageSource +
                ", currentSource=" + currentSource +
                '}';
    }

    public double resistance() {
        return resistance;
    }

    public double voltageSource() {
        return voltageSource;
    }

    public double currentSource() {
        return currentSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ElectricalProperties) obj;
        return Double.doubleToLongBits(this.resistance) == Double.doubleToLongBits(that.resistance) &&
                Double.doubleToLongBits(this.voltageSource) == Double.doubleToLongBits(that.voltageSource) &&
                Double.doubleToLongBits(this.currentSource) == Double.doubleToLongBits(that.currentSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resistance, voltageSource, currentSource);
    }


}
