package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class AddToElectricGraphEvent extends Event {
    public final CircuitBuilder builder;
    public final ServerLevel level;
    public final InfrastructureSavedData sd;

    public AddToElectricGraphEvent(CircuitBuilder builder, ServerLevel level, InfrastructureSavedData sd) {
        this.builder = builder;
        this.level = level;
        this.sd = sd;
    }
}
