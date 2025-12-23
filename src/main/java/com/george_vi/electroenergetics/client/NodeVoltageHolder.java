package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import net.createmod.catnip.animation.AnimationTickHolder;

import java.util.HashMap;
import java.util.Map;

public class NodeVoltageHolder {

    public static Map<InWorldNode, VoltageEntry> NODE_VOLTAGES = new HashMap<>();

    public static Map<InWorldNode, VoltageEntry> getAllVoltages() {
        return NODE_VOLTAGES;
    }

    public static VoltageEntry getVoltageEntry(InWorldNode node) {
        VoltageEntry e = NODE_VOLTAGES.get(node);
        if (e == null)
            return NodeVoltageHolder.VoltageEntry.ZERO;
        if (e.invalid()) {
            NODE_VOLTAGES.remove(node);
            return NodeVoltageHolder.VoltageEntry.ZERO;
        }
        return e;
    }

    public static VoltageEntry getVoltageEntryOrNull(InWorldNode node) {
        VoltageEntry e = NODE_VOLTAGES.get(node);
        if (e == null)
            return null;
        if (e.invalid()) {
            NODE_VOLTAGES.remove(node);
            return null;
        }
        return e;
    }

    public static void addVoltageData(InWorldNode node, VoltageEntry e) {
        NODE_VOLTAGES.put(node, e);
    }

    public static double getVoltageBetween(InWorldNode node1, InWorldNode node2) {
        VoltageEntry e1 = getVoltageEntry(node1);
        VoltageEntry e2 = getVoltageEntry(node2);
        double max = 0, min = 0, sum = 0;
        if (e1.voltages.length != e2.voltages.length)
            return 0;
        for (int i = 0; i < e1.voltages.length; i++) {
            double v = (e1.voltages[i] - e2.voltages[i]);
            sum += v * v;

            if (i == 0) {
                max = min = v;
                continue;
            }

            if (v > max) max = v;
            if (v < min) min = v;

        }

        double rmsVoltage = Math.sqrt(sum);
        // Flip RMS so that for mostly-DC negative voltages, the RMS is also negative.
        if (min < 0 && max < 0)
            rmsVoltage = -rmsVoltage;
        else if (min < 0 && max < min * -0.1)
            rmsVoltage = -rmsVoltage;

        return rmsVoltage;
    }

    public static class VoltageEntry {
        public static final VoltageEntry ZERO;
        public double rmsVoltage;
        public double[] voltages;
        // frequency >= 5 ? AC : DC
        public float frequency;
        public double sentTick;

        boolean isAC() {
            return frequency >= 5;
        }

        public void recompute() {
            sentTick = AnimationTickHolder.getTicks();

            double max = 0, min = 0, sum = 0;
            for (int i = 0; i < voltages.length; i++) {
                double v = voltages[i];
                sum += v * v;

                if (i == 0) {
                    max = min = v;
                    continue;
                }

                if (v > max) max = v;
                if (v < min) min = v;
            }

            rmsVoltage = Math.sqrt(sum / voltages.length);

            // Flip RMS so that for mostly-DC negative voltages, the RMS is also negative.
            if (min < 0 && max < 0)
                rmsVoltage = -rmsVoltage;
            else if (min < 0 && max < min * -0.1)
                rmsVoltage = -rmsVoltage;
        }

        public boolean invalid() {
            return AnimationTickHolder.getTicks() - sentTick > 4 || AnimationTickHolder.getTicks() < sentTick;
        }

        static {
            ZERO = new VoltageEntry();
            ZERO.voltages = new double[1];
        }
    }
}
