package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class TransformerBehaviour {

    public static void preTick(InWorldNode[] nodes, double ratio, BlockPos pos, BridgeCollector bridges, TransformerBehaviourDataHolder extraData) {
        // 0 | R | 4 U_U_U 1
        // 2 U_U_U 5 | R | 3

        TransformerElectricalProperties ep = new TransformerElectricalProperties(ratio,
                new DirectionalNodeConnection(nodes[4], nodes[1]),
                new DirectionalNodeConnection(nodes[2], nodes[5]));

        bridges.builder(pos)
                .node(nodes[4])
                .node(nodes[5])
                .resistor(nodes[0], nodes[4], 0.1)
                .resistor(nodes[5], nodes[3], 0.1)
                .connect(nodes[5], nodes[2], ep.getOtherProperties())
                .connect(nodes[1], nodes[4], ep);

    }

    public static double postTick(InWorldNode[] nodes, SimulationResults results, TransformerBehaviourDataHolder extraData) {
        double primaryVoltage = results.getVoltageAt(nodes[0], nodes[1]);
        double secondaryVoltage = results.getVoltageAt(nodes[2], nodes[3]);
        double secondaryCurrent = results.getCurrentThrough(nodes[5], nodes[3]);
        double primaryCurrent = results.getCurrentThrough(nodes[0], nodes[4]);
        extraData.lastPrimaryVoltage = primaryVoltage;
        extraData.lastSecondaryVoltage = secondaryVoltage;
        double load = -secondaryCurrent * secondaryVoltage;
        extraData.load = load;
        double feed = primaryCurrent * primaryVoltage;
        extraData.feed = feed;

        extraData.average(primaryVoltage, secondaryVoltage);

        return extraData.backwards ? feed : load;
    }

    public static InWorldNode[] setupStandardNodes(BlockPos pos) {
        return new InWorldNode[] {new InWorldNode(0, pos), new InWorldNode(1, pos), new InWorldNode(2, pos), new InWorldNode(3, pos), new InWorldNode(4, pos), new InWorldNode(5, pos)};
    }

    public static class TransformerBehaviourDataHolder {
        public RMSHolder averagePrimaryVoltage;
        public RMSHolder averageSecondaryVoltage;
        public double lastPrimaryVoltage;
        public double lastSecondaryVoltage;
        public boolean backwards;
        public final int averageWindowSize;
        public double feed;
        public double load;

        public TransformerBehaviourDataHolder() {
            averageWindowSize = 22;
            averagePrimaryVoltage = new RMSHolder(averageWindowSize);
            averageSecondaryVoltage = new RMSHolder(averageWindowSize);
        }

        public TransformerBehaviourDataHolder(CompoundTag tag) {
            this();

            averagePrimaryVoltage.read(tag, "AveragePrimaryVoltage");
            averageSecondaryVoltage.read(tag, "AverageSecondaryVoltage");
            lastPrimaryVoltage = tag.getDouble("LastPrimaryVoltage");
            lastSecondaryVoltage = tag.getDouble("LastSecondaryVoltage");
            backwards = tag.getBoolean("Backwards");

            long[] pArr = tag.getLongArray("PrimaryVoltages");
            long[] sArr = tag.getLongArray("SecondaryVoltages");

            feed = tag.getDouble("Feed");
            load = tag.getDouble("Load");
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            averagePrimaryVoltage.write(tag, "AveragePrimaryVoltage");
            averageSecondaryVoltage.write(tag, "AverageSecondaryVoltage");
            tag.putDouble("LastPrimaryVoltage", lastPrimaryVoltage);
            tag.putDouble("LastSecondaryVoltage", lastSecondaryVoltage);
            tag.putBoolean("Backwards", backwards);

            tag.putDouble("Feed", feed);
            tag.putDouble("Load", load);
            return tag;
        }

        public void average(double primaryVoltage, double secondaryVoltage) {
            averagePrimaryVoltage.add(primaryVoltage);
            averageSecondaryVoltage.add(secondaryVoltage);
        }

    }
}
