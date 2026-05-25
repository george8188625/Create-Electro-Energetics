package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.world.item.DyeColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEWireTypes {

    private static final DeferredRegister<WireType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_TYPE, CreateElectroEnergetics.ID);

    public static final DeferredHolder<WireType, WireType> COPPER = WIRE_TYPES.register("copper", () -> new WireType.Builder(CEEPartialModels.COPPER_WIRE_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.wireResistance::get)
            .droppedTag(CEETags.COPPER_WIRE)
            .spoolItem(CEEItems.COPPER_WIRE_SPOOL::get)
            .maxTemperature(() -> 5000)
            .maxLength(CEEConfigs.server().maxWireLength::get)
            .build());

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<WireType, WireType>[] COLORED_WIRES = new DeferredHolder[DyeColor.values().length];

    public static final DeferredHolder<WireType, WireType> STANDARD = WIRE_TYPES.register("standard", () -> new WireType.Builder(CEEPartialModels.WIRE_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.wireResistance::get)
            .droppedItem(CEEItems.INSULATED_WIRE)
            .spoolItem(CEEItems.WIRE_SPOOL::get)
            .maxInsulationVoltage(CEEConfigs.server().voltageValues.wireMaxVoltage::get)
            .maxTemperature(() -> 3540)
            .replaceOnOverheated(COPPER)
            .insulationResistance(330_000)
            .maxLength(CEEConfigs.server().maxWireLength::get)
            .dyeable(COLORED_WIRES)
            .build());

    public static final DeferredHolder<WireType, WireType> DUPLEX = WIRE_TYPES.register("duplex", () -> new WireType.Builder(CEEPartialModels.DUPLEX_WIRE_SEGMENT)
            .resistance(() -> 1e+11d)
            .maxLength(CEEConfigs.server().maxBundledWireLength::get)
            .decorative()
            .build());

    public static final DeferredHolder<WireType, WireType> BUNDLE_CONDUCTOR = WIRE_TYPES.register("bundle_conductor", () -> new WireType.Builder(CEEPartialModels.DUPLEX_WIRE_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.wireResistance::get)
            .droppedItem(CEEItems.INSULATED_WIRE)
            .spoolItem(CEEItems.DUPLEX_WIRE_SPOOL::get)
            .maxInsulationVoltage(CEEConfigs.server().voltageValues.wireMaxVoltage::get)
            .maxTemperature(() -> Double.MAX_VALUE)
            .maxLength(() -> Integer.MAX_VALUE)
            .invulnerable()
            .build());

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<WireType, WireType>[] COLORED_HEAVILY_INSULATED_WIRES = new DeferredHolder[DyeColor.values().length];

    public static final DeferredHolder<WireType, WireType> HEAVILY_INSULATED = WIRE_TYPES.register("heavily_insulated", () -> new WireType.Builder(CEEPartialModels.HEAVILY_INSULATED_WIRE_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.wireResistance::get)
            .droppedItem(CEEItems.HEAVILY_INSULATED_WIRE)
            .spoolItem(CEEItems.HEAVILY_INSULATED_WIRE_SPOOL::get)
            .maxInsulationVoltage(CEEConfigs.server().voltageValues.heavilyInsulatedWireMaxVoltage::get)
            .maxTemperature(() -> 5000)
            .replaceOnOverheated(COPPER)
            .insulationResistance(660_000)
            .maxLength(CEEConfigs.server().maxHeavilyInsulatedWireLength::get)
            .sag(0.7f)
            .thickness(3/16f)
            .dyeable(COLORED_HEAVILY_INSULATED_WIRES)
            .build());

    public static final DeferredHolder<WireType, WireType> CREATIVE = WIRE_TYPES.register("creative", () -> new WireType.Builder(CEEPartialModels.CREATIVE_WIRE_SEGMENT)
            .resistance(() -> 0.00001d)
            .spoolItem(CEEItems.CREATIVE_WIRE_SPOOL::get)
            .maxInsulationVoltage(() -> 1e+11d)
            .maxTemperature(() -> 1e+11d)
            .insulationResistance(1e+11d)
            .maxLength(CEEConfigs.server().maxWireLength::get)
            .build());

    public static final DeferredHolder<WireType, WireType> IRON = WIRE_TYPES.register("iron", () -> new WireType.Builder(CEEPartialModels.IRON_WIRE_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.ironWireResistance::get)
            .droppedItem(CEEItems.IRON_WIRE_STRAND)
            .spoolItem(CEEItems.IRON_WIRE_SPOOL::get)
            .maxTemperature(() -> 6000)
            .maxLength(CEEConfigs.server().maxWireLength::get)
            .build());

    public static final DeferredHolder<WireType, WireType> IRON_BUS = WIRE_TYPES.register("iron_bus", () -> new WireType.Builder(CEEPartialModels.IRON_BUS_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.ironWireResistance::get)
            .droppedTag(CEETags.IRON_BUS_COMPONENT)
            .spoolItem(CEEItems.IRON_BUS_SPOOL::get)
            .maxTemperature(() -> 10000)
            .maxLength(CEEConfigs.server().maxBusWireLength::get)
            .sag(0f)
            .thickness(2/16f)
            .build());

    public static final DeferredHolder<WireType, WireType> IRON_RAIL = WIRE_TYPES.register("iron_rail", () -> new WireType.Builder(CEEPartialModels.IRON_RAIL_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.ironRailResistance::get)
            .droppedTag(CEETags.IRON_RAIL_COMPONENT)
            .spoolItem(CEEItems.IRON_RAIL_SPOOL::get)
            .maxTemperature(() -> 15000)
            .maxLength(CEEConfigs.server().maxBusWireLength::get)
            .sag(0f)
            .thickness(4/16f)
            .build());

    public static final DeferredHolder<WireType, WireType> ELECTRUM = WIRE_TYPES.register("electrum", () -> new WireType.Builder(CEEPartialModels.ELECTRUM_WIRE_SEGMENT)
            .resistance(CEEConfigs.server().resistanceValues.electrumWireResistance::get)
            .droppedTag(CEETags.ELECTRUM_WIRE)
            .spoolItem(CEEItems.ELECTRUM_WIRE_SPOOL::get)
            .maxTemperature(() -> 3540)
            .maxLength(CEEConfigs.server().maxWireLength::get)
            .build());


    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_WIRES[color.ordinal()] =
                    WIRE_TYPES.register("colored_" + color.getSerializedName(),
                    () -> new WireType.Builder(CEEPartialModels.COLORED_WIRE_SEGMENTS[color.ordinal()])
                    .resistance(CEEConfigs.server().resistanceValues.wireResistance::get)
                    .droppedItem(CEEItems.INSULATED_WIRE)
                    .spoolItem(CEEItems.WIRE_SPOOL::get)
                    .maxInsulationVoltage(CEEConfigs.server().voltageValues.wireMaxVoltage::get)
                    .maxTemperature(() -> 3540)
                    .replaceOnOverheated(COPPER)
                    .insulationResistance(330_000)
                    .maxLength(CEEConfigs.server().maxWireLength::get)
                    .dyeable(COLORED_WIRES, color)
                    .build());

            COLORED_HEAVILY_INSULATED_WIRES[color.ordinal()] =
                    WIRE_TYPES.register(color.getSerializedName() + "_heavily_insulated",
                    () -> new WireType.Builder(CEEPartialModels.COLORED_HEAVILY_INSULATED_WIRE_SEGMENTS[color.ordinal()])
                    .resistance(CEEConfigs.server().resistanceValues.wireResistance::get)
                    .droppedItem(CEEItems.HEAVILY_INSULATED_WIRE)
                    .spoolItem(CEEItems.HEAVILY_INSULATED_WIRE_SPOOL::get)
                    .maxInsulationVoltage(CEEConfigs.server().voltageValues.heavilyInsulatedWireMaxVoltage::get)
                    .maxTemperature(() -> 5000)
                    .replaceOnOverheated(COPPER)
                    .insulationResistance(660_000)
                    .maxLength(CEEConfigs.server().maxHeavilyInsulatedWireLength::get)
                    .sag(0.7f)
                    .thickness(3/16f)
                    .dyeable(COLORED_HEAVILY_INSULATED_WIRES, color)
                    .build());
        }
    }

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }

}
