package com.george_vi.electroenergetics.content.varistor;

import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;

public class VaristorProperties extends MicroTickingElectricalProperties {

    public VaristorProperties() {}
    public double voltageAtContact;
    public int voltageAtOneAmp;

    @Override
    public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
        this.resistance = tickVaristor(voltageAtContact, voltageAtOneAmp);
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {
        voltageAtContact = allVoltages[(n1 << microTickBits) | (microTick)];
    }

    /**
     * Returns the resistance Value of the Varistor with the
     * given Voltage at it's contact and the voltage
     *
     * @see com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHeldVaristor
     */
    public double tickVaristor(double voltageAtVaristor, int voltageAtOneAmp) {
        double a = (1 / 0.4f);
        if (voltageAtVaristor == 0) {
            //Avoids NaN when circuit isn't closed
            return 1_000_000;
        }
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
