package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ElectricPumpDevice extends SimulatedDevice {
    public ElectricPumpDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                bridges.builder(pos).resistor(0, 1, be.getResistance());
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double vd = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                be.setVoltage((float) vd);
    }
}
