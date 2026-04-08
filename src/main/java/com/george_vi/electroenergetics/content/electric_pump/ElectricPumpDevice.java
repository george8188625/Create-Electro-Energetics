package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ElectricPumpDevice extends SimpleElectricalDevice {
    public ElectricPumpBlockEntity be;


    public ElectricPumpDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                bridges.builder(pos).resistor(0, 1, this.be.getResistance());
            }
        }
    }


    @Override
    public void postTick(SimulationResults results) {
        double vd = results.getVoltageAt(pos, 0, 1);

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.setVoltage((float) vd);
            }
        }
    }


}
