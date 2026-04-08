package com.george_vi.electroenergetics.content.pole;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ConcretePoleDevice extends SimpleElectricalDevice {
    public boolean top;
    public boolean bottom;

    public ConcretePoleDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (this.bottom) {
            int length = 0;
            // Incremented by 2
            // The reason this can be done is that there is no possible way to skip an end of a pole if it's in a correct state.
            // If it skips the top-most device of a pole, then it just goes back one.
            // If it lands in the right place, it lands in the right place.
            // A pole always ends with a top-most pole state.
            for (int i = 1; i + pos.getY() < level.getMaxBuildHeight(); i += 2) {
                ConcretePoleDevice device = deviceSD.getDevice(pos.above(i), ConcretePoleDevice.class);
                if (device == null) {
                    length = i - 1;
                    ConcretePoleDevice device1 = deviceSD.getDevice(pos.above(i), ConcretePoleDevice.class);
                    if (device1 == null)
                        return;
                    break;
                }
                else if (device.top) {
                    length = i;
                    break;
                }
            }

            if (length > 0)
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, pos.above(length)), CEEConfigs.server().resistanceValues.wireResistance.get() * length, 0, 0);
        }

        if (top || bottom)
            bridges.builder(pos)
                    .resistor(0, 1, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(1, 2, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(2, 3, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(3, 0, CEEConfigs.server().resistanceValues.wireResistance.get());

    }

    @Override
    public void read(CompoundTag tag) {
        top = tag.getBoolean("Top");
        bottom = tag.getBoolean("Bottom");
    }

    @Override
    public void write(CompoundTag tag) {
        if (bottom)
            tag.putBoolean("Bottom", true);
        if (top)
            tag.putBoolean("Top", true);
    }

}
