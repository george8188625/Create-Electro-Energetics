package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CServer extends ConfigBase {
    public final ConfigDouble motorResistance = d(30d, 0.1d, "motorResistance", "[in Ohms]");
    public final ConfigDouble pumpResistance = d(200d, 0.1d, "pumpResistance", "[in Ohms]");
    public final ConfigDouble bulbResistance = d(1000d, 0.1d, "bulbResistance", "[in Ohms]");
    public final ConfigDouble wireResistance = d(0.01d, 0.0001d, "wireResistance", "[in Ohms / Meter]");

    public final ConfigFloat bulbBreakAmperage = f(0.5f, 0.0f, "bulbBreakAmperage", "[in Amps]");
    public final ConfigInt maxWireLength = i(128, 8, "wireLength", "[in Meters]");
    public final ConfigInt wiresPerSpool = i(4, 1, 8, "wireItemsPerSpool");
    public final ConfigBool wiresBreak = b(true, "wiresBreak", "Wires break when overloaded");
    public final ConfigBool optimizeGraph = b(true, "optimizeGraph");

    @Override
    public String getName() {
        return "server";
    }

    private ConfigDouble d(double current, double min, String name, String... comments) {
        return new ConfigDouble(name, current, min,  Double.MAX_VALUE, comments);
    }

    public class ConfigDouble extends CValue<Double, ModConfigSpec.DoubleValue> {

        public ConfigDouble(String name, double current, double min, double max, String... comment) {
            super(name, builder -> builder.defineInRange(name, current, min, max), comment);
        }
    }
}
