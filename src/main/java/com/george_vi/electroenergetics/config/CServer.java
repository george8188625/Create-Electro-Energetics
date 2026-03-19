package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CServer extends ConfigBase {

    public final ConfigDouble wattFeTConversionRate = d(34, 0.0001d, "wattFeTConversionRate", "this many watts is one FE/tick");
    public final ConfigInt maxWireLength = i(128, 8, "wireLength", "[in Meters]");
    public final ConfigInt maxHeavilyInsulatedWireLength = i(32, 8, "heavilyInsulatedWireLength", "[in Meters]");
    public final ConfigInt maxBusWireLength = i(8, 1, "busWireLength", "[in Meters]");
    public final ConfigInt maxCatenaryLength = i(64, 8, "catenaryLength", "[in Meters]");
    public final ConfigInt wiresPerSpool = i(4, 1, 8, "wireItemsPerSpool");
    public final ConfigBool wiresBreak = b(true, "wiresBreak", "Wires break when overloaded");
    public final ConfigBool enableElectrocution = b(true, "enableElectrocution", "Wires can cause damage to players and entities");
    public final ConfigBool enableCrossContact = b(false, "enableCrossContact", "Uninsulated wires can connect");
    public final ConfigBool componentDamage = b(true, "componentDamage", "Components get damaged when overloaded");
    public final ConfigBool saveInfrastructureInSchematics = b(true, "saveInfrastructureSDInSchematics");
    public final CResistances resistanceValues = nested(1, CResistances::new, "Resistance Values");
    public final CVoltages voltageValues = nested(1, CVoltages::new, "Voltage Values");
    public final CTrains trainValues = nested(1, CTrains::new, "Train Values");
    public final CSimulation simulationConfig = nested(1, CSimulation::new, "Simulation Configs");

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
