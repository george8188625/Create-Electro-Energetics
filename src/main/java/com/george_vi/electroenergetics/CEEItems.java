package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import static com.george_vi.electroenergetics.CreateElecrtoEnergetics.REGISTRATE;

public class CEEItems {
    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final ItemEntry<WireSpoolItem> WIRE_SPOOL = REGISTRATE.item("wire_spool", properties -> new WireSpoolItem(properties, CEEWireTypes.STANDARD))
            .register();
    public static final ItemEntry<EmptySpoolItem> EMPTY_SPOOL = REGISTRATE.item("empty_spool", EmptySpoolItem::new)
            .register();
    public static final ItemEntry<Item> INSULATED_WIRE = REGISTRATE.item("insulated_wire", Item::new)
            .register();
    public static final ItemEntry<Item> COPPER_WIRE = REGISTRATE.item("copper_wire", Item::new)
            .tag(AllTags.commonItemTag("wires/copper"))
            .register();

    public static final ItemEntry<ClampMeterItem> CLAMP_METER = REGISTRATE.item("clamp_meter", ClampMeterItem::new)
            .register();

    public static void register() {

    }
}
