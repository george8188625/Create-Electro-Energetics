package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.buzzer.BuzzerBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.EmergencyStopBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.MomentarySwitchBlock;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.CapacitorBlock;
import com.george_vi.electroenergetics.content.electronic_components.diode.DiodeBlock;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorBlock;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlock;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlock;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlockItem;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerBlock;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographMovementBehaviour;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorBlock;
import com.george_vi.electroenergetics.content.connector.QuadConnectorBlock;
import com.george_vi.electroenergetics.content.connector.TripleConnectorBlock;
import com.george_vi.electroenergetics.content.converter.ConverterBlock;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.CutOffSwitchBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchBlock;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlock;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlock;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlock;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterItem;
import com.george_vi.electroenergetics.content.fuse.FuseBlock;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlock;
import com.george_vi.electroenergetics.content.ground_rod.GroundRodBlock;
import com.george_vi.electroenergetics.content.pole.ConcretePoleBlock;
import com.george_vi.electroenergetics.content.pole.PoleMountBlock;
import com.george_vi.electroenergetics.content.redstone_relay.RedstoneRelayBlock;
import com.george_vi.electroenergetics.content.relay.RelayBlock;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlock;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlock;
import com.george_vi.electroenergetics.content.sign.WarningSignBlock;
import com.george_vi.electroenergetics.content.transformer.RadiatorPanelBlock;
import com.george_vi.electroenergetics.content.transformer.TransformerBlock;
import com.george_vi.electroenergetics.content.transformer.TransformerCoreBlock;
import com.george_vi.electroenergetics.content.voltage_regulator.VoltageRegulatorBlock;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.client.ElectricStatsTooltipModifier;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.gauge.GaugeGenerator;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.george_vi.electroenergetics.CreateElecrtoEnergetics.REGISTRATE;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CEEBlocks {
    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final BlockEntry<ConnectorBlock> CONNECTOR = REGISTRATE.block("connector", ConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> p.directionalBlock(c.get(), bs -> AssetLookup.partialBaseModel(c, p, bs.getValue(ConnectorBlock.STYLE).suffix)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<ConcretePoleBlock> CONCRETE_POLE = REGISTRATE.block("concrete_pole", ConcretePoleBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> BlockStateGen.horizontalAxisBlock(c, p, bs ->
                    !(bs.getValue(ConcretePoleBlock.BOTTOM) || bs.getValue(ConcretePoleBlock.TOP)) ? AssetLookup.partialBaseModel(c, p, "middle") :
                            bs.getValue(ConcretePoleBlock.BOTTOM) && bs.getValue(ConcretePoleBlock.TOP) ? AssetLookup.partialBaseModel(c, p) :
                                    bs.getValue(ConcretePoleBlock.BOTTOM) ? AssetLookup.partialBaseModel(c, p, "bottom") : AssetLookup.partialBaseModel(c, p, "top")
                    ))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block_middle"))
            .build()
            .register();

    public static final BlockEntry<PoleMountBlock> POLE_MOUNT = REGISTRATE.block("pole_mount", PoleMountBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), bs ->
                    bs.getValue(PoleMountBlock.INVERTED) ? AssetLookup.partialBaseModel(c, p, "upside_down") :
                            AssetLookup.partialBaseModel(c, p)
            ))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<DoubleConnectorBlock> DOUBLE_CONNECTOR = REGISTRATE.block("double_connector", DoubleConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate(DirectionalRolledDeviceBlock::generateBlockState)
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<TripleConnectorBlock> TRIPLE_CONNECTOR = REGISTRATE.block("triple_connector", TripleConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate(DirectionalRolledDeviceBlock::generateBlockState)
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<QuadConnectorBlock> QUAD_CONNECTOR = REGISTRATE.block("quad_connector", QuadConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate(BlockStateGen.directionalBlockProvider(false))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<GroundRodBlock> GROUND_ROD = REGISTRATE.block("ground_rod", GroundRodBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
            .blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, s -> p.models().getExistingFile(p.modLoc("block/ground_rod"))))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<CreativeBatteryBlock> CREATIVE_BATTERY = REGISTRATE.block("creative_battery", CreativeBatteryBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .forceSolidOn())
            .blockstate((c, p) -> p.directionalBlock(c.get(), (bs) ->
                    p.models().withExistingParent(c.getName() + (bs.getValue(CreativeBatteryBlock.AC) ? "_ac" : ""), "cube")
                            .texture("down", "block/creative_battery_negative")
                            .texture("up", "block/creative_battery_positive")
                            .texture("east", "block/creative_battery_side")
                            .texture("west", "block/creative_battery_side")
                            .texture("north", bs.getValue(CreativeBatteryBlock.AC) ? "block/creative_battery_side_signs_ac" : "block/creative_battery_side_signs")
                            .texture("south", bs.getValue(CreativeBatteryBlock.AC) ? "block/creative_battery_side_signs_ac" : "block/creative_battery_side_signs")
                            .texture("particle", "block/creative_battery_negative")))
            .transform(pickaxeOnly())
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<FuseBlock> FUSE = REGISTRATE.block("fuse", FuseBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate(BlockStateGen.directionalBlockProvider(false))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<FuseBlock> BROKEN_FUSE = REGISTRATE.block("broken_fuse", FuseBlock::broken)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate(BlockStateGen.directionalBlockProvider(false))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<BulbBlock> BULB = REGISTRATE.block("bulb", BulbBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate(DirectionalRolledDeviceBlock::generateBlockState)
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistance(CEEConfigs.server().resistanceValues.bulbResistance::get)
                    .addVoltage(() -> 300)))
            .build()
            .register();

    public static final BlockEntry<BulbBlock> BROKEN_BULB = REGISTRATE.block("broken_bulb", BulbBlock::broken)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> p.modLoc("block/bulb/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/bulb/item")))
            .build()
            .register();

    public static final BlockEntry<CutOffSwitchBlock> CUT_OFF_SWITCH = REGISTRATE.block("cut_off_switch", properties -> new CutOffSwitchBlock(properties, false))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> bs.getValue(CutOffSwitchBlock.CLOSED) ? p.modLoc("block/cut_off_switch/block_closed") : p.modLoc("block/cut_off_switch/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<CutOffSwitchBlock> DOUBLE_SWITCH = REGISTRATE.block("double_switch", properties -> new CutOffSwitchBlock(properties, true))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> bs.getValue(CutOffSwitchBlock.CLOSED) ? p.modLoc("block/double_switch/block_closed") : p.modLoc("block/double_switch/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<RedstoneRelayBlock> REDSTONE_RELAY = REGISTRATE.block("redstone_relay", RedstoneRelayBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> p.modLoc((bs.getValue(RedstoneRelayBlock.INVERTED) ? "block/redstone_relay/block_inverted" : "block/redstone_relay/block") + (bs.getValue(RedstoneRelayBlock.POWERED) ? "_powered" : ""))))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<EnergyMeterBlock> ENERGY_METER = REGISTRATE.block("energy_meter", properties -> new EnergyMeterBlock(properties, false))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(state.getValue(EnergyMeterBlock.INVERTED) ?
                                    AssetLookup.partialBaseModel(c, p, "inverted") :
                                    AssetLookup.partialBaseModel(c, p))
                            .rotationY((int) state.getValue(EnergyMeterBlock.FACING).getOpposite().toYRot())
                            .build()
            )))
            .transform(pickaxeOnly())
            .item(EnergyMeterItem::new)
            .properties(p -> p.stacksTo(1))
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<EnergyMeterBlock> TRI_POLAR_ENERGY_METER = REGISTRATE.block("tri_polar_energy_meter", properties -> new EnergyMeterBlock(properties, true))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(state.getValue(EnergyMeterBlock.INVERTED) ?
                                    AssetLookup.partialBaseModel(c, p, "inverted") :
                                    AssetLookup.partialBaseModel(c, p))
                            .rotationY((int) state.getValue(EnergyMeterBlock.FACING).getOpposite().toYRot())
                            .build()
            )))
            .transform(pickaxeOnly())
            .item(EnergyMeterItem::new)
            .properties(p -> p.stacksTo(1))
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<HVSwitchBlock> HV_SWITCH = REGISTRATE.block("high_voltage_switch", HVSwitchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .build()
            .register();

    public static final BlockEntry<AccumulatorBlock> ACCUMULATOR = REGISTRATE.block("accumulator", AccumulatorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.horizontalBlockProvider(false))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<ElectricGaugeBlock> AMMETER = REGISTRATE.block("ammeter", ElectricGaugeBlock::ammeter)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .blockstate(new GaugeGenerator()::generate)
            .transform(pickaxeOnly())
            .item()
            .transform(ModelGen.customItemModel("gauge", "_", "item"))
            .register();

    public static final BlockEntry<ElectricGaugeBlock> VOLTMETER = REGISTRATE.block("voltmeter", ElectricGaugeBlock::voltmeter)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .blockstate(new GaugeGenerator()::generate)
            .transform(pickaxeOnly())
            .item()
            .transform(ModelGen.customItemModel("gauge", "_", "item"))
            .register();

    public static final BlockEntry<ElectricMotorBlock> ELECTRIC_MOTOR = REGISTRATE.block("electric_motor", ElectricMotorBlock::new)
            .tag(AllTags.optionalTag(BuiltInRegistries.BLOCK, CreateElecrtoEnergetics.rl("train_electric_motor")))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.directionalBlockProvider(true))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistance(CEEConfigs.server().resistanceValues.motorResistance::get)))
            .build()
            .register();

    public static final BlockEntry<ElectricPumpBlock> ELECTRIC_PUMP = REGISTRATE.block("electric_pump", ElectricPumpBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.directionalBlock(c.get(), bs ->
                    bs.getValue(ElectricPumpBlock.ROLL) ? AssetLookup.partialBaseModel(c, p, "roll") : AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();


    public static final BlockEntry<TransformerBlock> TRANSFORMER = REGISTRATE.block("transformer", TransformerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.horizontalBlockProvider(false))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addMaxPower(() -> 25000)))
            .build()
            .register();

    public static final BlockEntry<TransformerCoreBlock> TRANSFORMER_CORE = REGISTRATE.block("transformer_core", TransformerCoreBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .transform(pickaxeOnly())
            .loot((lt, b) -> lt.add(b, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).add(LootItem.lootTableItem(b.asItem())).when(
                    AnyOfCondition.anyOf(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(b).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TransformerCoreBlock.FACING, Direction.EAST)),
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(b).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TransformerCoreBlock.FACING, Direction.NORTH))
                    )
            ))))
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .build()
            .register();

    public static final BlockEntry<RadiatorPanelBlock> RADIATOR_PANEL = REGISTRATE.block("radiator_panel", RadiatorPanelBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(DirectionalRolledDeviceBlock::generateBlockState)
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<VoltageRegulatorBlock> VOLTAGE_REGULATOR = REGISTRATE.block("voltage_regulator", VoltageRegulatorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<AlternatorRotorBlock> ALTERNATOR_ROTOR = REGISTRATE.block("alternator_rotor", AlternatorRotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, s -> AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<AlternatorBrushesBlock> ALTERNATOR_BRUSHES = REGISTRATE.block("alternator_brushes", AlternatorBrushesBlock::new)
            .tag(AllTags.optionalTag(BuiltInRegistries.BLOCK, CreateElecrtoEnergetics.rl("train_sound_modifier")))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.directionalBlockProvider(true))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .build()
            .register();


    public static final BlockEntry<Block> MAGNET_BLOCK = REGISTRATE.block("magnet", Block::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.simpleCubeAll("magnet"))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry))
            .build()
            .register();

    public static final BlockEntry<ConverterBlock> CONVERTER = REGISTRATE.block("converter", ConverterBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> bs.getValue(ConverterBlock.SOURCE) ? p.modLoc("block/converter/block_source") : p.modLoc("block/converter/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<CatenaryHolderBlock> CATENARY_HOLDER = REGISTRATE.block("catenary_holder", CatenaryHolderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, s -> s.getValue(CatenaryHolderBlock.STYLE).isLow() ? AssetLookup.partialBaseModel(c, p, "low") : AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<PantographBlock> PANTOGRAPH = REGISTRATE.block("pantograph", PantographBlock::new)
            .initialProperties(SharedProperties::netheriteMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.get(), bs -> bs.getValue(PantographBlock.DOUBLE) ? AssetLookup.partialBaseModel(c, p, "double") : AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .onRegister(movementBehaviour(new PantographMovementBehaviour()))
            .item()
            .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .build()
            .register();

    public static final BlockEntry<DiodeBlock> DIODE = REGISTRATE.block("diode", DiodeBlock::new)
            .initialProperties(SharedProperties::netheriteMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .blockstate((c, p) -> p.horizontalBlock(c.get(), p.models().getExistingFile(p.modLoc("block/electronics/diode"))))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/electronics/diode")))
            .build()
            .register();

    public static final BlockEntry<ResistorBlock> RESISTOR = REGISTRATE.block("resistor", properties -> new ResistorBlock(properties, false))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> p.modLoc("block/electronics/resistor")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/electronics/resistor")))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addMaxPower(() -> 1300)))
            .build()
            .register();

    public static final BlockEntry<ResistorBlock> CREATIVE_RESISTOR = REGISTRATE.block("creative_resistor", properties -> new ResistorBlock(properties, true))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> p.modLoc("block/electronics/creative_resistor")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/electronics/creative_resistor")))
            .properties(p -> p.rarity(Rarity.EPIC))
            .build()
            .register();

    public static final BlockEntry<FuseHolderBlock> FUSE_HOLDER = REGISTRATE.block("fuse_holder", FuseHolderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(DirectionalRolledDeviceBlock::generateBlockState)
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<BuzzerBlock> BUZZER = REGISTRATE.block("buzzer", BuzzerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(DirectionalRolledDeviceBlock::generateBlockState)
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addResistance(() -> 1000)
                    .addMaxVoltage(() -> 100)))
            .build()
            .register();

    public static final BlockEntry<RelayBlock> RELAY = REGISTRATE.block("relay", RelayBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> bs.getValue(RelayBlock.INVERTED) ? p.modLoc("block/relay/block_inverted") : p.modLoc("block/relay/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<CapacitorBlock> CAPACITOR = REGISTRATE.block("capacitor", CapacitorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> p.modLoc("block/electronics/capacitor")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/electronics/capacitor")))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addMaxVoltage(() -> 500)))
            .build()
            .register();

    public static final BlockEntry<MomentarySwitchBlock> MOMENTARY_SWITCH = REGISTRATE.block("momentary_switch", MomentarySwitchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> bs.getValue(MomentarySwitchBlock.CLOSED) ? p.modLoc("block/momentary_switch/block_closed") : p.modLoc("block/momentary_switch/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<EmergencyStopBlock> EMERGENCY_STOP_BUTTON = REGISTRATE.block("emergency_stop_button", EmergencyStopBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> bs.getValue(EmergencyStopBlock.ACTIVATED) ? p.modLoc("block/emergency_stop_button/block_pressed") : p.modLoc("block/emergency_stop_button/block")))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<IndicatorBulbBlock> INDICATOR_BULB = REGISTRATE.block("indicator_bulb", IndicatorBulbBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> DirectionalRolledDeviceBlock.generateBlockState(c, p, bs -> p.modLoc("block/indicator_bulb/block_" + bs.getValue(IndicatorBulbBlock.SIDE))))
            .transform(pickaxeOnly())
            .loot((lt, b) -> lt.add(b, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).add(LootItem.lootTableItem(b.asItem())).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(IndicatorBulbBlock.SIDE, 2))))))) // why are loot table gens so looong wtf
            .item(IndicatorBulbBlockItem::new)
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .build()
            .register();

    public static final BlockEntry<WarningSignBlock> HIGH_VOLTAGE_SIGN = REGISTRATE.block("high_voltage_sign", WarningSignBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), bs -> bs.getValue(WarningSignBlock.ATTACHED) ? AssetLookup.partialBaseModel(c, p, "attached") : AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<WarningSignBlock> ELECTRIC_SHOCK_SIGN = REGISTRATE.block("electric_shock_sign", WarningSignBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), bs -> bs.getValue(WarningSignBlock.ATTACHED) ? AssetLookup.partialBaseModel(c, p, "attached") : AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<WarningSignBlock> GROUNDING_SIGN = REGISTRATE.block("grounding_sign", WarningSignBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), bs -> bs.getValue(WarningSignBlock.ATTACHED) ? AssetLookup.partialBaseModel(c, p, "attached") : AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<PotentiometerBlock> POTENTIOMETER = REGISTRATE.block("potentiometer", PotentiometerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .onRegister(i -> ElectricStatsTooltipModifier.ALL_ENTRIES.register(i, new ElectricStatsTooltipModifier.ElectricStatSet()
                    .addMaxPower(() -> 1300)))
            .build()
            .register();

    public static void register() {

    }
}
