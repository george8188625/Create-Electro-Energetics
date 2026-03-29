package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class GaugeDevice extends SimulatedDevice<GaugeDevice.DataHolder> {
    public final boolean voltmeter;
    public GaugeDevice(ResourceLocation id, boolean voltmeter) {
        super(id);
        this.voltmeter = voltmeter;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos).resistor(0, 1, voltmeter ? 1_000_000 : 0.01);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        if (!level.isLoaded(pos))
            return;

        double vd = results.getVoltageAt(pos, 0, 1);
        extraData.rmsVoltages.add(vd);
        vd = extraData.rmsVoltages.get();
        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricGaugeBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.voltage = vd;
                extraData.be.setValue(voltmeter ? Math.abs(vd) : Math.abs(vd) / 0.01);
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.rmsVoltages = new RMSHolder(2);
        dataHolder.rmsVoltages.read(tag, "Voltages");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        extraData.rmsVoltages.write(tag, "Voltages");
        return tag;
    }

    public static class DataHolder {
        public ElectricGaugeBlockEntity be;
        public RMSHolder rmsVoltages;
    }
}
