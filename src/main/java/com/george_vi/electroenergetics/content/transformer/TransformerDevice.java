package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
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

public class TransformerDevice extends SimulatedDevice {
    public TransformerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        boolean backwards = extraData.getBoolean("Backwards");
        double ratio = extraData.getDouble("Ratio");
        if (ratio == 0)
            ratio = 1;

        double rawLoad = extraData.getDouble("Load");
        double rawFeed = -extraData.getDouble("Feed");

        double lastPrimaryVoltage = extraData.getDouble("LastPrimaryVoltage");
        double lastSecondaryVoltage = extraData.getDouble("LastSecondaryVoltage");
        double averagePrimaryVoltage = extraData.getDouble("AveragePrimaryVoltage");
        double averageSecondaryVoltage = extraData.getDouble("AverageSecondaryVoltage");

        if (backwards) {
            double load = Math.max(0, rawFeed);

            bridges.builder(pos)
                    .node(4)
                    .node(5)
                    .resistor(0, 4, 0.001)
                    .resistor(2, 5, Math.abs(averageSecondaryVoltage) < 1e-6d || Math.abs(load) < 1e-6d ? 1e+6d : (averageSecondaryVoltage / (load / averageSecondaryVoltage)))
                    .energyLimitedSource(4, 1, 2000_000, -averageSecondaryVoltage * ratio)
                    .resistor(0, 1, 50000000)
                    .resistor(5, 3, 0.1);

            if (Math.abs(lastPrimaryVoltage / ratio) > Math.abs(lastSecondaryVoltage) && load < 0.0001)
                extraData.remove("Backwards");
        } else {
            double load = Math.max(0, rawLoad);

            bridges.builder(pos)
                    .node(4)
                    .node(5)
                    .resistor(0, 4, 0.1)
                    .resistor(4, 1, Math.abs(averagePrimaryVoltage) < 1e-6d || Math.abs(load) < 1e-6d ? 1e+6d : (averagePrimaryVoltage / (load / averagePrimaryVoltage)))
                    .energyLimitedSource(2, 5, 2000_000, -averagePrimaryVoltage / ratio)
                    .resistor(2, 3, 50000000)
                    .resistor(5, 3, 0.001);
            if (Math.abs(lastPrimaryVoltage / ratio) < Math.abs(lastSecondaryVoltage) && load < 0.0001)
                extraData.putBoolean("Backwards", true);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double primaryVoltage = results.getVoltageAt(pos, 0, 1);
        extraData.putDouble("LastPrimaryVoltage", primaryVoltage);
        double secondaryVoltage = results.getVoltageAt(pos, 2, 3);
        double secondaryCurrent = results.getCurrentThrough(pos, 5, 3);
        extraData.putDouble("LastSecondaryVoltage", secondaryVoltage);
        double load = -secondaryCurrent * secondaryVoltage;
        extraData.putDouble("Load", load);
        double primaryCurrent = results.getCurrentThrough(pos, 0, 4);
        double feed = primaryCurrent * primaryVoltage;
        extraData.putDouble("Feed", feed);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof TransformerBlockEntity be)
                be.power = extraData.getBoolean("Backwards") ? -feed : load;

        // calculate the average voltages

        double[] prevPrimaryVoltages = extraData.getList("LastPrimaryVoltages", Tag.TAG_DOUBLE).stream().mapToDouble(t -> ((DoubleTag)t).getAsDouble()).toArray();
        ListTag pTag = new ListTag();
        pTag.add(DoubleTag.valueOf(primaryVoltage));
        for (int i = 0; i < Math.min(22, prevPrimaryVoltages.length); i++) {
            pTag.add(DoubleTag.valueOf(prevPrimaryVoltages[i]));
        }
        extraData.put("LastPrimaryVoltages", pTag);

        double averagePrimaryVoltage = (Arrays.stream(prevPrimaryVoltages).sum() + primaryVoltage) / (prevPrimaryVoltages.length + 1);


        double[] prevSecondaryVoltages = extraData.getList("LastSecondaryVoltages", Tag.TAG_DOUBLE).stream().mapToDouble(t -> ((DoubleTag)t).getAsDouble()).toArray();
        ListTag sTag = new ListTag();
        sTag.add(DoubleTag.valueOf(secondaryVoltage));
        for (int i = 0; i < Math.min(22, prevSecondaryVoltages.length); i++) {
            sTag.add(DoubleTag.valueOf(prevSecondaryVoltages[i]));
        }
        extraData.put("LastSecondaryVoltages", sTag);

        double averageSecondaryVoltage = (Arrays.stream(prevSecondaryVoltages).sum() + secondaryVoltage) / (prevSecondaryVoltages.length + 1);

        extraData.putDouble("AveragePrimaryVoltage", averagePrimaryVoltage);
        extraData.putDouble("AverageSecondaryVoltage", averageSecondaryVoltage);
    }
}

