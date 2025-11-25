package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class EnergyMeterDevice extends SimulatedDevice<EnergyMeterDevice.DataHolder> {
    public EnergyMeterDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.isClosed) {
            bridges.builder(pos).resistor(0, 2, 0.001);
            bridges.builder(pos).resistor(1, 3, 0.001);
            bridges.builder(pos).resistor(0, 1, 9999);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double vd = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 2);
        double v = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);
        double amps = vd / 0.001;

        if (Math.abs(amps) > 0.01 && extraData.isClosed)
            extraData.totalEnergy += (amps * v / 72000) / 1000;

        if (!level.isLoaded(pos))
            return;


        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof EnergyMeterBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.setTotalEnergy((float) extraData.totalEnergy);
                extraData.be.activePower = extraData.isClosed ? amps * v : 0;
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.totalEnergy = tag.getDouble("TotalEnergy");
        dataHolder.isClosed = tag.getBoolean("Closed");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("TotalEnergy", extraData.totalEnergy);
        tag.putBoolean("Closed", extraData.isClosed);
        return tag;
    }

    public static class DataHolder {
        public double totalEnergy;
        public boolean isClosed;
        public EnergyMeterBlockEntity be;
    }
}
