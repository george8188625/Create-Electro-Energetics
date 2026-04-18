package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEWireTypes;

import java.util.Collections;

public class CatenaryConnectionData extends WireData {
    public boolean isLow;

    public CatenaryConnectionData(float temperature, boolean isLow, double length) {
        super(CEEWireTypes.COPPER.get(), temperature, Collections.emptyList(), length);
        this.isLow = isLow;
    }
}
