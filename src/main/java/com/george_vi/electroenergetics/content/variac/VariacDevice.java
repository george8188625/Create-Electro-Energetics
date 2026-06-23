package com.george_vi.electroenergetics.content.variac;

import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerElectricalProperties;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class VariacDevice extends SimpleElectricalDevice {
    public double ratio;
    public float progress;
    public boolean oilLogged;
    public float temp;

    public VariacDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    private static final int PRIMARY_DIV = 10;
    private static final int WIPER_DIV = 11;
    private static final double RESISTANCE = 0.01d;

    /*

        Very fancy drawing of how it all goes together

           PRIMARY [0]                                        WIPER [2]
              │                                                 │
             ┌┴┐                                               ┌┴┐
             │R│                                               │R│
             └┬┘                                               └┬┘
              │                       [1]                       │
         PRIMARY_DIV ──\/\/\/\/\/\── COMMON ──\/\/\/\/\/\── WIPER_DIV
     */

    private boolean wasActive;

    @Override
    public void preTick(BridgeCollector bridges) {

        double windingRatio = ratio * progress;

        if (Math.abs(windingRatio) < 0.01) {
            bridges.builder(pos)
                    .resistor(0, 1, 1_000_000)
                    .resistor(2, 1, RESISTANCE);
            wasActive = false;
            return;
        }

        wasActive = true;

        InWorldNode primaryNode = new InWorldNode(0, pos);
        InWorldNode commonNode = new InWorldNode(1, pos);
        InWorldNode wiperNode = new InWorldNode(2, pos);
        InWorldNode wiperDivNode = new InWorldNode(WIPER_DIV, pos);
        InWorldNode primaryDivNode = new InWorldNode(PRIMARY_DIV, pos);
        TransformerElectricalProperties ep = new TransformerElectricalProperties(1 / windingRatio,
                new DirectionalNodeConnection(commonNode, primaryDivNode),
                new DirectionalNodeConnection(wiperDivNode, wiperNode));

        bridges.bridge(primaryNode, primaryDivNode, ElectricalProperties.resistor(RESISTANCE));
        bridges.bridge(wiperDivNode, commonNode, ElectricalProperties.resistor(RESISTANCE));
        bridges.bridge(primaryDivNode, commonNode, ep);
        bridges.bridge(wiperDivNode, wiperNode, ep.getOtherProperties());
    }

    @Override
    public void postTick(SimulationResults results) {
        float loss = 0;
        if (wasActive) {
            double primaryCurrent = results.getCurrentThrough(pos, 0, PRIMARY_DIV);
            double primaryVoltage = results.getVoltageAt(0, 1);
            loss = (float) Math.abs(primaryCurrent * primaryVoltage);
        }
        float powerMultiplier = (CEEConfigs.server().powerValues.variacMaxPower.getF() / 1300);

        temp = ElectricalDevice.updateTemp(temp, Math.min(loss / powerMultiplier, 10_000));

        if (oilLogged)
            ElectricalDevice.handleTemp(level, pos, deviceSD, temp, 60_000, 80_000);
        else
            ElectricalDevice.handleTemp(level, pos, deviceSD, temp, 30_000, 40_000);
    }

    @Override
    public void update() {
        super.update();

        oilLogged = level.getFluidState(pos).is(CEEFluids.TRANSFORMER_OIL.get().getSource());
    }

    @Override
    public void read(CompoundTag tag) {
        this.ratio = tag.getDouble("Ratio");
        this.temp = tag.getFloat("Temp");
        this.progress = tag.getFloat("Progress");
        this.oilLogged = tag.getBoolean("OilLogged");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("Ratio", ratio);
        tag.putFloat("Temp", temp);
        tag.putFloat("Progress", progress);
        if (oilLogged)
            tag.putBoolean("OilLogged", true);
    }
}
