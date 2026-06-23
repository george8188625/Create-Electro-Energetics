package com.george_vi.electroenergetics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class CEETags {
    public static final TagKey<Item> ELECTRUM_WIRE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wires/electrum"));
    public static final TagKey<Item> ELECTRUM_NUGGET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/electrum"));
    public static final TagKey<Item> COPPER_WIRE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wires/copper"));
    public static final TagKey<Item> COPPER_NUGGET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/copper"));
    public static final TagKey<Item> COPPER_PLATE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates/copper"));
    public static final TagKey<Item> ZINC_INGOT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/zinc"));
    public static final TagKey<Item> IRON_WIRE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wires/iron"));
    public static final TagKey<Item> IRON_NUGGET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/iron"));
    public static final TagKey<Item> IRON_INGOT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/iron"));
    public static final TagKey<Item> IRON_PLATE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates/iron"));
    public static final TagKey<Item> ELECTRIC_MOTORS = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("electric_motors"));
    public static final TagKey<Fluid> PLANT_OIL = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "plantoil"));
    public static final TagKey<Item> WIRE_ATTACHMENT = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("wire_attachment"));
    public static final TagKey<Item> HIDE_ON_LINEMANS_STICK = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("hide_on_linemans_stick"));
    public static final TagKey<Item> ELECTRICAL_PANEL_ATTACHMENT = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("electrical_panel_attachment"));
    public static final TagKey<Item> WIRE_SPOOLS = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("wire_spools"));
    public static final TagKey<Item> HANGED_WIRE_SPOOLS = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("hanged_wire_spools"));
    public static final TagKey<Item> FUSE_AMPERAGE_SETTING = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("fuse_amperage_setting"));
    public static final TagKey<Item> DYED_ELECTRICAL_PANELS = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("dyed_electrical_panels"));

    // This tag defines items that can be used as wire cutters
    public static final TagKey<Item> WIRE_CUTTERS = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("wire_cutters"));

    // This tag defines items that cause electrical panel covers to be hidden when held
    public static final TagKey<Item> ELECTRICAL_PANEL_SEE_THROUGH = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("electrical_panel_see_through"));

    // This tag defines items that can remove attachments from wires
    public static final TagKey<Item> ATTACHMENT_REMOVAL_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("attachment_removal_item"));

    // This tag defines items that can label nodes
    public static final TagKey<Item> NODE_RENAME_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("node_rename_item"));

    // This tag defines items that can label panel attachments
    public static final TagKey<Item> PANEL_ATTACHMENT_RENAME_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("panel_attachment_rename_item"));

    public static final TagKey<Block> TRAIN_SOUND_MODIFIER = TagKey.create(Registries.BLOCK, CreateElectroEnergetics.rl("train_sound_modifier"));

    // This tag defines the blocks that work as accumulators on trains
    public static final TagKey<Block> TRAIN_ACCUMULATOR = TagKey.create(Registries.BLOCK, CreateElectroEnergetics.rl("train_accumulator"));

    // This tag defines the blocks that provide infinite electric power to trains
    public static final TagKey<Block> TRAIN_CREATIVE_SOURCE = TagKey.create(Registries.BLOCK, CreateElectroEnergetics.rl("train_creative_source"));

    // This tag defines the blocks that can increase the transformer core power rating
    public static final TagKey<Block> TRANSFORMER_HEAT_DISSIPATORS = TagKey.create(Registries.BLOCK, CreateElectroEnergetics.rl("transformer_heat_dissipators"));

    // This tag defines the blocks that can be used as electric motors on trains
    public static final TagKey<Block> TRAIN_ELECTRIC_MOTOR = TagKey.create(Registries.BLOCK, CreateElectroEnergetics.rl("train_electric_motor"));

    // This tag defines the blocks that the ground rod can be placed on
    public static final TagKey<Block> EARTH = TagKey.create(Registries.BLOCK, CreateElectroEnergetics.rl("earth"));

    // This tag defines the item used as an ingredient for the iron rail
    public static final TagKey<Item> IRON_RAIL_COMPONENT = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("iron_rail_component"));

    // This tag defines the item used as an ingredient for the iron bus
    public static final TagKey<Item> IRON_BUS_COMPONENT = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("iron_bus_component"));

    // This tag defines which item can be used as a fuse bypass in the fuse holder
    public static final TagKey<Item> FUSE_BYPASS_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("fuse_bypass_item"));

    // This tag defines which item can be used as a wire damper wire decoration
    public static final TagKey<Item> WIRE_DAMPER_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("wire_damper_item"));

    // This tag defines which items can be used to repair bulbs by right-clicking
    public static final TagKey<Item> BULB_REPAIR_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("bulb_repair_item"));

    // This tag is used to detach interactable items held by a fuse holder.
    public static final TagKey<Item> FUSE_WRENCH = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("fuse_wrench"));

    // This tag is used to make items 'inspect' nodes.
    public static final TagKey<Item> SEE_NODE_DATA = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("see_node_data"));

    // Tags for sable compatibility:
    public static final TagKey<Block> SUPER_LIGHT = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sable", "super_light"));
    public static final TagKey<Block> LIGHT = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sable", "light"));

    // Tags for Farmer's Delight compatibility
    public static final TagKey<Block> FD_HEAT_SOURCES = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath("farmersdelight", "heat_sources"));

    public static @NotNull Item itemFromTag(TagKey<Item> tag) {
        var it = BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator();
        if (!it.hasNext())
            return Items.AIR;
        return it.next().value();
    }
}
