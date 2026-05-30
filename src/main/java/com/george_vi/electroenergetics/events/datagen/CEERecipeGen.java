package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.data.internal.NeoForgeItemTagsProvider;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public class CEERecipeGen extends RecipeProvider {

    public CEERecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
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
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/empty_spool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.COPPER_WIRE, 1)
                .pattern("n")
                .pattern("n")
                .pattern("n")
                .define('n', CEETags.COPPER_NUGGET)
                .unlockedBy("has_copper_nugget", has(CEETags.COPPER_NUGGET))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/copper_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.IRON_WIRE, 1)
                .pattern("n")
                .pattern("n")
                .pattern("n")
                .define('n', CEETags.IRON_NUGGET)
                .unlockedBy("has_iron_nugget", has(CEETags.IRON_NUGGET))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/iron_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.ELECTRUM_WIRE, 1)
                .pattern("n")
                .pattern("n")
                .pattern("n")
                .define('n', CEETags.ELECTRUM_NUGGET)
                .unlockedBy("has_electrum_nugget", has(CEETags.ELECTRUM_NUGGET))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(CEETags.ELECTRUM_NUGGET))), CreateElectroEnergetics.rl("crafting/electrum_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.INSULATED_WIRE, 8)
                .pattern("www")
                .pattern("wkw")
                .pattern("www")
                .define('w', CEETags.COPPER_WIRE)
                .define('k', Items.DRIED_KELP)
                .unlockedBy("has_copper_wire", has(CEETags.COPPER_WIRE))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/insulated_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.HEAVILY_INSULATED_WIRE, 4)
                .pattern("pwp")
                .pattern("wkw")
                .pattern("pwp")
                .define('w', CEEItems.INSULATED_WIRE)
                .define('k', Items.DRIED_KELP)
                .define('p', Items.PAPER)
                .unlockedBy("has_insulated_wire", has(CEEItems.INSULATED_WIRE))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/heavily_insulated_wire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.IRON_WIRE_STRAND)
                .pattern("ww")
                .pattern("ww")
                .define('w', CEEItems.IRON_WIRE)
                .unlockedBy("has_iron_wire", has(CEETags.IRON_WIRE))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/iron_wire_strand"));

        conditionalWireSpool(recipeOutput, CEEItems.ELECTRUM_WIRE_SPOOL, CEETags.ELECTRUM_WIRE, CEETags.ELECTRUM_NUGGET);
        wireSpool(recipeOutput, CEEItems.HEAVILY_INSULATED_WIRE_SPOOL, CEEItems.HEAVILY_INSULATED_WIRE);
        wireSpool(recipeOutput, CEEItems.WIRE_SPOOL, CEEItems.INSULATED_WIRE);
        wireSpool(recipeOutput, CEEItems.COPPER_WIRE_SPOOL, CEETags.COPPER_WIRE);
        wireSpool(recipeOutput, CEEItems.IRON_WIRE_SPOOL, CEEItems.IRON_WIRE_STRAND);
        wireSpool(recipeOutput, CEEItems.IRON_BUS_SPOOL, CEETags.IRON_BUS_COMPONENT);
        wireSpool(recipeOutput, CEEItems.IRON_RAIL_SPOOL, CEETags.IRON_RAIL_COMPONENT);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ALTERNATOR_BRUSHES)
                .pattern(" C ")
                .pattern("SMB")
                .pattern(" C ")
                .define('C', CEEBlocks.CONNECTOR)
                .define('M', CEEItems.COMMUTATOR)
                .define('S', AllBlocks.SHAFT)
                .define('B', Items.IRON_BARS)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/alternator_brushes"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ALTERNATOR_ROTOR)
                .pattern("WAW")
                .pattern("ASA")
                .pattern("WAW")
                .define('W', CEEItems.COPPER_WIRE_SPOOL)
                .define('S', AllBlocks.SHAFT)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/alternator_rotor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONNECTOR, 4)
                .pattern(" n ")
                .pattern(" A ")
                .pattern(" T ")
                .define('n', CEETags.COPPER_NUGGET)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('T', ItemTags.TERRACOTTA)
                .unlockedBy("has_copper_nugget", has(CEETags.COPPER_NUGGET))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/connector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.INSULATOR, 6)
                .pattern(" T ")
                .pattern(" A ")
                .pattern(" T ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('T', ItemTags.TERRACOTTA)
                .unlockedBy("has_terracotta", has(ItemTags.TERRACOTTA))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/insulator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONNECTOR, 2)
                .pattern("C")
                .define('C', CEEBlocks.DOUBLE_CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/connector_from_double"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONNECTOR, 3)
                .pattern("C")
                .define('C', CEEBlocks.TRIPLE_CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/connector_from_triple"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONNECTOR, 4)
                .pattern("C")
                .define('C', CEEBlocks.QUAD_CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/connector_from_quad"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.DOUBLE_CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/double_connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.TRIPLE_CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/triple_connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.QUAD_CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .requires(CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/quad_connector"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.QUAD_CONNECTOR)
                .requires(CEEBlocks.DOUBLE_CONNECTOR)
                .requires(CEEBlocks.DOUBLE_CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/quad_connector_from_double"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.AMMETER)
                .pattern("G")
                .define('G', CEEBlocks.VOLTMETER)
                .unlockedBy("has_voltmeter", has(CEEBlocks.VOLTMETER))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/ammeter_from_voltmeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.VOLTMETER)
                .pattern("G")
                .define('G', CEEBlocks.AMMETER)
                .unlockedBy("has_ammeter", has(CEEBlocks.AMMETER))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/voltmeter_from_ammeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.VOLTMETER)
                .pattern(" c ")
                .pattern("CAC")
                .define('c', Items.COMPASS)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/voltmeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.AMMETER)
                .pattern(" c ")
                .pattern("CAC")
                .define('c', Items.CLOCK)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/ammeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.BULB)
                .pattern(" g ")
                .pattern(" w ")
                .pattern("CAC")
                .define('g', Items.GLASS)
                .define('w', CEETags.COPPER_WIRE)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/bulb"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.INDICATOR_BULB)
                .pattern("CEC")
                .define('E', AllItems.ELECTRON_TUBE)
                .define('C', CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/indicator_bulb"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.FUSE)
                .pattern("AgA")
                .pattern("CwC")
                .pattern("AgA")
                .define('g', Items.GLASS)
                .define('w', CEETags.COPPER_WIRE)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/fuse"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CUT_OFF_SWITCH)
                .pattern(" k ")
                .pattern(" S ")
                .pattern("CAC")
                .define('k', Items.DRIED_KELP)
                .define('S', CEETags.COPPER_PLATE)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/cut_off_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.DOUBLE_SWITCH)
                .pattern("SAS")
                .define('S', CEEBlocks.CUT_OFF_SWITCH)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/double_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.REDSTONE_RELAY)
                .pattern(" R ")
                .pattern("ASA")
                .define('S', CEEBlocks.CUT_OFF_SWITCH)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('R', Items.REDSTONE_TORCH)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/redstone_relay"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.RELAY)
                .pattern(" W ")
                .pattern("ASA")
                .define('S', CEEBlocks.CUT_OFF_SWITCH)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('W', CEEItems.WIRE_SPOOL)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/relay"));


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.MAGNET_BLOCK, 4)
                .pattern(" I ")
                .pattern("IAI")
                .pattern(" I ")
                .define('I', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_industrial_iron_block", has(AllBlocks.INDUSTRIAL_IRON_BLOCK))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/magnet_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.COMMUTATOR)
                .pattern("ASA")
                .pattern("CSC")
                .pattern("ASA")
                .define('S', AllBlocks.SHAFT)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('C', CEETags.COPPER_PLATE)
                .unlockedBy("has_industrial_iron_block", has(AllBlocks.INDUSTRIAL_IRON_BLOCK))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/commutator"));

        for (DyeColor color : DyeColor.values()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ELECTRIC_MOTORS[color.ordinal()])
                    .pattern("Cc ")
                    .pattern("RMD")
                    .pattern("Cc ")
                    .define('C', CEEBlocks.CONNECTOR)
                    .define('c', CEEItems.COPPER_WIRE_SPOOL)
                    .define('M', AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .define('R', CEEItems.COMMUTATOR)
                    .define('D', DyeItem.byColor(color))
                    .unlockedBy("has_rotor", has(CEEBlocks.ALTERNATOR_ROTOR))
                    .save(recipeOutput, CreateElectroEnergetics.rl("crafting/" + color.getSerializedName() + "_electric_motor"));

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEEBlocks.ELECTRIC_MOTORS[color.ordinal()])
                    .requires(CEETags.ELECTRIC_MOTORS)
                    .requires(DyeItem.byColor(color))
                    .unlockedBy("has_electric_motors", has(CEETags.ELECTRIC_MOTORS))
                    .save(recipeOutput, CreateElectroEnergetics.rl("crafting/" + color.getSerializedName() + "_electric_motor_dye"));
        }

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
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/electric_pump"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.STATOR)
                .pattern("AS")
                .pattern("MS")
                .pattern("AS")
                .define('M', CEEBlocks.MAGNET_BLOCK)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('S', CEETags.IRON_PLATE)
                .unlockedBy("has_magnet_block", has(CEEBlocks.MAGNET_BLOCK))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/stator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ENERGY_METER)
                .pattern("C C")
                .pattern("vPa")
                .pattern(" s ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('C', CEEBlocks.DOUBLE_CONNECTOR)
                .define('v', CEEBlocks.VOLTMETER)
                .define('a', CEEBlocks.AMMETER)
                .define('s', CEETags.IRON_PLATE)
                .unlockedBy("has_voltmeter", has(CEEBlocks.VOLTMETER))
                .unlockedBy("has_ammeter", has(CEEBlocks.AMMETER))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/energy_meter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.TRI_POLAR_ENERGY_METER)
                .pattern("C C")
                .pattern("vPa")
                .pattern(" s ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('C', CEEBlocks.TRIPLE_CONNECTOR)
                .define('v', CEEBlocks.VOLTMETER)
                .define('a', CEEBlocks.AMMETER)
                .define('s', CEETags.IRON_PLATE)
                .unlockedBy("has_voltmeter", has(CEEBlocks.VOLTMETER))
                .unlockedBy("has_ammeter", has(CEEBlocks.AMMETER))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/tri_polar_energy_meter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.GROUND_ROD)
                .pattern("C")
                .pattern("I")
                .pattern("I")
                .define('C', CEEBlocks.CONNECTOR)
                .define('I', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/ground_rod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.POLE_MOUNT, 3)
                .pattern(" ss")
                .pattern("AAA")
                .pattern("A  ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('s', CEETags.IRON_PLATE)
                .unlockedBy("has_andesite", has(AllItems.ANDESITE_ALLOY))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/pole_mount"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONCRETE_POLE, 8)
                .pattern("CCC")
                .pattern("CWC")
                .pattern("CCC")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('C', Items.LIGHT_GRAY_CONCRETE)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/concrete_pole"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CONVERTER)
                .pattern("C C")
                .pattern("WAW")
                .pattern(" C ")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/converter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.TRANSFORMER)
                .pattern("C C")
                .pattern("WOW")
                .pattern("T T")
                .define('W', CEEItems.COPPER_WIRE_SPOOL)
                .define('C', CEEBlocks.DOUBLE_CONNECTOR)
                .define('T', CEEItems.TRANSFORMER_CORE_LAMINATION)
                .define('O', CEEFluids.TRANSFORMER_OIL.getBucket().orElseThrow())
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/transformer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.VOLTAGE_REGULATOR)
                .pattern(" P ")
                .pattern("WTW")
                .pattern(" O ")
                .define('W', CEEItems.COPPER_WIRE_SPOOL)
                .define('T', CEEBlocks.TRANSFORMER_CORE)
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('O', CEEFluids.TRANSFORMER_OIL.getBucket().orElseThrow())
                .unlockedBy("has_transformer", has(CEEBlocks.TRANSFORMER))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/voltage_regulator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CURRENT_TRANSFORMER)
                .pattern("CAC")
                .pattern("TwT")
                .pattern("CSC")
                .define('A', AllBlocks.ANDESITE_ALLOY_BLOCK)
                .define('w', CEETags.COPPER_WIRE)
                .define('S', CEEItems.WIRE_SPOOL)
                .define('C', CEEBlocks.CONNECTOR)
                .define('T', ItemTags.TERRACOTTA)
                .unlockedBy("has_transformer", has(CEEBlocks.TRANSFORMER))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/current_transformer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.HV_SWITCH)
                .pattern(" Sn")
                .pattern("CAS")
                .pattern("CC ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('n', CEETags.COPPER_NUGGET)
                .define('S', AllBlocks.SHAFT)
                .define('C', CEEBlocks.CONNECTOR)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/hv_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CATENARY_HOLDER, 4)
                .pattern("III")
                .pattern(" N ")
                .pattern("III")
                .define('N', CEETags.IRON_NUGGET)
                .define('I', CEETags.IRON_INGOT)
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/catenary_holder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.PANTOGRAPH)
                .pattern(" KK")
                .pattern("CAS")
                .pattern("CC ")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('C', CEEBlocks.CONNECTOR)
                .define('K', Items.DRIED_KELP)
                .define('S', AllBlocks.SHAFT)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/pantograph"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.RAIL_CONTACT_SHOE)
                .pattern("CKA")
                .pattern("ASA")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('C', CEEBlocks.CONNECTOR)
                .define('K', Items.DRIED_KELP)
                .define('S', AllBlocks.SHAFT)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/rail_contact_shoe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.CLAMP_METER)
                .pattern(" WW")
                .pattern("SPS")
                .pattern("A A")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('W', CEEItems.WIRE_SPOOL)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('S', AllItems.STURDY_SHEET)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/clamp_meter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.BUZZER)
                .pattern(" S ")
                .pattern("CWC")
                .pattern(" A ")
                .define('W', CEEItems.WIRE_SPOOL)
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('S', CEETags.IRON_PLATE)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/buzzer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.MOMENTARY_SWITCH)
                .pattern(" K ")
                .pattern(" B ")
                .pattern("CAC")
                .define('C', CEEBlocks.CONNECTOR)
                .define('K', Items.DRIED_KELP)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('B', ItemTags.BUTTONS)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/momentary_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.EMERGENCY_STOP_BUTTON)
                .pattern(" R ")
                .pattern(" B ")
                .pattern("CAC")
                .define('C', CEEBlocks.CONNECTOR)
                .define('R', Items.RED_CONCRETE)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('B', ItemTags.BUTTONS)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/emergency_stop_button"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.RESISTOR)
                .pattern(" S ")
                .pattern("CcC")
                .pattern(" S ")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('c', Items.COAL)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/resistor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.POTENTIOMETER)
                .pattern(" S ")
                .pattern("ccc")
                .pattern("CCC")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', AllBlocks.SHAFT.asItem())
                .define('c', Items.COAL)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/potentiometer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.DIODE)
                .pattern(" S ")
                .pattern("CQC")
                .pattern(" S ")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('Q', Items.QUARTZ)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/diode"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.HV_CAPACITOR)
                .pattern("CSC")
                .pattern("ccc")
                .pattern("ccc")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('c', CEEBlocks.CAPACITOR)
                .unlockedBy("has_capacitor", has(CEEBlocks.CAPACITOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/high_voltage_capacitor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.CAPACITOR)
                .pattern("ScS")
                .pattern("SZS")
                .pattern("C C")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('Z', CEETags.ZINC_INGOT)
                .define('c', CEETags.COPPER_PLATE)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/capacitor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.INDUCTOR)
                .pattern(" S ")
                .pattern("CcC")
                .pattern(" S ")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('c', CEEItems.COPPER_WIRE_SPOOL)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/inductor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ACCUMULATOR)
                .pattern("CAC")
                .pattern("ZAc")
                .pattern("ZAc")
                .define('C', CEEBlocks.CONNECTOR)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('Z', CEETags.ZINC_INGOT)
                .define('c', CEETags.COPPER_PLATE)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/accumulator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.FUSE_HOLDER)
                .pattern("C C")
                .pattern("ASA")
                .pattern("C C")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/fuse_holder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.ELECTRICAL_PANEL, 2)
                .pattern("A A")
                .pattern("ASA")
                .pattern("A A")
                .define('S', CEETags.IRON_PLATE)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/electrical_panel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.RADIATOR_PANEL, 4)
                .pattern("ASA")
                .pattern("ASA")
                .pattern("ASA")
                .define('S', CEETags.IRON_PLATE)
                .define('A', AllItems.ANDESITE_ALLOY)
                .unlockedBy("has_andesite_alloy", has(AllItems.ANDESITE_ALLOY))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/radiator_panel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEBlocks.RESISTIVE_HEATER)
                .pattern("ccc")
                .pattern("CAC")
                .pattern("ASA")
                .define('C', CEEBlocks.CONNECTOR)
                .define('S', CEETags.IRON_PLATE)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('c', Items.COAL_BLOCK)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/resistive_heater"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.MINIATURE_MOMENTARY_SWITCH)
                .pattern(" ")
                .pattern("S")
                .pattern("T")
                .define('S', CEEBlocks.MOMENTARY_SWITCH)
                .define('T', ItemTags.TERRACOTTA)
                .unlockedBy("has_connector", has(CEEBlocks.CONNECTOR))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/miniature_momentary_switch"));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(CEETags.IRON_PLATE), RecipeCategory.DECORATIONS, CEEBlocks.HIGH_VOLTAGE_SIGN, 2)
                .unlockedBy("has_iron_plate", has(CEETags.IRON_PLATE))
                .save(recipeOutput, CreateElectroEnergetics.rl("stonecutting/high_voltage_sign"));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(CEETags.IRON_PLATE), RecipeCategory.DECORATIONS, CEEBlocks.ELECTRIC_SHOCK_SIGN, 2)
                .unlockedBy("has_iron_plate", has(CEETags.IRON_PLATE))
                .save(recipeOutput, CreateElectroEnergetics.rl("stonecutting/electric_shock_sign"));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(CEETags.IRON_PLATE), RecipeCategory.DECORATIONS, CEEItems.TRANSFORMER_CORE_LAMINATION, 1)
                .unlockedBy("has_iron_plate", has(CEETags.IRON_PLATE))
                .save(recipeOutput, CreateElectroEnergetics.rl("stonecutting/transformer_core_lamination"));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(CEETags.IRON_PLATE), RecipeCategory.DECORATIONS, CEEBlocks.GROUNDING_SIGN, 2)
                .unlockedBy("has_iron_plate", has(CEETags.IRON_PLATE))
                .save(recipeOutput, CreateElectroEnergetics.rl("stonecutting/grounding_sign"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEEItems.WIRE_DAMPER)
                .pattern(" S ")
                .pattern(" C ")
                .pattern("A A")
                .define('C', Tags.Items.CHAINS)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('S', CEETags.IRON_PLATE)
                .unlockedBy("has_wire_spool", has(CEEItems.WIRE_SPOOL))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/wire_damper"));

        sequencedAssembly("transformer_core", b -> b.require(CEEItems.TRANSFORMER_CORE_LAMINATION)
                        .transitionTo(CEEItems.INCOMPLETE_TRANSFORMER_CORE)
                        .addOutput(CEEBlocks.TRANSFORMER_CORE.asStack(), 1)
                        .loops(1)
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEEItems.TRANSFORMER_CORE_LAMINATION)))
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEEItems.TRANSFORMER_CORE_LAMINATION)))
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEEItems.TRANSFORMER_CORE_LAMINATION)))
                        .addStep(PressingRecipe::new, rb -> rb)
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEEItems.COPPER_WIRE_SPOOL)))
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEEItems.COPPER_WIRE_SPOOL)))
                , recipeOutput);

        sequencedAssembly("miniature_circuit_breaker", b -> b.require(ItemTags.TERRACOTTA)
                        .transitionTo(CEEItems.INCOMPLETE_MINIATURE_CIRCUIT_BREAKER)
                        .addOutput(CEEItems.MINIATURE_CIRCUIT_BREAKER.asStack(), 1)
                        .loops(1)
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(AllItems.PRECISION_MECHANISM)))
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEETags.COPPER_NUGGET)))
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEETags.COPPER_WIRE)))
                        .addStep(DeployerApplicationRecipe::new,
                                rb -> rb.require(Ingredient.of(CEEItems.COPPER_WIRE_SPOOL)))
                        .addStep(PressingRecipe::new, rb -> rb)
                , recipeOutput);


    }

    protected void sequencedAssembly(String name, UnaryOperator<SequencedAssemblyRecipeBuilder> transform, RecipeOutput recipeOutput) {
        transform.apply(new SequencedAssemblyRecipeBuilder(CreateElectroEnergetics.rl(name))).build(recipeOutput);
    }

    public static void wireSpool(RecipeOutput recipeOutput, ItemEntry<WireSpoolItem> spool, ItemEntry<Item> wire) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, spool)
                .pattern(" w ")
                .pattern("wsw")
                .pattern(" w ")
                .define('w', wire)
                .define('s', CEEItems.EMPTY_SPOOL)
                .unlockedBy("has_" + wire.getRegisteredName(), has(wire))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/" + spool.getKey().location().getPath()));
    }

    public static void wireSpool(RecipeOutput recipeOutput, ItemEntry<WireSpoolItem> spool, TagKey<Item> wire) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, spool)
                .pattern(" w ")
                .pattern("wsw")
                .pattern(" w ")
                .define('w', wire)
                .define('s', CEEItems.EMPTY_SPOOL)
                .unlockedBy("has_" + wire.location().getPath(), has(wire))
                .save(recipeOutput, CreateElectroEnergetics.rl("crafting/" + spool.getKey().location().getPath()));
    }

    public static void conditionalWireSpool(RecipeOutput recipeOutput, ItemEntry<WireSpoolItem> spool, TagKey<Item> wire, TagKey<Item> condition) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, spool)
                .pattern(" w ")
                .pattern("wsw")
                .pattern(" w ")
                .define('w', wire)
                .define('s', CEEItems.EMPTY_SPOOL)
                .unlockedBy("has_" + wire.location().getPath(), has(wire))
                .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(condition))), CreateElectroEnergetics.rl("crafting/" + spool.getKey().location().getPath()));
    }
}

