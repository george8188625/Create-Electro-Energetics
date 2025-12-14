package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntityRenderer;
import com.george_vi.electroenergetics.content.buzzer.BuzzerBlockEntity;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.CapacitorBlockEntity;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorBlockEntity;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorRenderer;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlockEntity;
import com.george_vi.electroenergetics.content.fuse.FuseHolderRenderer;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlockEntity;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbRenderer;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerBlockEntity;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographRenderer;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlockEntity;
import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchBlockEntity;
import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchRenderer;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeRenderer;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlockEntity;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlockEntity;
import com.george_vi.electroenergetics.content.transformer.TransformerBlockEntity;
import com.george_vi.electroenergetics.content.transformer.TransformerCoreBlockEntity;
import com.george_vi.electroenergetics.content.voltage_regulator.VoltageRegulatorBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.george_vi.electroenergetics.CreateElecrtoEnergetics.REGISTRATE;

public class CEEBlockEntityTypes {
    public static final BlockEntityEntry<EnergyMeterBlockEntity> ENERGY_METER = REGISTRATE.blockEntity("energy_meter", EnergyMeterBlockEntity::new)
            .displaySource(CEEDisplaySources.ENERGY_METER)
            .displaySource(CEEDisplaySources.WATTMETER)
            .validBlocks(CEEBlocks.ENERGY_METER::get, CEEBlocks.TRI_POLAR_ENERGY_METER::get)
            .register();

    public static final BlockEntityEntry<AccumulatorBlockEntity> ACCUMULATOR = REGISTRATE.blockEntity("accumulator", AccumulatorBlockEntity::new)
            .validBlock(CEEBlocks.ACCUMULATOR)
            .register();

    public static final BlockEntityEntry<ElectricGaugeBlockEntity> VOLTMETER = REGISTRATE.blockEntity("voltmeter", ElectricGaugeBlockEntity::voltmeter)
            .displaySource(CEEDisplaySources.VOLTAGE)
            .validBlock(CEEBlocks.VOLTMETER::get)
            .renderer(() -> ElectricGaugeRenderer::voltmeter)
            .register();

    public static final BlockEntityEntry<ElectricGaugeBlockEntity> AMMETER = REGISTRATE.blockEntity("ammeter", ElectricGaugeBlockEntity::ammeter)
            .displaySource(CEEDisplaySources.AMPERAGE)
            .validBlock(CEEBlocks.AMMETER::get)
            .renderer(() -> ElectricGaugeRenderer::ammeter)
            .register();

