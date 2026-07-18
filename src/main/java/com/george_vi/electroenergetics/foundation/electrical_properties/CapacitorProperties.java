package com.george_vi.electroenergetics.foundation.electrical_properties;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;

public class CapacitorProperties extends MicroTickingElectricalProperties {

    /**
     * <p>This is a saved state for the capacitor that defines its charge level.</p>
     * <p>Read only during the postTick phase.</p>
     * <p>Write only during the preTick phase.</p>
     */
    public double lastVoltage;
    public double lastCurrent; // save for Damped Trapezoidal
    private double currentConductance; // pass raw data to afterTick

    /**
     * <p>Specifies the capacitance of the capacitor behavior.</p>
     * <p>Written only during the preTick phase.</p>
     */
    public double capacitance;

    public CapacitorProperties() {

    }

    @Override
    public void tick(double[] allVoltages, int microTick, int totalMicroTicks, int n1, int n2) {
        tickCapacitor(totalMicroTicks);
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int totalMicroTicks) {
        lastVoltage =
                allVoltages[n1 * totalMicroTicks + microTick] -
                allVoltages[n2 * totalMicroTicks + microTick];
        lastCurrent = currentConductance * lastVoltage - this.currentSource;
    }

    private void tickCapacitor(int totalMicroTicks) {
        double alpha = CEEConfigs.server().simulationConfig.capacitySolverDamp.get();
        double capacitance = Math.max(this.capacitance, 1e-12d);
        double timeStep = 0.05 / totalMicroTicks;

        this.currentConductance = capacitance / (alpha * timeStep);
        double historyCurrent = currentConductance * lastVoltage + ((1 - alpha) / alpha) * lastCurrent;

        this.resistance = 1 / currentConductance;
        this.currentSource = historyCurrent;
    }
}
