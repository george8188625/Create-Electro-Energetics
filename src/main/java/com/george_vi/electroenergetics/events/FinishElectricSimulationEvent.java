package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class FinishElectricSimulationEvent extends Event {
    public final SimulationResults results;
    public final ServerLevel level;
    public final InfrastructureSavedData sd;

    public FinishElectricSimulationEvent(SimulationResults results, ServerLevel level, InfrastructureSavedData sd) {
        this.results = results;
        this.level = level;
        this.sd = sd;
    }
}
