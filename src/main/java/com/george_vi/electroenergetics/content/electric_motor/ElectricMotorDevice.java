package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricMotorDevice extends SimpleElectricalDevice {
    public ElectricMotorBlockEntity be;

    public ElectricMotorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }


    @Override
    public void preTick(BridgeCollector bridges) {
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                double load = Mth.clamp(this.be.load, 0.05, 1);
                if (Double.isNaN(load))
                    load = 0;
                bridges.builder(pos)
                        .resistor(0, 1, 0.8 * Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 6,
                                CEEConfigs.server().resistanceValues.motorResistance.get() / load));
            }
        }
    }

    @Override
    public void postTick(SimulationResults results) {
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                double[] v0 = results.getVoltages(new InWorldNode(0, pos));
                double[] v1 = results.getVoltages(new InWorldNode(1, pos));
                int n = Math.min(v0.length, v1.length);
                for (int i = 0; i < n; i++) {
                    this.be.averageVoltage.add(v0[i] - v1[i]);
                }
            }
        }
    }

    @Override
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
