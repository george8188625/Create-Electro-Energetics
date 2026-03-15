package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorDevice;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.buzzer.BuzzerDevice;
import com.george_vi.electroenergetics.content.connector.ConnectorDevice;
import com.george_vi.electroenergetics.content.converter.ConverterDevice;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryDevice;
import com.george_vi.electroenergetics.content.cut_off_switch.CutOffSwitchDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor.HVCapacitorDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_switch.HVSwitchDevice;
import com.george_vi.electroenergetics.content.cut_off_switch.MomentarySwitchDevice;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorDevice;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpDevice;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.CapacitorDevice;
import com.george_vi.electroenergetics.content.electronic_components.diode.DiodeDevice;
import com.george_vi.electroenergetics.content.electronic_components.resistor.CreativeResistorDevice;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorDevice;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterDevice;
import com.george_vi.electroenergetics.content.energy_meter.TriPolarEnergyMeterDevice;
import com.george_vi.electroenergetics.content.fuse.FuseDevice;
import com.george_vi.electroenergetics.content.fuse.FuseHolderDevice;
import com.george_vi.electroenergetics.content.gauge.GaugeDevice;
import com.george_vi.electroenergetics.content.ground_rod.GroundRodDevice;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbDevice;
import com.george_vi.electroenergetics.content.pole.ConcretePoleDevice;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerDevice;
import com.george_vi.electroenergetics.content.redstone_relay.RedstoneRelayDevice;
import com.george_vi.electroenergetics.content.relay.RelayDevice;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerCoreDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerDevice;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.VoltageRegulatorDevice;
import com.george_vi.electroenergetics.foundation.base.TemporaryDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CEESimulatedDevices {
    static Map<ResourceLocation, SimulatedDevice<?>> BY_ID = new HashMap<>();

    public static final SimulatedDevice<?> TEMPORARY = register(new TemporaryDevice(CreateElectroEnergetics.rl("temporary")));
    public static final SimulatedDevice<?> CONNECTOR = register(new ConnectorDevice(CreateElectroEnergetics.rl("connector")));
    public static final SimulatedDevice<?> CREATIVE_BATTERY = register(new CreativeBatteryDevice(CreateElectroEnergetics.rl("creative_battery")));
    public static final SimulatedDevice<?> BULB = register(new BulbDevice(CreateElectroEnergetics.rl("bulb")));
    public static final SimulatedDevice<?> CUT_OFF_SWITCH = register(new CutOffSwitchDevice(CreateElectroEnergetics.rl("cut_off_switch"), 1));
    public static final SimulatedDevice<?> DOUBLE_SWITCH = register(new CutOffSwitchDevice(CreateElectroEnergetics.rl("double_switch"), 2));
    public static final SimulatedDevice<?> ENERGY_METER = register(new EnergyMeterDevice(CreateElectroEnergetics.rl("energy_meter")));
    public static final SimulatedDevice<?> TRI_POLAR_ENERGY_METER = register(new TriPolarEnergyMeterDevice(CreateElectroEnergetics.rl("tri_polar_energy_meter")));
    public static final SimulatedDevice<?> ELECTRIC_MOTOR = register(new ElectricMotorDevice(CreateElectroEnergetics.rl("electric_motor")));
    public static final SimulatedDevice<?> ELECTRIC_PUMP = register(new ElectricPumpDevice(CreateElectroEnergetics.rl("electric_pump")));
    public static final SimulatedDevice<?> TRANSFORMER = register(new TransformerDevice(CreateElectroEnergetics.rl("transformer")));
    public static final SimulatedDevice<?> VOLTAGE_REGULATOR = register(new VoltageRegulatorDevice(CreateElectroEnergetics.rl("voltage_regulator")));
    public static final SimulatedDevice<?> GROUND_ROD = register(new GroundRodDevice(CreateElectroEnergetics.rl("ground")));
    public static final SimulatedDevice<?> VOLTMETER = register(new GaugeDevice(CreateElectroEnergetics.rl("voltmeter"), true));
    public static final SimulatedDevice<?> AMMETER = register(new GaugeDevice(CreateElectroEnergetics.rl("ammeter"), false));
    public static final SimulatedDevice<?> ALTERNATOR_BRUSHES = register(new AlternatorBrushesDevice(CreateElectroEnergetics.rl("alternator_brushes")));
    public static final SimulatedDevice<?> FUSE = register(new FuseDevice(CreateElectroEnergetics.rl("fuse")));
    public static final SimulatedDevice<?> ACCUMULATOR = register(new AccumulatorDevice(CreateElectroEnergetics.rl("accumulator")));
    public static final SimulatedDevice<?> CONVERTER = register(new ConverterDevice(CreateElectroEnergetics.rl("converter")));
    public static final SimulatedDevice<?> REDSTONE_RELAY = register(new RedstoneRelayDevice(CreateElectroEnergetics.rl("redstone_relay")));
    public static final SimulatedDevice<?> CONCRETE_POLE = register(new ConcretePoleDevice(CreateElectroEnergetics.rl("concrete_pole")));
    public static final SimulatedDevice<?> HV_SWITCH = register(new HVSwitchDevice(CreateElectroEnergetics.rl("high_voltage_switch")));
    public static final SimulatedDevice<?> DIODE = register(new DiodeDevice(CreateElectroEnergetics.rl("diode")));
    public static final SimulatedDevice<?> RESISTOR = register(new ResistorDevice(CreateElectroEnergetics.rl("resistor")));
    public static final SimulatedDevice<?> CREATIVE_RESISTOR = register(new CreativeResistorDevice(CreateElectroEnergetics.rl("creative_resistor")));
    public static final SimulatedDevice<?> FUSE_HOLDER = register(new FuseHolderDevice(CreateElectroEnergetics.rl("fuse_holder")));
    public static final SimulatedDevice<?> BUZZER = register(new BuzzerDevice(CreateElectroEnergetics.rl("buzzer")));
    public static final SimulatedDevice<?> RELAY = register(new RelayDevice(CreateElectroEnergetics.rl("relay")));
    public static final SimulatedDevice<?> CAPACITOR = register(new CapacitorDevice(CreateElectroEnergetics.rl("capacitor"), CEEConfigs.server().voltageValues.capacitorVoltage::get));
    public static final SimulatedDevice<?> HV_CAPACITOR = register(new HVCapacitorDevice(CreateElectroEnergetics.rl("high_voltage_capacitor"), CEEConfigs.server().voltageValues.highVoltageCapacitorVoltage::get));
    public static final SimulatedDevice<?> MOMENTARY_SWITCH = register(new MomentarySwitchDevice(CreateElectroEnergetics.rl("momentary_switch")));
    public static final SimulatedDevice<?> TRANSFORMER_CORE = register(new TransformerCoreDevice(CreateElectroEnergetics.rl("transformer_core")));
    public static final SimulatedDevice<?> INDICATOR_BULB = register(new IndicatorBulbDevice(CreateElectroEnergetics.rl("indicator_bulb")));
    public static final SimulatedDevice<?> POTENTIOMETER = register(new PotentiometerDevice(CreateElectroEnergetics.rl("potentiometer")));

    public static SimulatedDevice<?> register(SimulatedDevice<?> device) {
        BY_ID.put(device.getID(), device);
        return device;
    }

    public static SimulatedDevice<?> get(ResourceLocation id) {
        return BY_ID.get(id);
    }
}
