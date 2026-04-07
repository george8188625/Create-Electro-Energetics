package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

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
                double load = Mth.clamp(this.be.load, 0.1, 3);
                if (Double.isNaN(load))
                    load = 0;
                bridges.builder(pos)
                        .resistor(0, 1, 0.8 * Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 3, CEEConfigs.server().resistanceValues.motorResistance.get() / load));
            }
        }
    }

    @Override
    public void postTick(SimulationResults results) {
        double vd = results.getVoltageAt(pos, 0, 1);

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.averageVoltage.add(vd);
            }
        }
    }
    
}
