package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class CSimulation extends ConfigBase {
    public final ConfigBool optimizeGraph = b(true, "optimizeGraph", "Don't turn off unless you want to cook your TPS");
    public final ConfigInt microTicks = i(1, 1, 64, "microTicks", "This describes how many times the simulation is run every tick.");
    public final ConfigFloat capacitySolverDamp = f(0.5005f, 0.5f, 1.0f, "capacitySolverDamp",
            "Balances solver stability and capacitor accuracy (0.5 = most accurate, 1.0 = most stable).");

    @Override
    public @NotNull String getName() {
        return "simulation";
    }
}
