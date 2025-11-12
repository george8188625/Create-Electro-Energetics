package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class IndicatorBulbDevice extends SimulatedDevice {
    public IndicatorBulbDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        byte side = extraData.getByte("Side");
        if (side == 0)
            bridges.builder(pos)
                    .resistor(0, 1, 1000);
        if (side == 1)
            bridges.builder(pos)
                    .resistor(2, 3, 1000);
        if (side == 2)
            bridges.builder(pos)
                    .resistor(0, 1, 1000)
                    .resistor(2, 3, 1000);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        byte side = extraData.getByte("Side");

        float firstLight = 0;
        float secondLight = 0;
        if (side == 0 || side == 2)
            firstLight = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, 0, 1) / 70));
        if (side == 1 || side == 2)
            secondLight = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, 2, 3) / 70));

        float oldSecondLight = extraData.getFloat("OldSecondLight");
        float oldFirstLight = extraData.getFloat("OldFirstLight");

        extraData.putDouble("OldSecondLight", secondLight);
        extraData.putDouble("OldFirstLight", firstLight);

        secondLight = Math.min(secondLight, oldSecondLight);
        firstLight = Math.min(firstLight, oldFirstLight);
        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof IndicatorBulbBlockEntity be) {
                if (Math.abs(be.firstLight - firstLight) > 0.02 || Math.abs(be.secondLight - secondLight) > 0.02) {
                    be.firstLight = firstLight;
                    be.secondLight = secondLight;
                    be.sendData();
                }
            }
        }

    }
}
