package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ElectricMotorDevice extends SimulatedDevice {
    public ElectricMotorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                bridges.builder(pos)
                        .resistor(0, 1, 0.8 * Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 3, CEEConfigs.server().resistanceValues.motorResistance.get() / Mth.clamp(be.load, 0.1, 3)));
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double vd = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                be.setVoltage((float) vd);
    }
}
