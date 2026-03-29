package com.george_vi.electroenergetics.foundation.electrical_properties;

import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;

public class CapacitorProperties extends MicroTickingElectricalProperties {

    /**
     * <p>This is a saved state for the capacitor that defines its charge level.</p>
     * <p>Read only during the postTick phase.</p>
     */
    public double lastVoltage;

    /**
     * <p>Specifies the capacitance of the capacitor behavior.</p>
     * <p>Written only during the preTick phase.</p>
     */
    public double capacitance;

    public CapacitorProperties() {

    }

    @Override
    public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
        tickCapacitor(totalMicroTicks);
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {
        lastVoltage =
                allVoltages[(n1 << microTickBits) | (microTick)] -
                allVoltages[(n2 << microTickBits) | (microTick)];
    }

    private void tickCapacitor(int totalMicroTicks) {
        double capacitance = Math.max(this.capacitance, 1e-12d);
        double timeStep = 0.05 / totalMicroTicks;

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * lastVoltage;

        this.resistance = 1 / conductance;
        this.currentSource = -historyCurrent;
    }
}
