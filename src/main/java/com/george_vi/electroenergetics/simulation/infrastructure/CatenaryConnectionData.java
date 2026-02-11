package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.simulation.WireType;

import java.util.Collections;

public class CatenaryConnectionData extends WireData {
    public boolean isLow;

    public CatenaryConnectionData(float temperature, boolean isLow) {
        super(CEEWireTypes.COPPER.get(), temperature, Collections.emptyList());
        this.isLow = isLow;
    }
}
