package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CVoltages extends ConfigBase {
    public final ConfigInt trainMinVoltage = i(1900, 0, "trainMinVoltage", "[in Volts]");
    public final ConfigDouble wireMaxVoltage = d(1500, 0, "wireMaxVoltage", "[in Volts]");
    public final ConfigDouble heavilyInsulatedWireMaxVoltage = d(20_000, 0, "heavilyInsulatedWireMaxVoltage", "[in Volts]");
    public final ConfigDouble highVoltageCapacitorVoltage = d(25_000, 0, "highVoltageCapacitorVoltage", "[in Volts]");
    public final ConfigDouble capacitorVoltage = d(500, 0, "capacitorVoltage", "[in Volts]");
    public final ConfigDouble inductorVoltage = d(500, 0, "inductorVoltage", "[in Volts]");
    public final ConfigDouble maxVoltmeterVoltage = d(10_000, 0, "maxVoltmeterVoltage", "[in Volts]");
    public final ConfigDouble maxAmmeterCurrent = d(200, 0, "maxAmmeterCurrent", "[in Amps]");

    @Override
    public String getName() {
        return "Voltages";
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
