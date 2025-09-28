package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEWireTypes {
    private static final DeferredRegister<WireType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireType, WireType> COPPER = WIRE_TYPES.register("copper", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.COPPER_WIRE_SEGMENT,
            CEEItems.COPPER_WIRE, CEEItems.COPPER_WIRE_SPOOL::get,
            false,
            () -> null, () -> 5000,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> STANDARD = WIRE_TYPES.register("standard", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.WIRE_SEGMENT,
            CEEItems.INSULATED_WIRE, CEEItems.WIRE_SPOOL::get,
            true,
            COPPER::get,
            () -> 3540,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> CREATIVE = WIRE_TYPES.register("creative", () -> new WireType(
            () -> 0.00001d,
            CEEPartialModels.CREATIVE_WIRE_SEGMENT,
            () -> Items.AIR, CEEItems.CREATIVE_WIRE_SPOOL::get,
            true,
            () -> null, () -> 9999999,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> IRON = WIRE_TYPES.register("iron", () -> new WireType(
            CEEConfigs.server().resistanceValues.ironWireResistance::get,
            CEEPartialModels.IRON_WIRE_SEGMENT, CEEItems.IRON_WIRE_STRAND,
            CEEItems.IRON_WIRE_SPOOL::get,
            false,
            () -> null, () -> 6000,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> IRON_BUS = WIRE_TYPES.register("iron_bus", () -> new WireType(
            CEEConfigs.server().resistanceValues.ironWireResistance::get,
            CEEPartialModels.IRON_BUS_SEGMENT, Items.IRON_INGOT::asItem,
            CEEItems.IRON_BUS_SPOOL::get,
            false,
            () -> null, () -> 10000,
            0f,
            CEEConfigs.server().maxBusWireLength::get));

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }
}
