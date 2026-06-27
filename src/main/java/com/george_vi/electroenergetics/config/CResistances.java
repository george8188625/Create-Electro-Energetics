package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CResistances extends ConfigBase {
    public final ConfigDouble motorResistance = d(30d, 0.1d, "motorResistance", "[in Ohms]");
    public final ConfigDouble pumpResistance = d(200d, 0.1d, "pumpResistance", "[in Ohms]");
    public final ConfigDouble bulbResistance = d(1000d, 0.1d, "bulbResistance", "[in Ohms]");
    public final ConfigDouble resistiveHeaterResistance = d(45d, 0.1d, "resistiveHeaterResistance", "[in Ohms]");
    public final ConfigDouble electricTrainAccelerationPowerConsumption = d(40_000d, 0.1d, "electricTrainAccelerationPowerConsumption", "[in Watts]");
    public final ConfigDouble electricTrainCruisePowerConsumption = d(20_000d, 0.1d, "electricTrainCruisePowerConsumption", "[in Watts]");
    public final ConfigDouble wireResistance = d(0.005d, 0.0001d, "wireResistance", "[in Ohms / Meter]");
    public final ConfigDouble electrumWireResistance = d(0.005d, 0.0001d, "electrumWireResistance", "[in Ohms / Meter]");
    public final ConfigDouble ironWireResistance = d(0.01d, 0.0001d, "ironWireResistance", "[in Ohms / Meter]");
    public final ConfigDouble ironRailResistance = d(0.003d, 0.0001d, "ironRailResistance", "[in Ohms / Meter]");
    public final ConfigDouble indicatorBulbResistance = d(1000, 0.0001d, "indicatorBulbResistance", "[in Ohms]");

    @Override
    public String getName() {
        return "Resistances";
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
