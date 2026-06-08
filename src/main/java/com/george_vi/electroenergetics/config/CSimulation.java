package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class CSimulation extends ConfigBase {
    public final ConfigBool optimizeGraph = b(true, "optimizeGraph", "Don't turn off unless you want to cook your TPS");
    public final ConfigBool creativeBatteryThevenin = b(true, "creativeBatteryThevenin", "Can improve solve times, but makes creative batteries not ideal sources");
    public final ConfigInt microTicks = i(1, 1, 32, "microTicks", "[EXPERIMENTAL] This describes how many times the simulation is run every tick.");

    @Override
    public @NotNull String getName() {
        return "simulation";
    }
}
