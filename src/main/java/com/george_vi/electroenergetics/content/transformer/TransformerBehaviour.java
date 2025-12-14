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

    public static void preTick(InWorldNode[] nodes, double ratio, BlockPos pos, BridgeCollector bridges, TransformerBehaviourDataHolder extraData) {

        double rawLoad = extraData.load;
        double rawFeed = -extraData.feed;

        double lastPrimaryVoltage = extraData.lastPrimaryVoltage;
        double lastSecondaryVoltage = extraData.lastSecondaryVoltage;
        double averagePrimaryVoltage = extraData.averagePrimaryVoltage;
        double averageSecondaryVoltage = extraData.averageSecondaryVoltage;

        if (Math.abs(lastPrimaryVoltage) < 0.1)
            lastPrimaryVoltage = 0;
        if (Math.abs(lastSecondaryVoltage) < 0.1)
            lastSecondaryVoltage = 0;

        if (Math.abs(averagePrimaryVoltage) < 0.1)
            averagePrimaryVoltage = 0;
        if (Math.abs(averageSecondaryVoltage) < 0.1)
            averageSecondaryVoltage = 0;

        if (extraData.backwards) {
            double load = Math.max(0, rawFeed);

            bridges.builder(pos)
                    .node(nodes[4])
                    .node(nodes[5])
                    .resistor(nodes[0], nodes[4], 0.001)
                    .resistor(nodes[2], nodes[5], Math.abs(averageSecondaryVoltage) < 1e-6d || Math.abs(load) < 1e-6d ? 1e+6d : (averageSecondaryVoltage / (load / averageSecondaryVoltage)))
                    .energyLimitedSource(nodes[4], nodes[1], 5000_000, -averageSecondaryVoltage * ratio)
                    .resistor(nodes[0], nodes[1], 50000000)
                    .resistor(nodes[5], nodes[3], 0.0001);

            if (Math.abs(lastPrimaryVoltage / ratio) > Math.abs(lastSecondaryVoltage) && load < 0.0001)
                extraData.backwards = false;
        } else {
            double load = Math.max(0, rawLoad);

            bridges.builder(pos)
                    .node(nodes[4])
                    .node(nodes[5])
                    .resistor(nodes[0], nodes[4], 0.0001)
                    .resistor(nodes[4], nodes[1], Math.abs(averagePrimaryVoltage) < 1e-6d || Math.abs(load) < 1e-6d ? 1e+6d : (averagePrimaryVoltage / (load / averagePrimaryVoltage)))
                    .energyLimitedSource(nodes[2], nodes[5], 5000_000, -averagePrimaryVoltage / ratio)
                    .resistor(nodes[2], nodes[3], 50000000)
                    .resistor(nodes[5], nodes[3], 0.001);
            if (Math.abs(lastPrimaryVoltage / ratio) < Math.abs(lastSecondaryVoltage) && load < 0.0001)
                extraData.backwards = true;
        }
    }

    public static double postTick(InWorldNode[] nodes, SimulationResults results, TransformerBehaviourDataHolder extraData) {
        double primaryVoltage = results.getVoltageAt(nodes[0], nodes[1]);
        extraData.lastPrimaryVoltage = primaryVoltage;
        double secondaryVoltage = results.getVoltageAt(nodes[2], nodes[3]);
        double secondaryCurrent = results.getCurrentThrough(nodes[5], nodes[3]);
        extraData.lastSecondaryVoltage = secondaryVoltage;
        double load = -secondaryCurrent * secondaryVoltage;
        extraData.load = load;
        double primaryCurrent = results.getCurrentThrough(nodes[0], nodes[4]);
        double feed = primaryCurrent * primaryVoltage;
        extraData.feed = feed;

        extraData.average(primaryVoltage, secondaryVoltage);

        return extraData.backwards ? feed : load;
    }

    public static InWorldNode[] setupStandardNodes(BlockPos pos) {
        return new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(2, pos), new InWorldNode(3, pos), new InWorldNode(4, pos), new InWorldNode(5, pos)};
    }

    public static class TransformerBehaviourDataHolder {
        public double averagePrimaryVoltage;
        public double averageSecondaryVoltage;
        public double lastPrimaryVoltage;
        public double lastSecondaryVoltage;
        public boolean backwards;
        public final int averageWindowSize;
        public double[] lastPrimaryVoltages;
        public double[] lastSecondaryVoltages;
        public int averagePointer;
        public double feed;
        public double load;

        public TransformerBehaviourDataHolder() {
            averageWindowSize = 22;
            lastPrimaryVoltages = new double[averageWindowSize];
            lastSecondaryVoltages = new double[averageWindowSize];
        }

        public TransformerBehaviourDataHolder(CompoundTag tag) {
            this();

            averagePrimaryVoltage = tag.getDouble("AveragePrimaryVoltage");
            averageSecondaryVoltage = tag.getDouble("AverageSecondaryVoltage");
            lastPrimaryVoltage = tag.getDouble("LastPrimaryVoltage");
            lastSecondaryVoltage = tag.getDouble("LastSecondaryVoltage");
            backwards = tag.getBoolean("Backwards");

            long[] pArr = tag.getLongArray("PrimaryVoltages");
            long[] sArr = tag.getLongArray("SecondaryVoltages");

            for (int i = 0; i < Math.min(pArr.length, averageWindowSize); i++)
                lastPrimaryVoltages[i] = Double.longBitsToDouble(pArr[i]);

            for (int i = 0; i < Math.min(sArr.length, averageWindowSize); i++)
                lastSecondaryVoltages[i] = Double.longBitsToDouble(sArr[i]);

            feed = tag.getDouble("Feed");
            load = tag.getDouble("Load");
            averagePointer = tag.getInt("AveragePointer");
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("AveragePrimaryVoltage", averagePrimaryVoltage);
            tag.putDouble("AverageSecondaryVoltage", averageSecondaryVoltage);
            tag.putDouble("LastPrimaryVoltage", lastPrimaryVoltage);
            tag.putDouble("LastSecondaryVoltage", lastSecondaryVoltage);
            tag.putBoolean("Backwards", backwards);
            tag.putLongArray("PrimaryVoltages", Arrays.stream(lastPrimaryVoltages).mapToLong(Double::doubleToRawLongBits).toArray());
            tag.putLongArray("SecondaryVoltages", Arrays.stream(lastSecondaryVoltages).mapToLong(Double::doubleToRawLongBits).toArray());
            tag.putDouble("Feed", feed);
            tag.putDouble("Load", load);
            tag.putInt("AveragePointer", averagePointer);
            return tag;
        }

        public void average(double primaryVoltage, double secondaryVoltage) {
            averagePointer = (averagePointer + 1) % averageWindowSize;
            lastPrimaryVoltages[averagePointer] = primaryVoltage;
            lastSecondaryVoltages[averagePointer] = secondaryVoltage;

            averagePrimaryVoltage = 0;
            for (double voltage : lastPrimaryVoltages) averagePrimaryVoltage += voltage;
            averagePrimaryVoltage /= averageWindowSize;

            averageSecondaryVoltage = 0;
            for (double voltage : lastSecondaryVoltages) averageSecondaryVoltage += voltage;
            averageSecondaryVoltage /= averageWindowSize;
        }

    }
}
