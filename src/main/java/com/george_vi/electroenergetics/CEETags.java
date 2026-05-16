package com.george_vi.electroenergetics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class CEETags {
    public static final TagKey<Item> ELECTRUM_WIRE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wires/electrum"));;
    public static final TagKey<Item> ELECTRUM_NUGGET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/electrum"));;
    public static final TagKey<Item> COPPER_WIRE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wires/copper"));;
    public static final TagKey<Item> COPPER_NUGGET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/copper"));;
    public static final TagKey<Item> COPPER_PLATE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates/copper"));
    public static final TagKey<Item> ZINC_INGOT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/zinc"));
    public static final TagKey<Item> IRON_WIRE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "wires/iron"));;
    public static final TagKey<Item> IRON_NUGGET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuggets/iron"));;
    public static final TagKey<Item> IRON_INGOT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/iron"));;
    public static final TagKey<Item> IRON_PLATE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates/iron"));
    public static final TagKey<Item> ELECTRIC_MOTORS = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("electric_motors"));

    // This tag defines items that can remove attachments from wires
    public static final TagKey<Item> ATTACHMENT_REMOVAL_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("attachment_removal_item"));

    // This tag defines items that can label nodes
    public static final TagKey<Item> NODE_RENAME_ITEM = TagKey.create(Registries.ITEM, CreateElectroEnergetics.rl("node_rename_item"));

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


    public static @NotNull Item itemFromTag(TagKey<Item> tag) {
        var it = BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator();
        if (!it.hasNext())
            return Items.AIR;
        return it.next().value();
    }
}
