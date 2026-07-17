package com.george_vi.electroenergetics.simulation.electrical_properties;

import com.george_vi.electroenergetics.simulation.util.SparseMatrix;

public abstract class NonlinearProperties extends ElectricalProperties {
    InvertedElectricalProperties inverted = null;

    @Override
    public final double resistance() {
        return 1e+11d;
    }

    @Override
    public double conductance() {
        return 0;
    }

    @Override
    public final ElectricalProperties invert() {
        if (inverted == null)
            return inverted = new InvertedElectricalProperties(this);
        return inverted;
    }

    @Override
    public byte dissolveMode() {
        return CANT_DISSOLVE;
    }

    public abstract void stampNonLinear(double v1, double v2, SparseMatrix matrix, double[] rhs, int n1, int n2, boolean first);
}
