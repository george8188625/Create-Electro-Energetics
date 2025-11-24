package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;

public class TransformerBehaviour {

    public static void preTick(InWorldNode[] nodes, double ratio, BlockPos pos, BridgeCollector bridges, CompoundTag extraData) {

        boolean backwards = extraData.getBoolean("Backwards");

        double rawLoad = extraData.getDouble("Load");
        double rawFeed = -extraData.getDouble("Feed");

        double lastPrimaryVoltage = extraData.getDouble("LastPrimaryVoltage");
        double lastSecondaryVoltage = extraData.getDouble("LastSecondaryVoltage");
        double averagePrimaryVoltage = extraData.getDouble("AveragePrimaryVoltage");
        double averageSecondaryVoltage = extraData.getDouble("AverageSecondaryVoltage");

        if (Math.abs(lastPrimaryVoltage) < 0.1)
            lastPrimaryVoltage = 0;
        if (Math.abs(lastSecondaryVoltage) < 0.1)
            lastSecondaryVoltage = 0;

        if (Math.abs(averagePrimaryVoltage) < 0.1)
            averagePrimaryVoltage = 0;
        if (Math.abs(averageSecondaryVoltage) < 0.1)
            averageSecondaryVoltage = 0;

        if (backwards) {
            double load = Math.max(0, rawFeed);

            bridges.builder(pos)
                    .node(nodes[4])
                    .node(nodes[5])
                    .resistor(nodes[0], nodes[4], 0.001)
                    .resistor(nodes[2], nodes[5], Math.abs(averageSecondaryVoltage) < 1e-6d || Math.abs(load) < 1e-6d ? 1e+6d : (averageSecondaryVoltage / (load / averageSecondaryVoltage)))
                    .energyLimitedSource(nodes[4], nodes[1], 5000_000, -averageSecondaryVoltage * ratio)
                    .resistor(nodes[0], nodes[1], 50000000)
                    .resistor(nodes[5], nodes[3], 0.1);

            if (Math.abs(lastPrimaryVoltage / ratio) > Math.abs(lastSecondaryVoltage) && load < 0.0001)
                extraData.remove("Backwards");
        } else {
            double load = Math.max(0, rawLoad);

            bridges.builder(pos)
                    .node(nodes[4])
                    .node(nodes[5])
                    .resistor(nodes[0], nodes[4], 0.1)
                    .resistor(nodes[4], nodes[1], Math.abs(averagePrimaryVoltage) < 1e-6d || Math.abs(load) < 1e-6d ? 1e+6d : (averagePrimaryVoltage / (load / averagePrimaryVoltage)))
                    .energyLimitedSource(nodes[2], nodes[5], 5000_000, -averagePrimaryVoltage / ratio)
                    .resistor(nodes[2], nodes[3], 50000000)
                    .resistor(nodes[5], nodes[3], 0.001);
            if (Math.abs(lastPrimaryVoltage / ratio) < Math.abs(lastSecondaryVoltage) && load < 0.0001)
                extraData.putBoolean("Backwards", true);
        }
    }

    public static double postTick(InWorldNode[] nodes, SimulationResults results, CompoundTag extraData) {
        double primaryVoltage = results.getVoltageAt(nodes[0], nodes[1]);
        extraData.putDouble("LastPrimaryVoltage", primaryVoltage);
        double secondaryVoltage = results.getVoltageAt(nodes[2], nodes[3]);
        double secondaryCurrent = results.getCurrentThrough(nodes[5], nodes[3]);
        extraData.putDouble("LastSecondaryVoltage", secondaryVoltage);
        double load = -secondaryCurrent * secondaryVoltage;
        extraData.putDouble("Load", load);
        double primaryCurrent = results.getCurrentThrough(nodes[0], nodes[4]);
        double feed = primaryCurrent * primaryVoltage;
        extraData.putDouble("Feed", feed);

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

        return extraData.getBoolean("Backwards") ? feed : load;
    }

    public static InWorldNode[] setupStandardNodes(BlockPos pos) {
        return new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(2, pos), new InWorldNode(3, pos), new InWorldNode(4, pos), new InWorldNode(5, pos)};
    }
}
