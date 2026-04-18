package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PantographDevice extends SimpleElectricalDevice {
    public PantographBlockEntity be;

    public PantographDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }


    @Override
    public void preTick(BridgeCollector bridges) {
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof PantographBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                if (be.attachedNode != null)
                    bridges.builder(pos)
                            .resistor(new InWorldNode(0, pos), be.attachedNode, 0.01);
            }
        }
    }

}
