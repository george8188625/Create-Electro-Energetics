package com.george_vi.electroenergetics.foundation.electrical_properties;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import net.minecraft.util.Mth;

public class AccumulatorProperties extends MicroTickingElectricalProperties {

    public double internalResistance = 0.1;

    /**
     * <p>This is a saved state for the accumulator that defines its charge level.</p>
     * <p>Read only during the postTick phase.</p>
     * <p>Write only during the preTick phase.</p>
     */
    public double storedCharge;

    /**
     * <p>Specifies the voltage of the accumulator behavior.</p>
     * <p>Write only during the preTick phase.</p>
     */
    public double cellVoltage = 24;
    public double capacitance;

    public AccumulatorProperties() {

    }

    @Override
    public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
        double soc = soc();
        double sourceVoltage = baseOpenCircuitVoltage(soc) * cellVoltage;

        currentSource = -sourceVoltage / internalResistance;
        resistance = internalResistance;
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {
        double lastVoltage =
                allVoltages[(n1 << microTickBits) | (microTick)] -
                allVoltages[(n2 << microTickBits) | (microTick)];

        double dt = 0.05 / totalMicroTicks;

        double soc = soc();

        double sourceVoltage = baseOpenCircuitVoltage(soc) * cellVoltage;

        double lastCurrent = (sourceVoltage - lastVoltage) / internalResistance;

        storedCharge -= lastCurrent * dt;

        storedCharge = Mth.clamp(storedCharge, 0.0, getNominalCharge());
    }

    public static double getNominalCharge() {
        return CEEConfigs.server().voltageValues.maxAccumulatorCharge.get();
    }

    public static double baseOpenCircuitVoltage(double soc) {
        soc = Mth.clamp(soc, 0, 1);

        if (soc < 1e-5d)
            return 0;

        return (Math.log10(1000 * soc) / 4) + 0.25d;
    }

    public static double energyFromSOC(double charge, double cellVoltage, double nominalCharge) {
        double soc = charge / nominalCharge;
        double step = 1.0 / 100;

        double energy = 0.0;
        if (Math.abs(charge) <= 1e-1d || Double.isNaN(soc))
            return 0;

        for (double ch = step * 0.5;; ch += step) {
            double v = baseOpenCircuitVoltage(ch) * cellVoltage;
            if (ch > soc) {
                // Interpolate
                // The reason is, with a low step number, like 1/100, the result can be very steppy, this just makes it smooth.
                double bit = 1 - ((ch - soc) / step);
                energy = Mth.lerp(bit, energy, (energy + v * step * nominalCharge));
                break;
            }
            energy += v * step * nominalCharge;
        }

        return energy / 3600.0;
    }

    public double soc() {
        return Mth.clamp(storedCharge / getNominalCharge(), 0, 1);
    }
}
