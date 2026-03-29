package com.george_vi.electroenergetics.simulation.electrical_properties;

import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;

public interface CoupledProperties {
    DirectionalNodeConnection nodes();
    DirectionalNodeConnection coupledNodes();

    double ratio();

    boolean isPrimary();
}
