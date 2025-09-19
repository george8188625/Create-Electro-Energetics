package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CEEStandardRecipeGen extends RecipeProvider {

    public CEEStandardRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.EMPTY_SPOOL, 8)
                .pattern("S")
                .pattern("s")
                .pattern("S")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/empty_spool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.COPPER_WIRE, 1)
                .pattern("n")
                .pattern("n")
                .pattern("n")
                .define('n', AllTags.commonItemTag("nuggets/copper"))
                .unlockedBy("has_copper_nugget", has(AllTags.commonItemTag("nuggets/copper")))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/copper_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.IRON_WIRE, 1)
                .pattern("n")
                .pattern("n")
                .pattern("n")
                .define('n', AllTags.commonItemTag("nuggets/iron"))
                .unlockedBy("has_iron_nugget", has(AllTags.commonItemTag("nuggets/iron")))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/iron_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.INSULATED_WIRE, 8)
                .pattern("www")
                .pattern("wkw")
                .pattern("www")
                .define('w', AllTags.commonItemTag("wires/copper"))
                .define('k', Items.DRIED_KELP)
                .unlockedBy("has_copper_wire", has(AllTags.commonItemTag("wires/copper")))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/insulated_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.IRON_WIRE_STRAND)
                .pattern("ww")
                .pattern("ww")
                .define('w', CEEItems.IRON_WIRE)
                .unlockedBy("has_iron_wire", has(AllTags.commonItemTag("wires/iron")))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/iron_wire_strand"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.WIRE_SPOOL)
                .pattern(" w ")
                .pattern("wsw")
                .pattern(" w ")
                .define('w', CEEItems.INSULATED_WIRE)
                .define('s', CEEItems.EMPTY_SPOOL)
                .unlockedBy("has_insulated_wire", has(CEEItems.INSULATED_WIRE))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/wire_spool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.IRON_WIRE_SPOOL)
                .pattern(" w ")
                .pattern("wsw")
                .pattern(" w ")
                .define('w', CEEItems.IRON_WIRE_STRAND)
                .define('s', CEEItems.EMPTY_SPOOL)
                .unlockedBy("has_iron_wire_strand", has(CEEItems.IRON_WIRE_STRAND))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/iron_wire_spool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.IRON_BUS_SPOOL)
                .pattern(" w ")
                .pattern("wsw")
                .pattern(" w ")
                .define('w', Items.IRON_INGOT)
                .define('s', CEEItems.EMPTY_SPOOL)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/iron_bus_spool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ALTERNATOR_BRUSHES)
                .pattern(" B ")
                .pattern("CSC")
                .pattern(" B ")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', AllBlocks.SHAFT)
                .define('B', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/alternator_brushes"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ALTERNATOR_ROTOR)
                .pattern("BWB")
                .pattern("WSW")
                .pattern("BWB")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('S', AllBlocks.SHAFT)
                .define('B', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/alternator_rotor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONNECTOR, 4)
                .pattern(" n ")
                .pattern(" A ")
                .pattern(" T ")
                .define('n', AllTags.commonItemTag("nuggets/copper"))
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('T', ItemTags.TERRACOTTA)
                .unlockedBy("has_copper", has(AllTags.commonItemTag("nuggets/copper")))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.DOUBLE_CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/double_connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.TRIPLE_CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/triple_connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.QUAD_CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/quad_connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.QUAD_CONNECTOR)
                .requires(CEEBlocks.DOUBLE_CONNECTOR)
                .requires(CEEBlocks.DOUBLE_CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/quad_connector_from_double"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.AMMETER)
                .pattern("G")
                .define('G', CEEBlocks.VOLTMETER)
                .unlockedBy("has_voltmeter", has(CEEBlocks.VOLTMETER))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/ammeter_from_voltmeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.VOLTMETER)
                .pattern("G")
                .define('G', CEEBlocks.AMMETER)
                .unlockedBy("has_ammeter", has(CEEBlocks.AMMETER))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/voltmeter_from_ammeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.VOLTMETER)
                .pattern(" c ")
                .pattern("CAC")
                .define('c', Items.COMPASS)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/voltmeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.AMMETER)
                .pattern(" c ")
                .pattern("CAC")
                .define('c', Items.CLOCK)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/ammeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.BULB)
                .pattern(" g ")
                .pattern(" w ")
                .pattern("CAC")
                .define('g', Items.GLASS)
                .define('w', AllTags.commonItemTag("wires/copper"))
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/bulb"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.FUSE)
                .pattern("AgA")
                .pattern("CwC")
                .pattern("AgA")
                .define('g', Items.GLASS)
                .define('w', AllTags.commonItemTag("wires/copper"))
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/fuse"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CUT_OFF_SWITCH)
                .pattern(" k ")
                .pattern(" S ")
                .pattern("CAC")
                .define('k', Items.DRIED_KELP)
                .define('S', AllTags.commonItemTag("plates/copper"))
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/cut_off_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.DOUBLE_SWITCH)
                .pattern("SAS")
                .define('S', CEEBlocks.CUT_OFF_SWITCH)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/double_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.REDSTONE_RELAY)
                .pattern(" R ")
                .pattern("ASA")
                .define('S', CEEBlocks.CUT_OFF_SWITCH)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('R', Items.REDSTONE_TORCH)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/redstone_relay"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.MAGNET_BLOCK)
                .pattern(" I ")
                .pattern("IAI")
                .pattern(" I ")
                .define('I', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_industrial_iron_block", has(AllBlocks.INDUSTRIAL_IRON_BLOCK))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/magnet_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ELECTRIC_MOTOR)
                .pattern(" IC")
                .pattern("MRM")
                .pattern("CI ")
                .define('I', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                .define('C', CEEBlocks.CONNECTOR)
                .define('M', CEEBlocks.MAGNET_BLOCK)
                .define('R', CEEBlocks.ALTERNATOR_ROTOR)
                .unlockedBy("has_rotor", has(CEEBlocks.ALTERNATOR_ROTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/electric_motor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ELECTRIC_PUMP)
                .pattern(" MC")
                .pattern("APA")
                .pattern("CW ")
                .define('P', AllBlocks.MECHANICAL_PUMP)
                .define('C', CEEBlocks.CONNECTOR)
                .define('M', CEEBlocks.MAGNET_BLOCK)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('W', CEEItems.WIRE_SPOOL)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/electric_pump"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ENERGY_METER)
                .pattern("C C")
                .pattern("vPa")
                .pattern(" s ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('C', CEEBlocks.DOUBLE_CONNECTOR)
                .define('v', CEEBlocks.VOLTMETER)
                .define('a', CEEBlocks.AMMETER)
                .define('s', AllTags.commonItemTag("plates/iron"))
                .unlockedBy("has_voltmeter", has(CEEBlocks.VOLTMETER))
                .unlockedBy("has_ammeter", has(CEEBlocks.AMMETER))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/energy_meter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.TRI_POLAR_ENERGY_METER)
                .pattern("C C")
                .pattern("vPa")
                .pattern(" s ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('C', CEEBlocks.TRIPLE_CONNECTOR)
                .define('v', CEEBlocks.VOLTMETER)
                .define('a', CEEBlocks.AMMETER)
                .define('s', AllTags.commonItemTag("plates/iron"))
                .unlockedBy("has_voltmeter", has(CEEBlocks.VOLTMETER))
                .unlockedBy("has_ammeter", has(CEEBlocks.AMMETER))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/tri_polar_energy_meter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.GROUND_ROD)
                .pattern("C")
                .pattern("I")
                .pattern("I")
                .define('C', CEEBlocks.CONNECTOR)
                .define('I', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/ground_rod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.POLE_MOUNT, 3)
                .pattern(" ss")
                .pattern("AAA")
                .pattern("A  ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('s', AllTags.commonItemTag("plates/iron"))
                .unlockedBy("has_andesite", has(AllItems.ANDESITE_ALLOY))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/pole_mount"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONCRETE_POLE, 8)
                .pattern("CCC")
                .pattern("CWC")
                .pattern("CCC")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('C', Items.LIGHT_GRAY_CONCRETE)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/concrete_pole"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONVERTER)
                .pattern("C C")
                .pattern("WAW")
                .pattern(" C ")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/converter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.TRANSFORMER)
                .pattern("C C")
                .pattern("WAW")
                .pattern("AAA")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('C', CEEBlocks.DOUBLE_CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/transformer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.VOLTAGE_REGULATOR)
                .pattern(" S ")
                .pattern("WPW")
                .pattern(" T ")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('S', AllBlocks.SHAFT)
                .define('T', CEEBlocks.TRANSFORMER)
                .define('P', AllItems.PRECISION_MECHANISM)
                .unlockedBy("has_transformer", has(CEEBlocks.TRANSFORMER))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/voltage_regulator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.HV_SWITCH)
                .pattern(" Sn")
                .pattern("CAS")
                .pattern("CC ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('n', AllTags.commonItemTag("nuggets/copper"))
                .define('S', AllBlocks.SHAFT)
                .define('C', CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/hv_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CATENARY_HOLDER, 4)
                .pattern("III")
                .pattern(" N ")
                .pattern("III")
                .define('N', AllTags.commonItemTag("nuggets/iron"))
                .define('I', AllTags.commonItemTag("ingots/iron"))
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/catenary_holder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.PANTOGRAPH)
                .pattern(" KK")
                .pattern("CAS")
                .pattern("CC ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('C', CEEBlocks.CONNECTOR)
                .define('K', Items.DRIED_KELP)
                .define('S', AllBlocks.SHAFT)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/pantograph"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.CLAMP_METER)
                .pattern(" WW")
                .pattern("SPS")
                .pattern("A A")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('W', CEEItems.WIRE_SPOOL)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('S', AllItems.STURDY_SHEET)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElecrtoEnergetics.rl("crafting/clamp_meter"));
    }
}

