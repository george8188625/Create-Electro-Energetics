package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ConverterDevice extends SimulatedDevice<ConverterDevice.DataHolder> {
    public ConverterDevice(ResourceLocation id) {
        super(id);
    }

    static final int MAX_ENERGY = 100_000;

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.isSource)
            bridges.builder(pos)
                    .energyLimitedSource(0, 1, extraData.storedEnergy, extraData.voltage);
        else
            bridges.builder(pos)
                    .resistor(0, 1, extraData.resistance == 0 ? 999999 : extraData.resistance);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double conversionRate = 34;

        double vd = Math.abs(results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1));
        if (extraData.isSource) {

            double current = results.getCurrentThrough(pos, 0, 1);
            double power = Math.abs(current) * vd;

            extraData.storedEnergy -= power;

            if (extraData.storedEnergy < MAX_ENERGY && level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()), state.getValue(ConverterBlock.FACING));
                if (energyStorage != null)
                    extraData.storedEnergy += energyStorage.extractEnergy((int) ((MAX_ENERGY - extraData.storedEnergy) / conversionRate), false) * conversionRate;
            }

            return;
        }

        double power = Math.abs(vd * results.getCurrentThrough(pos, 0, 1));

        extraData.storedEnergy += power;

        if (extraData.storedEnergy > 0 && level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()), state.getValue(ConverterBlock.FACING));
            if (energyStorage != null)
                extraData.storedEnergy -= energyStorage.receiveEnergy((int) (extraData.storedEnergy / conversionRate), false) * conversionRate;
        }


        if (vd > 1)
            extraData.resistance = Math.max(20, vd / (Math.max((MAX_ENERGY - extraData.storedEnergy), 0.01) / vd));
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.voltage = tag.getDouble("Voltage");
        dataHolder.resistance = tag.getDouble("Resistance");
        dataHolder.storedEnergy = tag.getDouble("StoredEnergy");
        dataHolder.isSource = tag.getBoolean("Source");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Source", extraData.isSource);
        tag.putDouble("Voltage", extraData.voltage);
        tag.putDouble("Resistance", extraData.resistance);
        tag.putDouble("StoredEnergy", extraData.storedEnergy);
        return tag;
    }

    public static class DataHolder {
        public boolean isSource;
        public double voltage;
        public double resistance;
        public double storedEnergy;
        public ConverterBlockEntity be;
    }
}
