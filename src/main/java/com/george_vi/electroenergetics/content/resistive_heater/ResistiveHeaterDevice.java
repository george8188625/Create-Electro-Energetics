package com.george_vi.electroenergetics.content.resistive_heater;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ResistiveHeaterDevice extends SimpleElectricalDevice {
    public ResistiveHeaterBlockEntity be;

    public ResistiveHeaterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .resistor(0, 1, 45);
    }

    @Override
    public void postTick(SimulationResults results) {
        double vd = results.getVoltageAt(pos, 0, 1);

        if (be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ResistiveHeaterBlockEntity be)
                this.be = be;

        if (be != null) {
            if (be.isRemoved())
                be = null;
            else {
                be.setVoltage((float) Math.abs(vd));
            }
        }
    }
}