    public static final BlockEntityEntry<BulbBlockEntity> BULB = REGISTRATE.blockEntity("bulb", BulbBlockEntity::new)
            .validBlocks(CEEBlocks.BULB::get, CEEBlocks.BROKEN_BULB::get)
            .renderer(() -> BulbBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<CreativeBatteryBlockEntity> CREATIVE_BATTERY = REGISTRATE.blockEntity("creative_battery", CreativeBatteryBlockEntity::new)
            .validBlock(CEEBlocks.CREATIVE_BATTERY::get)
            .register();

    public static final BlockEntityEntry<TransformerBlockEntity> TRANSFORMER = REGISTRATE.blockEntity("transformer", TransformerBlockEntity::new)
            .validBlocks(CEEBlocks.TRANSFORMER::get)
            .register();

    public static final BlockEntityEntry<TransformerCoreBlockEntity> TRANSFORMER_CORE = REGISTRATE.blockEntity("transformer_core", TransformerCoreBlockEntity::new)
            .validBlocks(CEEBlocks.TRANSFORMER_CORE::get)
            .register();

    public static final BlockEntityEntry<ElectricPumpBlockEntity> ELECTRIC_PUMP = REGISTRATE.blockEntity("electric_pump", ElectricPumpBlockEntity::new)
            .validBlocks(CEEBlocks.ELECTRIC_PUMP::get)
            .register();

    public static final BlockEntityEntry<VoltageRegulatorBlockEntity> VOLTAGE_REGULATOR = REGISTRATE.blockEntity("voltage_regulator", VoltageRegulatorBlockEntity::new)
            .validBlock(CEEBlocks.VOLTAGE_REGULATOR::get)
            .register();

    public static final BlockEntityEntry<ElectricMotorBlockEntity> ELECTRIC_MOTOR = REGISTRATE.blockEntity("electric_motor", ElectricMotorBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, false)
            .validBlock(CEEBlocks.ELECTRIC_MOTOR::get)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<AlternatorRotorBlockEntity> ALTERNATOR_ROTOR = REGISTRATE.blockEntity("alternator_rotor", AlternatorRotorBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(CEEPartialModels.ROTOR), false)
            .validBlock(CEEBlocks.ALTERNATOR_ROTOR::get)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();
    public static final BlockEntityEntry<AlternatorBrushesBlockEntity> ALTERNATOR_BRUSHES = REGISTRATE.blockEntity("alternator_brushes", AlternatorBrushesBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, false)
            .validBlock(CEEBlocks.ALTERNATOR_BRUSHES)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<ConverterBlockEntity> CONVERTER = REGISTRATE.blockEntity("converter", ConverterBlockEntity::new)
            .validBlock(CEEBlocks.CONVERTER::get)
            .register();

    public static final BlockEntityEntry<HVSwitchBlockEntity> HV_SWITCH = REGISTRATE.blockEntity("high_voltage_switch", HVSwitchBlockEntity::new)
            .validBlock(CEEBlocks.HV_SWITCH::get)
            .renderer(()  -> HVSwitchRenderer::new)
            .register();

    public static final BlockEntityEntry<PantographBlockEntity> PANTOGRAPH = REGISTRATE.blockEntity("pantograph", PantographBlockEntity::new)
            .validBlock(CEEBlocks.PANTOGRAPH::get)
            .renderer(() -> PantographRenderer::new)
            .register();

    public static final BlockEntityEntry<CatenaryHolderBlockEntity> CATENARY_HOLDER = REGISTRATE.blockEntity("catenary_holder", CatenaryHolderBlockEntity::new)
            .validBlock(CEEBlocks.CATENARY_HOLDER::get)
            .renderer(() -> CatenaryHolderRenderer::new)
            .register();

    public static final BlockEntityEntry<ResistorBlockEntity> RESISTOR = REGISTRATE.blockEntity("resistor", ResistorBlockEntity::new)
            .validBlock(CEEBlocks.RESISTOR::get)
            .renderer(() -> ResistorRenderer::new)
            .register();

    public static final BlockEntityEntry<FuseHolderBlockEntity> FUSE_HOLDER = REGISTRATE.blockEntity("fuse_holder", FuseHolderBlockEntity::new)
            .validBlock(CEEBlocks.FUSE_HOLDER::get)
            .renderer(() -> FuseHolderRenderer::new)
            .register();

    public static final BlockEntityEntry<BuzzerBlockEntity> BUZZER = REGISTRATE.blockEntity("buzzer", BuzzerBlockEntity::new)
            .validBlock(CEEBlocks.BUZZER::get)
            .register();

    public static final BlockEntityEntry<CapacitorBlockEntity> CAPACITOR = REGISTRATE.blockEntity("capacitor", CapacitorBlockEntity::new)
            .validBlock(CEEBlocks.CAPACITOR::get)
            .register();

    public static final BlockEntityEntry<IndicatorBulbBlockEntity> INDICATOR_BULB = REGISTRATE.blockEntity("indicator_bulb", IndicatorBulbBlockEntity::new)
            .validBlock(CEEBlocks.INDICATOR_BULB::get)
            .renderer(() -> IndicatorBulbRenderer::new)
            .register();

    public static final BlockEntityEntry<PotentiometerBlockEntity> POTENTIOMETER = REGISTRATE.blockEntity("potentiometer", PotentiometerBlockEntity::new)
            .validBlock(CEEBlocks.POTENTIOMETER::get)
            .renderer(() -> PotentiometerRenderer::new)
            .register();


    public static void register() {

    }
}

