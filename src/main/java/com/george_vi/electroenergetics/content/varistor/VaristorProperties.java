package com.george_vi.electroenergetics.content.varistor;

import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;

public class VaristorProperties extends MicroTickingElectricalProperties {
    final VaristorDevice.DataHolder extraData;

    public VaristorProperties(VaristorDevice.DataHolder extraData) {
        this.extraData = extraData;
    }

    @Override
    public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
        this.resistance = tickVaristor(allVoltages[(n1 << microTickBits) | (microTick)], extraData.voltageAtOneAmp);
    }

    /**
     * Returns the resistance Value of the Varistor with the
     * given Voltage at it's contact and the voltage
     *
     * @see com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHeldVaristor
     *
     */
    public double tickVaristor(double voltageAtVaristor, int voltageAtOneAmp) {
        double a = (1 / 0.4f);
        return (
                Math.pow(
                        voltageAtOneAmp,
                        a)
                        /
                        Math.pow(
                                voltageAtVaristor,
                                (a - 1)
                        )
        );

    }
}
