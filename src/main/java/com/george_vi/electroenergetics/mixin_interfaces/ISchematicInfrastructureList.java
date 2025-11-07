package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.WireData;

import java.util.List;
import java.util.Map;

public interface ISchematicInfrastructureList {
    Map<NodeConnection, WireData> electroEnergetics$getWireConnections();

    List<InfrastructureSavedData.SimulatedDeviceInstance> electroEnergetics$getDevices();
}
