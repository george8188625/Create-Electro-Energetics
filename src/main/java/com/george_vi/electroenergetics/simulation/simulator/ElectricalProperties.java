package com.george_vi.electroenergetics.simulation.simulator;

import java.util.Objects;

public class ElectricalProperties {
    private final double resistance;
    private final double voltageSource;

    public ElectricalProperties(double resistance, double voltageSource) {
        this.resistance = resistance;
        this.voltageSource = voltageSource;
    }

    public ElectricalProperties invert() {
        return new ElectricalProperties(resistance, -voltageSource);
    }

    @Override
    public String toString() {
        return "{" +
                "resistance=" + resistance +
                ", voltageSource=" + voltageSource +
                '}';
    }

    public double resistance() {
        return resistance;
    }

    public double voltageSource() {
        return voltageSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ElectricalProperties) obj;
        return Double.doubleToLongBits(this.resistance) == Double.doubleToLongBits(that.resistance) &&
                Double.doubleToLongBits(this.voltageSource) == Double.doubleToLongBits(that.voltageSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resistance, voltageSource);
    }


}
