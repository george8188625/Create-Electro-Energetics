package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class CRotor extends ConfigBase {
    public final ConfigFloat rotorStressMultiplier = f(1, 0.0001f, "rotorStressMultiplier", "SUs of 1 Watt");
    public final ConfigFloat rotorPowerMultiplier = f(48, 0.0001f, "rotorPowerMultiplier", "decide output power of alternators through voltage");
    public final ConfigFloat rotorFullLoadCurrent = f(100, 0.0001f, "rotorFullLoadCurrent", "[in Amps]");

    @Override
    public @NotNull String getName() {
        return "rotor";
    }
}
