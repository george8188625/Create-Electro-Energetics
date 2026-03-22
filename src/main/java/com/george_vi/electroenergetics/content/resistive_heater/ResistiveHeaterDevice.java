package com.george_vi.electroenergetics.content.resistive_heater;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ResistiveHeaterDevice extends SimulatedDevice<ResistiveHeaterDevice.DataHolder> {

    public ResistiveHeaterDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos)
                .resistor(0, 1, 45);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double vd = results.getVoltageAt(pos, 0, 1);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ResistiveHeaterBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.setVoltage((float) Math.abs(vd));
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    public static class DataHolder {
        public ResistiveHeaterBlockEntity be;

    }
}
