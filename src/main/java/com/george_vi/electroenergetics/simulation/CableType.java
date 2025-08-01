package com.george_vi.electroenergetics.simulation;

import java.util.function.DoubleSupplier;

public class CableType {
    final int conductors;
    final DoubleSupplier resistance;

    public CableType(int conductors, DoubleSupplier resistance) {
        this.conductors = conductors;
        this.resistance = resistance;
    }

    public int getConductors() {
        return conductors;
    }

    public double getResistance() {
        return resistance.getAsDouble();
    }
}
