package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class IndicatorBulbDevice extends SimulatedDevice<IndicatorBulbDevice.DataHolder> {
    public IndicatorBulbDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.side == 0)
            bridges.builder(pos)
                    .resistor(0, 1, 1000);
        if (extraData.side == 1)
            bridges.builder(pos)
                    .resistor(2, 3, 1000);
        if (extraData.side == 2)
            bridges.builder(pos)
                    .resistor(0, 1, 1000)
                    .resistor(2, 3, 1000);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {

        float firstLight = 0;
        float secondLight = 0;
        if (extraData.side == 0 || extraData.side == 2)
            firstLight = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, 0, 1) / 70));
        if (extraData.side == 1 || extraData.side == 2)
            secondLight = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, 2, 3) / 70));

        float oldSecondLight = extraData.oldSecondLight;
        float oldFirstLight = extraData.oldFirstLight;

        extraData.oldSecondLight = secondLight;
        extraData.oldFirstLight = firstLight;

        firstLight = Math.min(firstLight, oldFirstLight);
        secondLight = Math.min(secondLight, oldSecondLight);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof IndicatorBulbBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                if (Math.abs(extraData.be.firstLight - firstLight) > 0.02 || Math.abs(extraData.be.secondLight - secondLight) > 0.02) {
                    extraData.be.firstLight = firstLight;
                    extraData.be.secondLight = secondLight;
                    extraData.be.sendData();
                }
            }
        }

    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.side = tag.getInt("Side");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Side", extraData.side);
        return tag;
    }

    public static class DataHolder {
        public int side;
        public float oldFirstLight;
        public float oldSecondLight;
        public IndicatorBulbBlockEntity be;
    }
}
