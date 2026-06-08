package com.george_vi.electroenergetics.foundation.electrical_properties;

import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;

public class InductorProperties extends MicroTickingElectricalProperties {

    /**
     * <p>This is a saved state for the inductor that defines its current level.</p>
     * <p>Read only during the postTick phase.</p>
     * <p>Write only during the preTick phase.</p>
     */
    public double lastCurrent;

    /**
     * <p>Specifies the inductance of the inductor behavior.</p>
     * <p>Written only during the preTick phase.</p>
     */
    public double inductance;

    public InductorProperties() {

    }

    @Override
    public void tick(double[] allVoltages, int microTick, int totalMicroTicks, int n1, int n2) {
        tickInductor(totalMicroTicks);
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int totalMicroTicks) {
        double voltage =
                allVoltages[n1 * totalMicroTicks + microTick] -
                allVoltages[n2 * totalMicroTicks + microTick];

        double inductance = Math.max(this.inductance, 1e-12d);
        double timeStep = 0.05 / totalMicroTicks;

        lastCurrent += (voltage / inductance) * timeStep;
    }

    private void tickInductor(int totalMicroTicks) {
        double inductance = Math.max(this.inductance, 1e-12d);
        double timeStep = 0.05 / totalMicroTicks;

        this.resistance = inductance / timeStep;
        this.currentSource = lastCurrent;
    }
}