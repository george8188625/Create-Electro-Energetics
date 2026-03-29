package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class ElectricMotorDevice extends SimulatedDevice<ElectricMotorDevice.DataHolder> {
    public ElectricMotorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                double load = Mth.clamp(extraData.be.load, 0.1, 3);
                if (Double.isNaN(load))
                    load = 0;
                bridges.builder(pos)
                        .resistor(0, 1, 0.8 * Math.min(CEEConfigs.server().resistanceValues.motorResistance.get() * 3, CEEConfigs.server().resistanceValues.motorResistance.get() / load));
            }
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double vd = results.getVoltageAt(pos, 0, 1);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.averageVoltage.add(vd);
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        return new DataHolder();
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        return new CompoundTag();
    }

    public static class DataHolder {
        public ElectricMotorBlockEntity be;
    }
}
