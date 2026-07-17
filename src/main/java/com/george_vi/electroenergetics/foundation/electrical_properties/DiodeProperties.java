package com.george_vi.electroenergetics.foundation.electrical_properties;

import com.george_vi.electroenergetics.simulation.electrical_properties.NortonCoupleNonlinearProperties;

public class DiodeProperties extends NortonCoupleNonlinearProperties {

    private double vOld;
    private final double vCrit, vt, is;
    private final double leakage = 1e-12d;

    public DiodeProperties(double thermalVoltage, double saturationCurrent) {
        this.vt = thermalVoltage;
        this.is = saturationCurrent;

        vCrit = vt * Math.log(vt / (Math.sqrt(2) * leakage));
    }

    @Override
    public void tick(double v1, double v2, boolean first) {
        double vd;
        if (first) {
            vd = vOld;
        } else {
            vd = v1 - v2;
            vd = limitStep(vOld, vd);
            vOld = vd;
        }

        double expVal = Math.exp(vd / vt);

        double Id = is * (expVal - 1.0);

        double gd = (is / vt) * expVal;

        double Ieq = Id - gd * vd;

        conductance = gd;
        currentSource = -Ieq;
    }

    @Override
    public double gMin() {
        return leakage;
    }

    private double limitStep(double vOld, double vNew) {
        if (vNew > vCrit && Math.abs(vNew - vOld) > (vt + vt)) {
            if (vOld > 0) {
                double arg = 1 + (vNew - vOld) / vt;
                if (arg > 0)
                    vNew = Math.max(-40 * vt, vOld + vt * Math.log(arg));
                else
                    vNew = vCrit;
            } else
                vNew = vt * Math.log(vNew / vt);
        }
        return vNew;
    }
}
