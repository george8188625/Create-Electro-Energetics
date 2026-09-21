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
import com.george_vi.electroenergetics.content.electric_fan.ElectricFanDevice;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorDevice;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpDevice;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelDevice;
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
import com.george_vi.electroenergetics.content.railway_electrification.third_rail.RailContactShoeDevice;
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
import com.george_vi.electroenergetics.content.variac.VariacDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceFactory;
import com.george_vi.electroenergetics.foundation.base.TemporaryDevice;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEESimulatedDevices {

    private static final DeferredRegister<SimulatedDeviceType<?>> DEVICES =
            DeferredRegister.create(CEERegistries.SIMULATED_DEVICE_TYPE, CreateElectroEnergetics.ID);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ConnectorDevice>>
		CONNECTOR = register("connector", ConnectorDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<PantographDevice>>
		PANTOGRAPH = register("pantograph", PantographDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<DoubleConnectorDevice>>
		DOUBLE_CONNECTOR = register("double_connector", DoubleConnectorDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<BulbDevice>>
		BULB = register("bulb", BulbDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TemporaryDevice>>
		TEMPORARY = register("temporary", TemporaryDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<InsulatorDevice>>
		INSULATOR = register("insulator", InsulatorDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CreativeBatteryDevice>>
		CREATIVE_BATTERY = register("creative_battery", CreativeBatteryDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CutOffSwitchDevice>>
		CUT_OFF_SWITCH = register("cut_off_switch",
			(level, pos, sd, type) -> new CutOffSwitchDevice(level, pos, sd, type, 1));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CutOffSwitchDevice>>
		DOUBLE_SWITCH = register("double_switch",
			(level, pos, sd, type) -> new CutOffSwitchDevice(level, pos, sd, type, 2));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<EnergyMeterDevice>>
		ENERGY_METER = register("energy_meter", EnergyMeterDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TriPolarEnergyMeterDevice>>
		TRI_POLAR_ENERGY_METER = register("tri_polar_energy_meter", TriPolarEnergyMeterDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectricMotorDevice>>
		ELECTRIC_MOTOR = register("electric_motor", ElectricMotorDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectricPumpDevice>>
		ELECTRIC_PUMP = register("electric_pump", ElectricPumpDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TransformerDevice>>
		TRANSFORMER = register("transformer", TransformerDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<VoltageRegulatorDevice>>
		VOLTAGE_REGULATOR = register("voltage_regulator", VoltageRegulatorDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<GroundRodDevice>>
		GROUND_ROD = register("ground", GroundRodDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<GaugeDevice>>
		VOLTMETER = register("voltmeter",
			(level, pos, sd, type) -> new GaugeDevice(level, pos, sd, type, true));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<GaugeDevice>>
		AMMETER = register("ammeter",
			(level, pos, sd, type) -> new GaugeDevice(level, pos, sd, type, false));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<AlternatorBrushesDevice>>
		ALTERNATOR_BRUSHES = register("alternator_brushes", AlternatorBrushesDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ThreePhaseAlternatorBrushesDevice>>
		THREE_PHASE_ALTERNATOR_BRUSHES = register("three_phase_alternator_brushes", ThreePhaseAlternatorBrushesDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<FuseDevice>>
		FUSE = register("fuse", FuseDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<AccumulatorDevice>>
		ACCUMULATOR = register("accumulator", AccumulatorDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ConverterDevice>>
		CONVERTER = register("converter", ConverterDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<RedstoneRelayDevice>>
		REDSTONE_RELAY = register("redstone_relay", RedstoneRelayDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ConcretePoleDevice>>
		CONCRETE_POLE = register("concrete_pole", ConcretePoleDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<HVSwitchDevice>>
		HV_SWITCH = register("high_voltage_switch", HVSwitchDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<DiodeDevice>>
		DIODE = register("diode", DiodeDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ResistorDevice>>
		RESISTOR = register("resistor",
			(level, pos, sd, type) -> new ResistorDevice(level, pos, sd, type, false));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ResistorDevice>>
		CREATIVE_RESISTOR = register("creative_resistor",
			(level, pos, sd, type) -> new ResistorDevice(level, pos, sd, type, true));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<FuseHolderDevice>>
		FUSE_HOLDER = register("fuse_holder", FuseHolderDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<BuzzerDevice>>
		BUZZER = register("buzzer", BuzzerDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<RelayDevice>>
		RELAY = register("relay", RelayDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CapacitorDevice>>
		CAPACITOR = register("capacitor",
			(level, pos, sd, type) -> new CapacitorDevice(level, pos, sd, type, CEEConfigs.server().voltageValues.capacitorVoltage::get));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<HVCapacitorDevice>>
		HV_CAPACITOR = register("high_voltage_capacitor",
			(level, pos, sd, type) -> new HVCapacitorDevice(level, pos, sd, type, CEEConfigs.server().voltageValues.highVoltageCapacitorVoltage::get));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<MomentarySwitchDevice>>
		MOMENTARY_SWITCH = register("momentary_switch", MomentarySwitchDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<TransformerCoreDevice>>
		TRANSFORMER_CORE = register("transformer_core", TransformerCoreDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<IndicatorBulbDevice>>
		INDICATOR_BULB = register("indicator_bulb", IndicatorBulbDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<PotentiometerDevice>>
		POTENTIOMETER = register("potentiometer", PotentiometerDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ResistiveHeaterDevice>>
		RESISTIVE_HEATER = register("resistive_heater", ResistiveHeaterDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SynchroscopeDevice>>
		SYNCHROSCOPE = register("synchroscope", SynchroscopeDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<FrequencyMeterDevice>>
		FREQUENCY_METER = register("frequency_meter", FrequencyMeterDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<InductorDevice>>
		INDUCTOR = register("inductor",
			(level, pos, sd, type) -> new InductorDevice(level, pos, sd, type, CEEConfigs.server().voltageValues.inductorVoltage::get));

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<CurrentTransformerDevice>>
		CURRENT_TRANSFORMER = register("current_transformer", CurrentTransformerDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<SF6BreakerDevice>>
		SF6_BREAKER = register("sulfur_hexafluoride_breaker", SF6BreakerDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<BundledWireTerminationDevice>>
		BUNDLED_WIRE_TERMINATION = register("bundled_wire_termination", BundledWireTerminationDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectricalPanelDevice>>
		ELECTRICAL_PANEL = register("electrical_panel", ElectricalPanelDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<RailContactShoeDevice>>
		RAIL_CONTACT_SHOE = register("rail_contact_shoe", RailContactShoeDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<VariacDevice>>
		VARIAC = register("variac", VariacDevice::new);

    public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<ElectricFanDevice>>
		ELECTRIC_FAN = register("electric_fan", ElectricFanDevice::new);

    private static <T extends SimulatedDevice> DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<T>> register(String name, SimulatedDeviceFactory<T> factory) {
        return DEVICES.register(name, () -> new SimulatedDeviceType<>(CreateElectroEnergetics.rl(name), factory));
    }

    public static void register(IEventBus bus) { DEVICES.register(bus); }
}