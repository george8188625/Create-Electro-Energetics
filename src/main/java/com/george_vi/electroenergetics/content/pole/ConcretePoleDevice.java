package com.george_vi.electroenergetics.content.pole;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ConcretePoleDevice extends SimulatedDevice<ConcretePoleDevice.DataHolder> {
    public ConcretePoleDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.bottom) {
            int length = 0;
            // Incremented by 2
            // The reason this can be done is that there is no possible way to skip an end of a pole if it's in a correct state.
            // If it skips the top-most device of a pole, then it just goes back one.
            // If it lands in the right place, it lands in the right place.
            // A pole always ends with a top-most pole state.
            for (int i = 1; i + pos.getY() < level.getMaxBuildHeight(); i += 2) {
                SimulatedDeviceInstance<?> di = bridges.getSD().getDevice(pos.above(i));
                if (di == null || di.simulatedDevice() != CEESimulatedDevices.CONCRETE_POLE || !(di.extraData() instanceof DataHolder dataHolder)) {
                    length = i - 1;
                    SimulatedDeviceInstance<?> ndi = bridges.getSD().getDevice(pos.above(length));
                    if (ndi == null || ndi.simulatedDevice() != CEESimulatedDevices.CONCRETE_POLE || !(ndi.extraData() instanceof DataHolder dataHolder && dataHolder.top))
                        return;
                    break;
                }
                else if (dataHolder.top) {
                    length = i;
                    break;
                }
            }

            if (length > 0)
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, pos.above(length)), CEEConfigs.server().resistanceValues.wireResistance.get() * length, 0, 0);
        }

        if (extraData.top || extraData.bottom)
            bridges.builder(pos)
                    .resistor(0, 1, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(1, 2, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(2, 3, CEEConfigs.server().resistanceValues.wireResistance.get())
                    .resistor(3, 0, CEEConfigs.server().resistanceValues.wireResistance.get());

    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.top = tag.getBoolean("Top");
        dataHolder.bottom = tag.getBoolean("Bottom");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        if (extraData.bottom)
            tag.putBoolean("Bottom", true);
        if (extraData.top)
            tag.putBoolean("Top", true);
        return tag;
    }

    public static class DataHolder {
        public boolean top;
        public boolean bottom;
    }
}
