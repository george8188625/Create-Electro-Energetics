package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.WireType;
import com.simibubi.create.AllTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CEEWireTypes {

    private static final DeferredRegister<WireType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireType, WireType> COPPER = WIRE_TYPES.register("copper", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.COPPER_WIRE_SEGMENT,
            () -> getFromTag(CEETags.COPPER_WIRE), CEEItems.COPPER_WIRE_SPOOL::get,
            0d,
            () -> 0d, () -> null, () -> 5000,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> STANDARD = WIRE_TYPES.register("standard", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.WIRE_SEGMENT,
            CEEItems.INSULATED_WIRE, CEEItems.WIRE_SPOOL::get,
            33_0000,
            CEEConfigs.server().voltageValues.wireMaxVoltage::get, COPPER,
            () -> 3540,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> HEAVILY_INSULATED = WIRE_TYPES.register("heavily_insulated", () -> new WireType(
            CEEConfigs.server().resistanceValues.wireResistance::get,
            CEEPartialModels.HEAVILY_INSULATED_WIRE_SEGMENT,
            CEEItems.HEAVILY_INSULATED_WIRE, CEEItems.HEAVILY_INSULATED_WIRE_SPOOL::get,
            66_0000,
            CEEConfigs.server().voltageValues.heavilyInsulatedWireMaxVoltage::get, COPPER,
            () -> 3540,
            0.7f,
            CEEConfigs.server().maxHeavilyInsulatedWireLength::get));

    public static final DeferredHolder<WireType, WireType> CREATIVE = WIRE_TYPES.register("creative", () -> new WireType(
            () -> 0.00001d,
            CEEPartialModels.CREATIVE_WIRE_SEGMENT,
            () -> Items.AIR, CEEItems.CREATIVE_WIRE_SPOOL::get,
            1e+11d,
            () -> 1e+11d, () -> null, () -> 9999999,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> IRON = WIRE_TYPES.register("iron", () -> new WireType(
            CEEConfigs.server().resistanceValues.ironWireResistance::get,
            CEEPartialModels.IRON_WIRE_SEGMENT, () -> getFromTag(AllTags.commonItemTag("wires/iron")),
            CEEItems.IRON_WIRE_SPOOL::get,
            0,
            () -> 0d, () -> null, () -> 6000,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final DeferredHolder<WireType, WireType> IRON_BUS = WIRE_TYPES.register("iron_bus", () -> new WireType(
            CEEConfigs.server().resistanceValues.ironWireResistance::get,
            CEEPartialModels.IRON_BUS_SEGMENT, Items.IRON_INGOT::asItem,
            CEEItems.IRON_BUS_SPOOL::get,
            0,
            () -> 0d, () -> null, () -> 10000,
            0f,
            CEEConfigs.server().maxBusWireLength::get));

    public static final DeferredHolder<WireType, WireType> ELECTRUM = WIRE_TYPES.register("electrum", () -> new WireType(
            CEEConfigs.server().resistanceValues.electrumWireResistance::get,
            CEEPartialModels.ELECTRUM_WIRE_SEGMENT, () -> getFromTag(CEETags.ELECTRUM_WIRE),
            CEEItems.ELECTRUM_WIRE_SPOOL::get,
            0,
            () -> 0d, () -> null, () -> 3540,
            1f,
            CEEConfigs.server().maxWireLength::get));

    public static final Map<DyeColor, DeferredHolder<WireType, WireType>> COLORED_WIRES = new HashMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_WIRES.put(color, WIRE_TYPES.register("colored_" + color.getName(), () -> new WireType(
                        CEEConfigs.server().resistanceValues.wireResistance::get,
                        CEEPartialModels.COLORED_WIRE_SEGMENTS.get(color),
                        CEEItems.INSULATED_WIRE, CEEItems.WIRE_SPOOL::get,
                        33_0000,
                        CEEConfigs.server().voltageValues.wireMaxVoltage::get, COPPER,
                        () -> 3540,
                        1f,
                        CEEConfigs.server().maxWireLength::get)));
        }
    }

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }

    private static @NotNull Item getFromTag(TagKey<Item> tag) {
        var it = BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator();
        if (!it.hasNext())
            return Items.AIR;
        return it.next().value();
    }
}
