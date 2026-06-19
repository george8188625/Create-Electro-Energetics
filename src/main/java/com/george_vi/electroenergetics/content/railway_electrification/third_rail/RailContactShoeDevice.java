package com.george_vi.electroenergetics.content.railway_electrification.third_rail;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.infrastructure.WireSimulationState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RailContactShoeDevice extends SimpleElectricalDevice {
    public RailContactShoeBlockEntity be;

    public RailContactShoeDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }


    @Override
    public void preTick(BridgeCollector bridges) {
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof RailContactShoeBlockEntity newBE)
                this.be = newBE;

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
