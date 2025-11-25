package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.WireData;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(SchematicLevel.class)
public class SchematicLevelMixin implements ISchematicInfrastructureList {
    @Unique
    Map<NodeConnection, WireData> electroEnergetics$wireConnections = new HashMap<>();

    @Unique
    List<SimulatedDeviceInstance> electroEnergetics$devices = new ArrayList<>();

    @Override
    public Map<NodeConnection, WireData> electroEnergetics$getWireConnections() {
        return electroEnergetics$wireConnections;
    }

    @Override
    public List<SimulatedDeviceInstance> electroEnergetics$getDevices() {
        return electroEnergetics$devices;
    }
}
