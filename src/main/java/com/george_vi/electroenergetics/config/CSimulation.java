package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;

public class CSimulation extends ConfigBase {
    public final ConfigBool optimizeGraph = b(true, "optimizeGraph", "Don't turn off unless you want to cook your TPS");
    public final ConfigBool creativeBatteryThevenin = b(true, "creativeBatteryThevenin", "Can improve solve times, but makes creative batteries not ideal sources");
    public final ConfigInt microTickBits = i(0, 0, 5, "microTickBits", "[EXPERIMENTAL] This to the power of two describes how many times the simulation is run every tick. Can be used to enable AC. Don't enable.");

    @Override
    public String getName() {
        return "simulation";
    }
}
