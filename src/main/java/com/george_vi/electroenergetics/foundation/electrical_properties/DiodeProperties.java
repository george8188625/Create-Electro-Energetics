package com.george_vi.electroenergetics.foundation.electrical_properties;

import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import net.minecraft.util.Mth;

public class DiodeProperties extends MicroTickingElectricalProperties {

    /**
     * <p>This is a saved state for the diode that defines its state.</p>
     * <p>Read only during the postTick phase.</p>
     * <p>Write only during the preTick phase.</p>
     */
    public double lastVoltage;

    @Override
    public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
        tickDiode(totalMicroTicks);
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {
        lastVoltage = allVoltages[(n2 << microTickBits) | (microTick)] - allVoltages[(n1 << microTickBits) | (microTick)];
    }

    private void tickDiode(int totalMicroTicks) {
        // Thanks, ChatGPT
        double iS = 10e-10d;
        double vT = 0.050;

        lastVoltage = Mth.clamp(lastVoltage, -0.8, 0.8);

        double Cj = 1e-11d;
        double gCap = Cj / (0.05 / totalMicroTicks);
        double iEqCap = gCap * lastVoltage;

        double g = Math.max(1e-12d, (iS / vT) * Math.exp(lastVoltage / vT)) + gCap;

        double resistance = 1 / g;
        double currentSource = iS * (Math.exp(lastVoltage / vT) - 1) - g * lastVoltage;
        this.resistance = resistance;
        this.currentSource = -currentSource - iEqCap;
    }
}
