package com.george_vi.electroenergetics;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

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
    public static final TagKey<Item> IRON_PLATE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates/iron"));
}
