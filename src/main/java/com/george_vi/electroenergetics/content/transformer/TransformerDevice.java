package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Map;

public class TransformerDevice extends SimulatedDevice {
    public TransformerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {

        bridges.builder(pos)
                .resistor(0, 1, extraData.getDouble("PrimaryResistance") == 0 ? 9999999 : extraData.getDouble("PrimaryResistance"))
                .energyLimitedSource(3, 2, extraData.getDouble("StoredEnergy"), extraData.getDouble("SecondaryVoltage"));
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double storedEnergy = extraData.getDouble("StoredEnergy");
        float ratio = extraData.getFloat("Ratio");
        if (ratio == 0)
            ratio = 1;

        double primaryVoltage = results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1);
        double secondaryVoltage = results.getVoltageAt(pos, 3) - results.getVoltageAt(pos, 2);
        if (Math.abs(primaryVoltage) < 0.1)
            primaryVoltage = 0;

        // incoming energy and load

        double secondaryCurrent = results.getCurrentThrough(pos, 2, 3);
        double load = secondaryCurrent * secondaryVoltage;
        if (load < -0.1)
            load = 0;
        else if (load < 100)
            load = 100;

        double primaryCurrent = results.getCurrentThrough(pos, 0, 1);
        double incomingEnergy = primaryCurrent * primaryVoltage;

        storedEnergy -= load;
        storedEnergy += incomingEnergy;
        if (storedEnergy < 0)
            storedEnergy = 0;

        // calculate the average primary voltage

        double[] prevVoltages = extraData.getList("PrevVoltages", Tag.TAG_DOUBLE).stream().mapToDouble(t -> ((DoubleTag)t).getAsDouble()).toArray();
        ListTag tag = new ListTag();
        tag.add(DoubleTag.valueOf(primaryVoltage));
        for (int i = 0; i < Math.min(9, prevVoltages.length); i++) {
            tag.add(DoubleTag.valueOf(prevVoltages[i]));
        }
        extraData.put("PrevVoltages", tag);

        double averageVoltage = Arrays.stream(prevVoltages).average().orElse(0);
//        double maxEnergy = Math.min(8000000 * Math.log10(averageVoltage + 40000) - 36_800_000, 2_000_000);
        double maxEnergy = 2_000_000;
        // calculate primary resistance

        double resistance;
        if ((storedEnergy - load) < maxEnergy) {
            double availableEnergy = maxEnergy - storedEnergy;
            double divisor = (availableEnergy > 1000 ? availableEnergy / 100 : availableEnergy) + load;
            resistance = Math.abs(averageVoltage / (divisor / averageVoltage));
        } else if (load > 10) {
            double divisor = (storedEnergy > maxEnergy ? load : load * 2);
            resistance = Math.abs(averageVoltage / (divisor / averageVoltage));
        } else {
            resistance = 999999;
        }

        double primaryResistance = storedEnergy / maxEnergy < 0.75 ? resistance / 10 : resistance;
        if (primaryResistance < 0.1)
            primaryResistance = 0.1;

        extraData.putDouble("PrimaryResistance", primaryResistance);
        extraData.putDouble("StoredEnergy", storedEnergy);
        extraData.putDouble("SecondaryVoltage", averageVoltage / ratio);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof TransformerBlockEntity be)
                be.power = load;
    }
}
