package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.client.ElectricStatsTooltipModifier;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.bundled_wire.BundledWireItem;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.WireType;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleSupplier;

import static com.george_vi.electroenergetics.CreateElectroEnergetics.REGISTRATE;

public class CEEItems {

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final ItemEntry<WireSpoolItem> WIRE_SPOOL = insulatedWireSpoolItem("wire_spool", CEEWireTypes.STANDARD, () -> CEEConfigs.server().voltageValues.wireMaxVoltage.get());

    public static final ItemEntry<WireSpoolItem> HEAVILY_INSULATED_WIRE_SPOOL = insulatedWireSpoolItem("heavily_insulated_wire_spool", CEEWireTypes.HEAVILY_INSULATED, () -> CEEConfigs.server().voltageValues.heavilyInsulatedWireMaxVoltage.get());

    public static final ItemEntry<WireSpoolItem> COPPER_WIRE_SPOOL = simpleWireSpoolItem("copper_wire_spool", CEEWireTypes.COPPER);

    public static final ItemEntry<WireSpoolItem> ELECTRUM_WIRE_SPOOL = simpleWireSpoolItem("electrum_wire_spool", CEEWireTypes.ELECTRUM);

    public static final ItemEntry<WireSpoolItem> IRON_WIRE_SPOOL = simpleWireSpoolItem("iron_wire_spool", CEEWireTypes.IRON);

    public static final ItemEntry<WireSpoolItem> IRON_BUS_SPOOL = simpleWireSpoolItem("iron_bus_spool", CEEWireTypes.IRON_BUS);

    public static final ItemEntry<WireSpoolItem> IRON_RAIL_SPOOL = simpleWireSpoolItem("iron_rail_spool", CEEWireTypes.IRON_RAIL);

    public static final ItemEntry<WireSpoolItem> CREATIVE_WIRE_SPOOL = REGISTRATE.item("creative_wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.CREATIVE))
            .properties(p -> p.rarity(Rarity.EPIC))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(() -> 0.00001d)))
            .register();

    public static final ItemEntry<BundledWireItem> DUPLEX_WIRE_SPOOL = REGISTRATE.item("duplex_wire_spool", properties -> new BundledWireItem(properties))
            .register();

    public static final ItemEntry<EmptySpoolItem> EMPTY_SPOOL = REGISTRATE.item("empty_spool", EmptySpoolItem::new)
            .register();

    public static final ItemEntry<Item> INSULATED_WIRE = REGISTRATE.item("insulated_wire", Item::new)
            .register();

    public static final ItemEntry<Item> HEAVILY_INSULATED_WIRE = REGISTRATE.item("heavily_insulated_wire", Item::new)
            .register();

    public static final ItemEntry<Item> COPPER_WIRE = REGISTRATE.item("copper_wire", Item::new)
            .tag(CEETags.COPPER_WIRE)
            .register();

    public static final ItemEntry<Item> ELECTRUM_WIRE = REGISTRATE.item("electrum_wire", Item::new)
            .tag(CEETags.ELECTRUM_WIRE)
            .register();

    public static final ItemEntry<Item> IRON_WIRE = REGISTRATE.item("iron_wire", Item::new)
            .tag(CEETags.IRON_WIRE)
            .register();

    public static final ItemEntry<Item> IRON_WIRE_STRAND = REGISTRATE.item("iron_wire_strand", Item::new)
            .register();

    public static final ItemEntry<ClampMeterItem> CLAMP_METER = REGISTRATE.item("clamp_meter", ClampMeterItem::new)
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_TRANSFORMER_CORE = REGISTRATE.item("incomplete_transformer_core", SequencedAssemblyItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/transformer_core/incomplete")))
            .register();



    private static @NotNull ItemEntry<WireSpoolItem> simpleWireSpoolItem(String name, DeferredHolder<WireType, WireType> wireType) {
        return REGISTRATE.item(name, properties -> new WireSpoolItem(properties, wireType))
                .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addResistancePerMeter(() -> wireType.get().getResistance())
                        .addMaxCurrent(() -> wireType.get().getMaxTemperature() / 30 + 33.33)))
                .register();
    }

    private static @NotNull ItemEntry<WireSpoolItem> insulatedWireSpoolItem(String name, DeferredHolder<WireType, WireType> wireType, DoubleSupplier insulationVoltage) {
        return REGISTRATE.item(name, properties -> new WireSpoolItem(properties, wireType))
                .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addResistancePerMeter(() -> wireType.get().getResistance())
                        .addMaxVoltage(insulationVoltage)
                        .addMaxCurrent(() -> wireType.get().getMaxTemperature() / 30 + 33.33)))
                .register();
    }

    public static void register() {

    }
}
