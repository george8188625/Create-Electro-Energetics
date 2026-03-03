package com.george_vi.electroenergetics;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

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
    public static final TagKey<Item> IRON_RAIL_COMPONENT = TagKey.create(Registries.ITEM, CreateElecrtoEnergetics.rl("iron_rail_component"));
    public static final TagKey<Item> IRON_BUS_COMPONENT = TagKey.create(Registries.ITEM, CreateElecrtoEnergetics.rl("iron_bus_component"));
    public static final TagKey<Item> IRON_PLATE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates/iron"));

    public static final TagKey<Block> TRAIN_ELECTRIC_MOTOR = TagKey.create(Registries.BLOCK, CreateElecrtoEnergetics.rl("train_electric_motor"));
    public static final TagKey<Block> TRAIN_ACCUMULATOR = TagKey.create(Registries.BLOCK, CreateElecrtoEnergetics.rl("train_accumulator"));
    public static final TagKey<Block> TRAIN_CREATIVE_SOURCE = TagKey.create(Registries.BLOCK, CreateElecrtoEnergetics.rl("train_creative_source"));
    public static final TagKey<Block> TRAIN_SOUND_MODIFIER = TagKey.create(Registries.BLOCK, CreateElecrtoEnergetics.rl("train_sound_modifier"));
    public static final TagKey<Block> TRANSFORMER_HEAT_DISSIPATORS = TagKey.create(Registries.BLOCK, CreateElecrtoEnergetics.rl("transformer_heat_dissipators"));
    public static final TagKey<Block> EARTH = TagKey.create(Registries.BLOCK, CreateElecrtoEnergetics.rl("earth"));

}
