package com.george_vi.electroenergetics;

import com.simibubi.create.AllTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class CEETags {
    public static final TagKey<Item> ELECTRUM_WIRE = AllTags.commonItemTag("wires/electrum");
    public static final TagKey<Item> ELECTRUM_NUGGET = AllTags.commonItemTag("nuggets/electrum");
    public static final TagKey<Item> COPPER_WIRE = AllTags.commonItemTag("wires/copper");
    public static final TagKey<Item> COPPER_NUGGET = AllTags.commonItemTag("nuggets/copper");
    public static final TagKey<Item> IRON_WIRE = AllTags.commonItemTag("wires/iron");
    public static final TagKey<Item> IRON_NUGGET = AllTags.commonItemTag("nuggets/iron");
    public static final TagKey<Item> IRON_RAIL_COMPONENT = AllTags.optionalTag(BuiltInRegistries.ITEM, CreateElecrtoEnergetics.rl("iron_rail_component"));
}
