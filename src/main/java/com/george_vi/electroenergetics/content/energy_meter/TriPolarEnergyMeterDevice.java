package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class TriPolarEnergyMeterDevice extends SimulatedDevice<EnergyMeterDevice.DataHolder> {
    public TriPolarEnergyMeterDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, EnergyMeterDevice.DataHolder extraData) {
        if (extraData.isClosed) {
            bridges.builder(pos).resistor(0, 3, 0.001);
            bridges.builder(pos).resistor(2, 5, 0.001);
            bridges.builder(pos).resistor(1, 4, 0.001);
            bridges.builder(pos).resistor(0, 1, 9999);
            bridges.builder(pos).resistor(1, 2, 9999);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, EnergyMeterDevice.DataHolder extraData) {
        double vd1 = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 3);
        double vd2 = results.getVoltageAt(pos, 2) - results.getVoltageAt(pos, 5);
        double v1 = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);
        double v2 = results.getVoltageAt(pos, 1) - results.getVoltageAt(pos, 2);

        double amps1 = vd1 / 0.001;
        double amps2 = vd2 / 0.001;

        if (Math.abs(amps1) > 0.01 && extraData.isClosed)
            extraData.totalEnergy += (amps1 * v1 / 72000) / 1000;

        if (Math.abs(amps2) > 0.01 && extraData.isClosed)
            extraData.totalEnergy -= (amps2 * v2 / 72000) / 1000;

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
                extraData.be.activePower = extraData.isClosed ? amps1 * v1 - amps2 * v2 : 0;
            }
        }
    }

    @Override
    public EnergyMeterDevice.DataHolder read(CompoundTag tag) {
        EnergyMeterDevice.DataHolder dataHolder = new EnergyMeterDevice.DataHolder();
        dataHolder.totalEnergy = tag.getDouble("TotalEnergy");
        dataHolder.isClosed = tag.getBoolean("Closed");
        return dataHolder;
    }

    @Override
    public CompoundTag write(EnergyMeterDevice.DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("TotalEnergy", extraData.totalEnergy);
        tag.putBoolean("Closed", extraData.isClosed);
        return tag;
    }
}
