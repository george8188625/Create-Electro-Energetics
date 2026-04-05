package com.george_vi.electroenergetics.content.rotor;

/**
 * This is used because Create's stress networks don't like being udpated tick-to-tick
 */
public class VirtualRotor {
    public float rpm;
    public float stress;
    public double voltage;

    /**
     * Used as speed instead of rpm.
     */
    public double actualOmega;
    public double angle;

    public int totalMicroTicks;

    double torqueAccumulated;
    double angleError;

    void advance() {
        angle = (angle + actualOmega) % 360;
        if (Double.isNaN(angle))
            angle = 0;

    }

    void swing() {
        double inertia = 30000;
        double damping = 0.1;
        double nominalOmega = 360 * (rpm * (40.0 / 256.0)) / 20 / totalMicroTicks;

        double netEnergy = torqueAccumulated - (stress / 20.0);

        angleError += netEnergy / inertia;
        angleError *= (1.0 - damping);

        actualOmega = nominalOmega + (angleError / totalMicroTicks);

        torqueAccumulated = 0;
    }
}
