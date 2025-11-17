package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public class CEEWireTypes {
    private static final DeferredRegister<WireType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireType, WireType> COPPER = WIRE_TYPES.register("copper", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.COPPER_WIRE_SEGMENT,
            CEEItems.COPPER_WIRE, CEEItems.COPPER_WIRE_SPOOL::get,
            0,
            () -> null, () -> 5000,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> STANDARD = WIRE_TYPES.register("standard", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.WIRE_SEGMENT,
            CEEItems.INSULATED_WIRE, CEEItems.WIRE_SPOOL::get,
            33_0000,
            COPPER,
            () -> 3540,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> CREATIVE = WIRE_TYPES.register("creative", () -> new WireType(
            () -> 0.00001d,
            CEEPartialModels.CREATIVE_WIRE_SEGMENT,
            () -> Items.AIR, CEEItems.CREATIVE_WIRE_SPOOL::get,
            1e+11d,
            () -> null, () -> 9999999,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> IRON = WIRE_TYPES.register("iron", () -> new WireType(
            CEEConfigs.server().resistanceValues.ironWireResistance::get,
            CEEPartialModels.IRON_WIRE_SEGMENT, CEEItems.IRON_WIRE_STRAND,
            CEEItems.IRON_WIRE_SPOOL::get,
            0,
            () -> null, () -> 6000,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> IRON_BUS = WIRE_TYPES.register("iron_bus", () -> new WireType(
            CEEConfigs.server().resistanceValues.ironWireResistance::get,
            CEEPartialModels.IRON_BUS_SEGMENT, Items.IRON_INGOT::asItem,
            CEEItems.IRON_BUS_SPOOL::get,
            0,
            () -> null, () -> 10000,
            0f,
            CEEConfigs.server().maxBusWireLength::get));

    public static final Map<DyeColor, DeferredHolder<WireType, WireType>> COLORED_WIRES = new HashMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_WIRES.put(color, WIRE_TYPES.register("colored_" + color.getName(), () -> new WireType(
                        CEEConfigs.server().resistanceValues.wireResistance::get,
                        CEEPartialModels.COLORED_WIRE_SEGMENTS.get(color),
                        CEEItems.INSULATED_WIRE, CEEItems.WIRE_SPOOL::get,
                        33_0000,
                        COPPER,
                        () -> 3540,
                        1f,
                        CEEConfigs.server().maxWireLength::get)));
        }
    }

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }
}
