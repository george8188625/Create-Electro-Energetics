package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.simulation.infrastructure.WireData;

public class ClientWireData {
    public WireData wireData;
    public WireEffect effect;

    public ClientWireData(WireData wireData) {
        this.wireData = wireData;
    }
}
