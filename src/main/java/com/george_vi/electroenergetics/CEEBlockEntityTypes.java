package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlockEntity;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeRenderer;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlockEntity;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlockEntity;
import com.george_vi.electroenergetics.content.transformer.TransformerBlockEntity;
import com.george_vi.electroenergetics.content.voltage_regulator.VoltageRegulatorBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.george_vi.electroenergetics.CreateElecrtoEnergetics.REGISTRATE;

public class CEEBlockEntityTypes {
    public static final BlockEntityEntry<EnergyMeterBlockEntity> ENERGY_METER = REGISTRATE.blockEntity("energy_meter", EnergyMeterBlockEntity::new)
            .validBlock(CEEBlocks.ENERGY_METER::get)
            .register();

    public static final BlockEntityEntry<ElectricGaugeBlockEntity> VOLTMETER = REGISTRATE.blockEntity("voltmeter", ElectricGaugeBlockEntity::voltmeter)
            .validBlock(CEEBlocks.VOLTMETER::get)
            .renderer(() -> ElectricGaugeRenderer::voltmeter)
            .register();

    public static final BlockEntityEntry<ElectricGaugeBlockEntity> AMMETER = REGISTRATE.blockEntity("ammeter", ElectricGaugeBlockEntity::ammeter)
            .validBlock(CEEBlocks.AMMETER::get)
            .renderer(() -> ElectricGaugeRenderer::ammeter)
            .register();

    public static final BlockEntityEntry<CreativeBatteryBlockEntity> CREATIVE_BATTERY = REGISTRATE.blockEntity("creative_battery", CreativeBatteryBlockEntity::new)
            .validBlock(CEEBlocks.CREATIVE_BATTERY::get)
            .register();

    public static final BlockEntityEntry<TransformerBlockEntity> TRANSFORMER = REGISTRATE.blockEntity("transformer", TransformerBlockEntity::new)
            .validBlocks(CEEBlocks.TRANSFORMER::get)
            .register();

//    public static final BlockEntityEntry<VoltageRegulatorBlockEntity> VOLTAGE_REGULATOR = REGISTRATE.blockEntity("voltage_regulator", VoltageRegulatorBlockEntity::new)
//            .validBlock(CEEBlocks.VOLTAGE_REGULATOR::get)
//            .register();

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


    public static void register() {

    }
}

