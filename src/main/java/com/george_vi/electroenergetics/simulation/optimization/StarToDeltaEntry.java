package com.george_vi.electroenergetics.simulation.optimization;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;

public class StarToDeltaEntry implements TopologyOptimizationEntry {
    public final ElectricalProperties a;
    public final ElectricalProperties b;
    public final ElectricalProperties c;
    public final WrappedIndexedNode centralNode;
    public final WrappedIndexedNode na;
    public final WrappedIndexedNode nb;
    public final WrappedIndexedNode nc;

    public StarToDeltaEntry(ElectricalProperties a, ElectricalProperties b, ElectricalProperties c,
                            WrappedIndexedNode na, WrappedIndexedNode nb, WrappedIndexedNode nc,
                            WrappedIndexedNode centralNode) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.na = na;
        this.nb = nb;
        this.nc = nc;
        this.centralNode = centralNode;
    }

    public double calculateRAB() {
        // Rab = (RaRb + RbRc + RcRa) / Rc
        return (a.resistance() * b.resistance() +
                b.resistance() * c.resistance() +
                c.resistance() * a.resistance()) / c.resistance();
    }

    public double calculateRBC() {
        // Rbc = (RaRb + RbRc + RcRa) / Ra
        return (a.resistance() * b.resistance() +
                b.resistance() * c.resistance() +
                c.resistance() * a.resistance()) / a.resistance();
    }

    public double calculateRCA() {
        // Rca = (RaRb + RbRc + RcRa) / Rb
        return (a.resistance() * b.resistance() +
                b.resistance() * c.resistance() +
                c.resistance() * a.resistance()) / b.resistance();
    }

    public double calculateCenter(double va, double vb, double vc) {
        return (a.conductance() * va + b.conductance() * vb + c.conductance() * vc) /
                (a.conductance() + b.conductance() + c.conductance());
    }
}
