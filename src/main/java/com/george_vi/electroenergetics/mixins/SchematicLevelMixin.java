package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
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
    Map<InWorldNodeConnection, WireData> electroEnergetics$wireConnections = new HashMap<>();

    @Unique
    List<SimulatedDeviceInstance> electroEnergetics$devices = new ArrayList<>();

    @Override
    public Map<InWorldNodeConnection, WireData> getWireConnections() {
        return electroEnergetics$wireConnections;
    }

    @Override
    public List<SimulatedDeviceInstance> getDevices() {
        return electroEnergetics$devices;
    }
}
