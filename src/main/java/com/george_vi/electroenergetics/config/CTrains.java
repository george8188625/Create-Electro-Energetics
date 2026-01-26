package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;

public class CTrains extends ConfigBase {
    public final ConfigInt ticksPerAccumulatorOnTrain = i(1200, 1, "trainAccumulatorTime", "[in Ticks / one Accumulator]");
    public final ConfigInt ticksPerAccumulatorChargeOnTrain = i(1200, 1, "trainAccumulatorChargeTime", "[in Ticks / one Accumulator]");
    public final ConfigFloat highSpeedPantographSparks = f(1.44f, 0f, "speedPantographSparkThreshold", "Trains faster than this value will have sparks on pantographs. Set to 0 to disable. [in Blocks / Tick]");
    public final ConfigBool winterPantographSparks = b(true, "winterPantographSparks", "Sparks appear on electric trains in cold conditions.");

    @Override
    public String getName() {
        return "trains";
    }
}
