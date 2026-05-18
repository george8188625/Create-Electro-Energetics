package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;

import java.util.List;
import java.util.Map;

public interface ISchematicInfrastructureList {
    Map<InWorldNodeConnection, WireData> getWireConnections();
    Map<InWorldNode, String> getNodeLabels();
    List<CatenaryConnection> getCatenaryConnections();
}
