package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
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
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlock;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlock;
import com.george_vi.electroenergetics.content.transformer.TransformerBlock;
import com.george_vi.electroenergetics.content.voltage_regulator.VoltageRegulatorBlock;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.gauge.GaugeGenerator;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
            .blockstate((c, p) -> p.directionalBlock(c.get(), bs -> bs.getValue(ConnectorBlock.STYLE) == ConnectorBlock.Style.LONG ? AssetLookup.partialBaseModel(c, p, "long") :
                    bs.getValue(ConnectorBlock.STYLE) == ConnectorBlock.Style.OUTER ? AssetLookup.partialBaseModel(c, p, "outer") : AssetLookup.partialBaseModel(c, p)))
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
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(DoubleConnectorBlock.ROLL) ?
                                    AssetLookup.partialBaseModel(c, p) :
                                    AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(DoubleConnectorBlock.FACING) == Direction.DOWN ? 180 : state.getValue(DoubleConnectorBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(DoubleConnectorBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(DoubleConnectorBlock.FACING).toYRot() : 0)
                            .build()
            )))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<TripleConnectorBlock> TRIPLE_CONNECTOR = REGISTRATE.block("triple_connector", TripleConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(TripleConnectorBlock.ROLL) ?
                                    AssetLookup.partialBaseModel(c, p) :
                                    AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(TripleConnectorBlock.FACING) == Direction.DOWN ? 180 : state.getValue(TripleConnectorBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(TripleConnectorBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(TripleConnectorBlock.FACING).toYRot() : 0)
                            .build()
            )))
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
            .blockstate((c, p) -> p.directionalBlock(c.get(), p.models().withExistingParent(c.getName(), "cube")
                    .texture("down", "block/creative_battery_negative")
                    .texture("up", "block/creative_battery_positive")
                    .texture("east", "block/creative_battery_side")
                    .texture("west", "block/creative_battery_side")
                    .texture("north", "block/creative_battery_side_signs")
                    .texture("south", "block/creative_battery_side_signs")
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
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(BulbBlock.ROLL) ?
                                    state.getValue(BulbBlock.LIGHT) == 0 ? AssetLookup.partialBaseModel(c, p) : state.getValue(BulbBlock.LIGHT) == 1 ? AssetLookup.partialBaseModel(c, p, "dim") : AssetLookup.partialBaseModel(c, p, "bright") :
                                    state.getValue(BulbBlock.LIGHT) == 0 ? AssetLookup.partialBaseModel(c, p, "roll") : state.getValue(BulbBlock.LIGHT) == 1 ? AssetLookup.partialBaseModel(c, p, "dim_roll") : AssetLookup.partialBaseModel(c, p, "bright_roll"))
                            .rotationX(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                            .build()
                )))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<BulbBlock> BROKEN_BULB = REGISTRATE.block("broken_bulb", BulbBlock::broken)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(BulbBlock.ROLL) ?
                                    AssetLookup.partialBaseModel(c, p) :
                                    AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                            .build()
            )))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<CutOffSwitchBlock> CUT_OFF_SWITCH = REGISTRATE.block("cut_off_switch", properties -> new CutOffSwitchBlock(properties, false))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(CutOffSwitchBlock.ROLL) ?
                                    state.getValue(CutOffSwitchBlock.CLOSED) ? AssetLookup.partialBaseModel(c, p, "closed") : AssetLookup.partialBaseModel(c, p) :
                                    state.getValue(CutOffSwitchBlock.CLOSED) ? AssetLookup.partialBaseModel(c, p, "closed_roll") : AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(CutOffSwitchBlock.FACING) == Direction.DOWN ? 180 : state.getValue(CutOffSwitchBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(CutOffSwitchBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(CutOffSwitchBlock.FACING).toYRot() : 0)
                            .build()
            )))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<CutOffSwitchBlock> DOUBLE_SWITCH = REGISTRATE.block("double_switch", properties -> new CutOffSwitchBlock(properties, true))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(CutOffSwitchBlock.ROLL) ?
                                    state.getValue(CutOffSwitchBlock.CLOSED) ? AssetLookup.partialBaseModel(c, p, "closed") : AssetLookup.partialBaseModel(c, p) :
                                    state.getValue(CutOffSwitchBlock.CLOSED) ? AssetLookup.partialBaseModel(c, p, "closed_roll") : AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(CutOffSwitchBlock.FACING) == Direction.DOWN ? 180 : state.getValue(CutOffSwitchBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(CutOffSwitchBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(CutOffSwitchBlock.FACING).toYRot() : 0)
                            .build()
            )))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<RedstoneRelayBlock> REDSTONE_RELAY = REGISTRATE.block("redstone_relay", RedstoneRelayBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(RedstoneRelayBlock.ROLL) ?
                                    state.getValue(RedstoneRelayBlock.POWERED) ? AssetLookup.partialBaseModel(c, p, "powered") : AssetLookup.partialBaseModel(c, p) :
                                    state.getValue(RedstoneRelayBlock.POWERED) ? AssetLookup.partialBaseModel(c, p, "powered_roll") : AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(RedstoneRelayBlock.FACING) == Direction.DOWN ? 180 : state.getValue(RedstoneRelayBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(RedstoneRelayBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(RedstoneRelayBlock.FACING).toYRot() : 0)
                            .build()
            )))
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
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry()).forAllStates((state ->
                    ConfiguredModel.builder()
                            .modelFile(!state.getValue(ConverterBlock.ROLL) ?
                                    state.getValue(ConverterBlock.SOURCE) ? AssetLookup.partialBaseModel(c, p, "source") : AssetLookup.partialBaseModel(c, p) :
                                    state.getValue(ConverterBlock.SOURCE) ? AssetLookup.partialBaseModel(c, p, "source_roll") : AssetLookup.partialBaseModel(c, p, "roll"))
                            .rotationX(state.getValue(ConverterBlock.FACING) == Direction.DOWN ? 180 : state.getValue(ConverterBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(state.getValue(ConverterBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(ConverterBlock.FACING).toYRot() : 0)
                            .build()
            )))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<CatenaryHolderBlock> CATENARY_HOLDER = REGISTRATE.block("catenary_holder", CatenaryHolderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, s -> AssetLookup.partialBaseModel(c, p)))
            .transform(pickaxeOnly())
            .item()
            .model((c, p) -> p.blockItem(c::getEntry, "/block"))
            .build()
            .register();

    public static final BlockEntry<PantographBlock> PANTOGRAPH = REGISTRATE.block("pantograph", PantographBlock::new)
            .initialProperties(SharedProperties::netheriteMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .transform(pickaxeOnly())
            .onRegister(movementBehaviour(new PantographMovementBehaviour()))
            .item()
            .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
            .model((c, p) -> p.blockItem(c::getEntry, "/item"))
            .build()
            .register();

    public static void register() {

    }
}
