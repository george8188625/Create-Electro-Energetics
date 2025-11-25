package com.george_vi.electroenergetics.content.electric_pump;

import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ElectricPumpDevice extends SimulatedDevice<ElectricPumpDevice.DataHolder> {
    public ElectricPumpDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                bridges.builder(pos).resistor(0, 1, extraData.be.getResistance());
            }
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double vd = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricPumpBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.setVoltage((float) vd);
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
        public ElectricPumpBlockEntity be;
    }
}
