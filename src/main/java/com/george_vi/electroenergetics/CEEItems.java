package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.ElectricStatsTooltipModifier;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import static com.george_vi.electroenergetics.CreateElecrtoEnergetics.REGISTRATE;

public class CEEItems {
    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final ItemEntry<WireSpoolItem> WIRE_SPOOL = REGISTRATE.item("wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.STANDARD))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(CEEConfigs.server().resistanceValues.wireResistance::get)))
            .register();

    public static final ItemEntry<WireSpoolItem> HEAVILY_INSULATED_WIRE_SPOOL = REGISTRATE.item("heavily_insulated_wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.HEAVILY_INSULATED))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(CEEConfigs.server().resistanceValues.wireResistance::get)))
            .register();

    public static final ItemEntry<WireSpoolItem> COPPER_WIRE_SPOOL = REGISTRATE.item("copper_wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.COPPER))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(CEEConfigs.server().resistanceValues.wireResistance::get)))
            .register();

    public static final ItemEntry<WireSpoolItem> IRON_WIRE_SPOOL = REGISTRATE.item("iron_wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.IRON))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(CEEConfigs.server().resistanceValues.ironWireResistance::get)))
            .register();

    public static final ItemEntry<WireSpoolItem> IRON_BUS_SPOOL = REGISTRATE.item("iron_bus_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.IRON_BUS))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(CEEConfigs.server().resistanceValues.ironWireResistance::get)))
            .register();

    public static final ItemEntry<WireSpoolItem> CREATIVE_WIRE_SPOOL = REGISTRATE.item("creative_wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.CREATIVE))
            .properties(p -> p.rarity(Rarity.EPIC))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistancePerMeter(() -> 0.00001d)))
            .register();

    public static final ItemEntry<EmptySpoolItem> EMPTY_SPOOL = REGISTRATE.item("empty_spool", EmptySpoolItem::new)
            .register();

    public static final ItemEntry<Item> INSULATED_WIRE = REGISTRATE.item("insulated_wire", Item::new)
            .register();

    public static final ItemEntry<Item> HEAVILY_INSULATED_WIRE = REGISTRATE.item("heavily_insulated_wire", Item::new)
            .register();

    public static final ItemEntry<Item> COPPER_WIRE = REGISTRATE.item("copper_wire", Item::new)
            .tag(AllTags.commonItemTag("wires/copper"))
            .register();

    public static final ItemEntry<Item> IRON_WIRE = REGISTRATE.item("iron_wire", Item::new)
            .tag(AllTags.commonItemTag("wires/iron"))
            .register();

    public static final ItemEntry<Item> IRON_WIRE_STRAND = REGISTRATE.item("iron_wire_strand", Item::new)
            .register();

    public static final ItemEntry<ClampMeterItem> CLAMP_METER = REGISTRATE.item("clamp_meter", ClampMeterItem::new)
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_TRANSFORMER_CORE = REGISTRATE.item("incomplete_transformer_core", SequencedAssemblyItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/transformer_core/incomplete")))
            .register();

    public static void register() {

    }
}
