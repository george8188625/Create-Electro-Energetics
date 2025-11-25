package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.WireData;

import java.util.List;
import java.util.Map;

public interface ISchematicInfrastructureList {
    Map<NodeConnection, WireData> electroEnergetics$getWireConnections();

    List<SimulatedDeviceInstance> electroEnergetics$getDevices();
}
