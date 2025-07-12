package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorDevice;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.connector.ConnectorDevice;
import com.george_vi.electroenergetics.content.converter.ConverterDevice;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryDevice;
import com.george_vi.electroenergetics.content.cut_off_switch.CutOffSwitchDevice;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorDevice;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterDevice;
import com.george_vi.electroenergetics.content.fuse.FuseDevice;
import com.george_vi.electroenergetics.content.gauge.GaugeDevice;
import com.george_vi.electroenergetics.content.ground_rod.GroundRodDevice;
import com.george_vi.electroenergetics.content.pole.ConcretePoleDevice;
import com.george_vi.electroenergetics.content.redstone_relay.RedstoneRelayDevice;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesDevice;
import com.george_vi.electroenergetics.content.transformer.TransformerDevice;
import com.george_vi.electroenergetics.content.voltage_regulator.VoltageRegulatorDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CEESimulatedDevices {
    static Map<ResourceLocation, SimulatedDevice> BY_ID = new HashMap<>();

    public static final SimulatedDevice CONNECTOR = register(new ConnectorDevice(CreateElecrtoEnergetics.rl("connector")));
    public static final SimulatedDevice CREATIVE_BATTERY = register(new CreativeBatteryDevice(CreateElecrtoEnergetics.rl("creative_battery")));
    public static final SimulatedDevice BULB = register(new BulbDevice(CreateElecrtoEnergetics.rl("bulb")));
    public static final SimulatedDevice CUT_OFF_SWITCH = register(new CutOffSwitchDevice(CreateElecrtoEnergetics.rl("cut_off_switch"), 1));
    public static final SimulatedDevice DOUBLE_SWITCH = register(new CutOffSwitchDevice(CreateElecrtoEnergetics.rl("double_switch"), 2));
    public static final SimulatedDevice ENERGY_METER = register(new EnergyMeterDevice(CreateElecrtoEnergetics.rl("energy_meter")));
    public static final SimulatedDevice ELECTRIC_MOTOR = register(new ElectricMotorDevice(CreateElecrtoEnergetics.rl("electric_motor")));
    public static final SimulatedDevice TRANSFORMER = register(new TransformerDevice(CreateElecrtoEnergetics.rl("transformer")));
    public static final SimulatedDevice VOLTAGE_REGULATOR = register(new VoltageRegulatorDevice(CreateElecrtoEnergetics.rl("voltage_regulator")));
    public static final SimulatedDevice GROUND_ROD = register(new GroundRodDevice(CreateElecrtoEnergetics.rl("ground")));
    public static final SimulatedDevice VOLTMETER = register(new GaugeDevice(CreateElecrtoEnergetics.rl("voltmeter"), true));
    public static final SimulatedDevice AMMETER = register(new GaugeDevice(CreateElecrtoEnergetics.rl("ammeter"), false));
    public static final SimulatedDevice ALTERNATOR_BRUSHES = register(new AlternatorBrushesDevice(CreateElecrtoEnergetics.rl("alternator_brushes")));
    public static final SimulatedDevice FUSE = register(new FuseDevice(CreateElecrtoEnergetics.rl("fuse")));
    public static final SimulatedDevice ACCUMULATOR = register(new AccumulatorDevice(CreateElecrtoEnergetics.rl("accumulator")));
    public static final SimulatedDevice CONVERTER = register(new ConverterDevice(CreateElecrtoEnergetics.rl("converter")));
    public static final SimulatedDevice REDSTONE_RELAY = register(new RedstoneRelayDevice(CreateElecrtoEnergetics.rl("redstone_relay")));
    public static final SimulatedDevice CONCRETE_POLE = register(new ConcretePoleDevice(CreateElecrtoEnergetics.rl("concrete_pole")));

    public static SimulatedDevice register(SimulatedDevice device) {
        BY_ID.put(device.getID(), device);
        return device;
    }

    public static SimulatedDevice get(ResourceLocation id) {
        return BY_ID.get(id);
    }
}
