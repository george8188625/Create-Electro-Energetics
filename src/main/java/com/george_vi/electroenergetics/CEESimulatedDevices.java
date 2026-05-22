package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorDevice;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.bundled_wire.BundledWireTerminationDevice;
import com.george_vi.electroenergetics.content.buzzer.BuzzerDevice;
import com.george_vi.electroenergetics.content.connector.ConnectorDevice;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorDevice;
import com.george_vi.electroenergetics.content.connector.InsulatorDevice;
import com.george_vi.electroenergetics.content.converter.ConverterDevice;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryDevice;
import com.george_vi.electroenergetics.content.cut_off_switch.CutOffSwitchDevice;
import com.george_vi.electroenergetics.content.cut_off_switch.MomentarySwitchDevice;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorDevice;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpDevice;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.CapacitorDevice;
import com.george_vi.electroenergetics.content.electronic_components.diode.DiodeDevice;
import com.george_vi.electroenergetics.content.electronic_components.inductor.InductorDevice;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorDevice;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterDevice;
import com.george_vi.electroenergetics.content.energy_meter.TriPolarEnergyMeterDevice;
import com.george_vi.electroenergetics.content.frequency_meter.FrequencyMeterDevice;
import com.george_vi.electroenergetics.content.fuse.FuseDevice;
import com.george_vi.electroenergetics.content.fuse.FuseHolderDevice;
import com.george_vi.electroenergetics.content.gauge.GaugeDevice;
import com.george_vi.electroenergetics.content.ground_rod.GroundRodDevice;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbDevice;
import com.george_vi.electroenergetics.content.pole.ConcretePoleDevice;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerDevice;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographDevice;
import com.george_vi.electroenergetics.content.redstone_relay.RedstoneRelayDevice;
import com.george_vi.electroenergetics.content.relay.RelayDevice;
import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterDevice;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesDevice;
import com.george_vi.electroenergetics.content.rotor.ThreePhaseAlternatorBrushesDevice;
import com.george_vi.electroenergetics.content.synchroscope.SynchroscopeDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.current_transformer.CurrentTransformerDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor.HVCapacitorDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_switch.HVSwitchDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.sf6_breaker.SF6BreakerDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerCoreDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.VoltageRegulatorDevice;
import com.george_vi.electroenergetics.foundation.base.TemporaryDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEESimulatedDevices {

    private static final DeferredRegister<SimulatedDeviceType<?>> DEVICES =
            DeferredRegister.create(CEERegistries.SIMULATED_DEVICE_TYPE, CreateElectroEnergetics.ID);


    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ConnectorDevice>> CONNECTOR = DEVICES.register("connector",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("connector"),
                    ((type, level, pos, sd) ->
                            new ConnectorDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<PantographDevice>> PANTOGRAPH = DEVICES.register("pantograph",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("pantograph"),
                    ((type, level, pos, sd) ->
                            new PantographDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<DoubleConnectorDevice>> DOUBLE_CONNECTOR = DEVICES.register("double_connector",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("connector"),
                    ((type, level, pos, sd) ->
                            new DoubleConnectorDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<BulbDevice>> BULB = DEVICES.register("bulb",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("bulb"),
                    ((type, level, pos, sd) ->
                            new BulbDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TemporaryDevice>> TEMPORARY = DEVICES.register("temporary",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("temporary"),
                    ((type, level, pos, sd) -> new TemporaryDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<InsulatorDevice>> INSULATOR = DEVICES.register("insulator",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("insulator"),
                    ((type, level, pos, sd) -> new InsulatorDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CreativeBatteryDevice>> CREATIVE_BATTERY = DEVICES.register("creative_battery",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("creative_battery"),
                    ((type, level, pos, sd) -> new CreativeBatteryDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CutOffSwitchDevice>> CUT_OFF_SWITCH = DEVICES.register("cut_off_switch",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("cut_off_switch"),
                    ((type, level, pos, sd) -> new CutOffSwitchDevice(level, pos, sd, type, 1))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CutOffSwitchDevice>> DOUBLE_SWITCH = DEVICES.register("double_switch",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("double_switch"),
                    ((type, level, pos, sd) -> new CutOffSwitchDevice(level, pos, sd, type, 2))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<EnergyMeterDevice>> ENERGY_METER = DEVICES.register("energy_meter",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("energy_meter"),
                    ((type, level, pos, sd) -> new EnergyMeterDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TriPolarEnergyMeterDevice>> TRI_POLAR_ENERGY_METER = DEVICES.register("tri_polar_energy_meter",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("tri_polar_energy_meter"),
                    ((type, level, pos, sd) -> new TriPolarEnergyMeterDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectricMotorDevice>> ELECTRIC_MOTOR = DEVICES.register("electric_motor",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("electric_motor"),
                    ((type, level, pos, sd) -> new ElectricMotorDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectricPumpDevice>> ELECTRIC_PUMP = DEVICES.register("electric_pump",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("electric_pump"),
                    ((type, level, pos, sd) -> new ElectricPumpDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TransformerDevice>> TRANSFORMER = DEVICES.register("transformer",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("transformer"),
                    ((type, level, pos, sd) -> new TransformerDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<VoltageRegulatorDevice>> VOLTAGE_REGULATOR = DEVICES.register("voltage_regulator",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("voltage_regulator"),
                    ((type, level, pos, sd) -> new VoltageRegulatorDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<GroundRodDevice>> GROUND_ROD = DEVICES.register("ground",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("ground"),
                    ((type, level, pos, sd) -> new GroundRodDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<GaugeDevice>> VOLTMETER = DEVICES.register("voltmeter",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("voltmeter"),
                    ((type, level, pos, sd) -> new GaugeDevice(level, pos, sd, type, true))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<GaugeDevice>> AMMETER = DEVICES.register("ammeter",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("ammeter"),
                    ((type, level, pos, sd) -> new GaugeDevice(level, pos, sd, type, false))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<AlternatorBrushesDevice>> ALTERNATOR_BRUSHES = DEVICES.register("alternator_brushes",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("alternator_brushes"),
                    ((type, level, pos, sd) -> new AlternatorBrushesDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ThreePhaseAlternatorBrushesDevice>> THREE_PHASE_ALTERNATOR_BRUSHES = DEVICES.register("three_phase_alternator_brushes",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("three_phase_alternator_brushes"),
                    ((type, level, pos, sd) -> new ThreePhaseAlternatorBrushesDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<FuseDevice>> FUSE = DEVICES.register("fuse",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("fuse"),
                    ((type, level, pos, sd) -> new FuseDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<AccumulatorDevice>> ACCUMULATOR = DEVICES.register("accumulator",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("accumulator"),
                    ((type, level, pos, sd) -> new AccumulatorDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ConverterDevice>> CONVERTER = DEVICES.register("converter",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("converter"),
                    ((type, level, pos, sd) -> new ConverterDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<RedstoneRelayDevice>> REDSTONE_RELAY = DEVICES.register("redstone_relay",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("redstone_relay"),
                    ((type, level, pos, sd) -> new RedstoneRelayDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ConcretePoleDevice>> CONCRETE_POLE = DEVICES.register("concrete_pole",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("concrete_pole"),
                    ((type, level, pos, sd) -> new ConcretePoleDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<HVSwitchDevice>> HV_SWITCH = DEVICES.register("high_voltage_switch",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("high_voltage_switch"),
                    ((type, level, pos, sd) -> new HVSwitchDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<DiodeDevice>> DIODE = DEVICES.register("diode",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("diode"),
                    ((type, level, pos, sd) -> new DiodeDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ResistorDevice>> RESISTOR = DEVICES.register("resistor",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("resistor"),
                    ((type, level, pos, sd) -> new ResistorDevice(level, pos, sd, type, false))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ResistorDevice>> CREATIVE_RESISTOR = DEVICES.register("creative_resistor",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("creative_resistor"),
                    ((type, level, pos, sd) -> new ResistorDevice(level, pos, sd, type, true))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<FuseHolderDevice>> FUSE_HOLDER = DEVICES.register("fuse_holder",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("fuse_holder"),
                    ((type, level, pos, sd) -> new FuseHolderDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<BuzzerDevice>> BUZZER = DEVICES.register("buzzer",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("buzzer"),
                    ((type, level, pos, sd) -> new BuzzerDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<RelayDevice>> RELAY = DEVICES.register("relay",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("relay"),
                    ((type, level, pos, sd) -> new RelayDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CapacitorDevice>> CAPACITOR = DEVICES.register("capacitor",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("capacitor"),
                    ((type, level, pos, sd) -> new CapacitorDevice(level, pos, sd, type, CEEConfigs.server().voltageValues.capacitorVoltage::get))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<HVCapacitorDevice>> HV_CAPACITOR = DEVICES.register("high_voltage_capacitor",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("high_voltage_capacitor"),
                    ((type, level, pos, sd) -> new HVCapacitorDevice(level, pos, sd, type, CEEConfigs.server().voltageValues.highVoltageCapacitorVoltage::get))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<MomentarySwitchDevice>> MOMENTARY_SWITCH = DEVICES.register("momentary_switch",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("momentary_switch"),
                    ((type, level, pos, sd) -> new MomentarySwitchDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TransformerCoreDevice>> TRANSFORMER_CORE = DEVICES.register("transformer_core",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("transformer_core"),
                    ((type, level, pos, sd) -> new TransformerCoreDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<IndicatorBulbDevice>> INDICATOR_BULB = DEVICES.register("indicator_bulb",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("indicator_bulb"),
                    ((type, level, pos, sd) -> new IndicatorBulbDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<PotentiometerDevice>> POTENTIOMETER = DEVICES.register("potentiometer",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("potentiometer"),
                    ((type, level, pos, sd) -> new PotentiometerDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ResistiveHeaterDevice>> RESISTIVE_HEATER = DEVICES.register("resistive_heater",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("resistive_heater"),
                    ((type, level, pos, sd) -> new ResistiveHeaterDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SynchroscopeDevice>> SYNCHROSCOPE = DEVICES.register("synchroscope",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("synchroscope"),
                    ((type, level, pos, sd) -> new SynchroscopeDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<FrequencyMeterDevice>> FREQUENCY_METER = DEVICES.register("frequency_meter",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("frequency_meter"),
                    ((type, level, pos, sd) -> new FrequencyMeterDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<InductorDevice>> INDUCTOR = DEVICES.register("inductor",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("inductor"),
                    ((type, level, pos, sd) -> new InductorDevice(level, pos, sd, type, CEEConfigs.server().voltageValues.inductorVoltage::get))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CurrentTransformerDevice>> CURRENT_TRANSFORMER = DEVICES.register("current_transformer",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("current_transformer"),
                    ((type, level, pos, sd) -> new CurrentTransformerDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SF6BreakerDevice>> SF6_BREAKER = DEVICES.register("sulfur_hexafluoride_breaker",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("sulfur_hexafluoride_breaker"),
                    ((type, level, pos, sd) -> new SF6BreakerDevice(level, pos, sd, type))));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<BundledWireTerminationDevice>> BUNDLED_WIRE_TERMINATION = DEVICES.register("bundled_wire_termination",
            () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl("bundled_wire_termination"),
                    ((type, level, pos, sd) -> new BundledWireTerminationDevice(level, pos, sd, type))));


    public static void register(IEventBus bus) {
        DEVICES.register(bus);
    }
}
