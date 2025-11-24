package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class EnergyMeterDevice extends SimulatedDevice {
    public EnergyMeterDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (extraData.getBoolean("Closed")) {
            bridges.builder(pos).resistor(0, 2, 0.001);
            bridges.builder(pos).resistor(1, 3, 0.001);
            bridges.builder(pos).resistor(0, 1, 9999);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double vd = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 2);
        double v = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);
        double amps = vd / 0.001;
        double totalEnergy = extraData.getDouble("TotalEnergy");

        if (Math.abs(amps) > 0.01 && extraData.getBoolean("Closed"))
            totalEnergy += (amps * v / 72000) / 1000;

        extraData.putDouble("TotalEnergy", totalEnergy);

        if (!level.isLoaded(pos))
            return;

        if (level.getBlockEntity(pos) instanceof EnergyMeterBlockEntity be) {
            be.setTotalEnergy((float) totalEnergy);
            be.activePower = extraData.getBoolean("Closed") ? amps * v : 0;
        }
    }
}
