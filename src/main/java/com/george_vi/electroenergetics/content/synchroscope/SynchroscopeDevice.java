package com.george_vi.electroenergetics.content.synchroscope;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class SynchroscopeDevice extends SimulatedDevice<SynchroscopeDevice.DataHolder> {
    public SynchroscopeDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos)
                .resistor(0, 1, 1_000_000)
                .resistor(1, 2, 1_000_000)
                .resistor(0, 3, 1_000_000)
                .resistor(1, 4, 1_000_000)
                .resistor(2, 5, 1_000_000);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof SynchroscopeBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            SynchroscopeBlockEntity be = extraData.be;
            if (be.isRemoved())
                extraData.be = null;
            else {
                float v = calculatePhaseOffset(pos, results, extraData) % 360;
                boolean validConnection = (extraData.phaseOrderP & 0b1100) == (extraData.phaseOrderS & 0b1100);
                if (Math.abs(be.phaseOffset - v) > 10 ||
                        (Math.abs(be.phaseOffset - v) > 0.1 && extraData.ticks % 20 == 0) ||
                        be.validConnection != validConnection) {
                    be.phaseOffset = v;
                    be.validConnection = validConnection;
                    be.sendData();
                }
            }
        }
    }

    private float calculatePhaseOffset(BlockPos pos, SimulationResults results, DataHolder extraData) {
        double[] p = results.getVoltages(new InWorldNode(1, pos));
        double[] s = results.getVoltages(new InWorldNode(4, pos));
        double[] p1 = results.getVoltages(new InWorldNode(0, pos));
        double[] s1 = results.getVoltages(new InWorldNode(3, pos));
        double[] p2 = results.getVoltages(new InWorldNode(2, pos));
        double[] s2 = results.getVoltages(new InWorldNode(5, pos));

        for (int i = 0; i < p.length; i++) {
            extraData.ticks++;
            double pi = p[i];
            double si = s[i];
            double pi1 = p1[i];
            double si1 = s1[i];
            double pi2 = p2[i];
            double si2 = s2[i];

            if (pi > 0 && extraData.prevP <= 0) {
                double interpolated = extraData.ticks + (-extraData.prevP / (pi - extraData.prevP));
                extraData.prevPeriodP = interpolated - extraData.prevCrossP;
                extraData.prevCrossP = interpolated;
                extraData.isFirstPhaseP = true;
                extraData.phaseOrderP <<= 2;
            }

            if (si > 0 && extraData.prevS <= 0) {
                double interpolated = extraData.ticks + (-extraData.prevS / (si - extraData.prevS));
                extraData.prevPeriodS = interpolated - extraData.prevCrossS;
                extraData.prevCrossS = interpolated;
                extraData.isFirstPhaseS = true;
                extraData.phaseOrderS <<= 2;
            }

            // The following mess is responsible for detecting if the synchroscope is wired correctly.
            // It checks if the phase ordering is the same, by checking which phases have zero crossings in what order.
            // For some reason I decided to do this very low level.
            // no idea why

            if (pi1 > 0 && extraData.prevP1 <= 0) {
                extraData.isFirstPhaseP = false;
            }

            if (si1 > 0 && extraData.prevS1 <= 0) {
                extraData.isFirstPhaseS = false;
            }

            if (pi2 > 0 && extraData.prevP2 <= 0) {
                if (extraData.isFirstPhaseP) {
                    extraData.isFirstPhaseP = false;
                    extraData.phaseOrderP |= 2;
                } else {
                    extraData.phaseOrderP |= 1;
                }
            }

            if (si2 > 0 && extraData.prevS2 <= 0) {
                if (extraData.isFirstPhaseS) {
                    extraData.isFirstPhaseS = false;
                    extraData.phaseOrderS |= 2;
                } else {
                    extraData.phaseOrderS |= 1;
                }
            }

            extraData.prevP = pi;
            extraData.prevS = si;
            extraData.prevP1 = pi1;
            extraData.prevS1 = si1;
            extraData.prevP2 = pi2;
            extraData.prevS2 = si2;
        }


        double diff = Mth.TWO_PI * (extraData.prevCrossS - extraData.prevCrossP) / extraData.prevPeriodP;

        double delta = Double.isNaN(diff) ? 0 : diff - extraData.prevDiff;
        diff = Double.isNaN(diff) ? 0 : diff;
        if (delta > 100 || delta < -100) // If delta would ever become Infinity, this would brick words. This prevents it.
            delta = 0;
        while (delta > Mth.PI) delta -= Mth.TWO_PI;
        while (delta < -Mth.PI) delta += Mth.TWO_PI;

        extraData.unwrappedPhase += delta;
        extraData.prevDiff = diff;
        if (Double.isNaN(extraData.unwrappedPhase))
            extraData.unwrappedPhase = delta;
//        if (Math.abs(extraData.unwrappedPhase - diff) > 0.1d)
//        extraData.unwrappedPhase = diff;
        return (float) Math.toDegrees(extraData.unwrappedPhase);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.prevPeriodP = tag.getDouble("PrevPeriodP");
        dataHolder.prevPeriodS = tag.getDouble("PrevPeriodS");
        dataHolder.prevCrossP = tag.getDouble("PrevCrossP");
        dataHolder.prevCrossS = tag.getDouble("PrevCrossS");
        dataHolder.prevP = tag.getDouble("PrevP");
        dataHolder.prevS = tag.getDouble("PrevS");
        dataHolder.prevP1 = tag.getDouble("PrevP1");
        dataHolder.prevS1 = tag.getDouble("PrevS1");
        dataHolder.prevP2 = tag.getDouble("PrevP2");
        dataHolder.prevS2 = tag.getDouble("PrevS2");
        dataHolder.prevDiff = tag.getDouble("PrevDiff");
        dataHolder.unwrappedPhase = tag.getDouble("UnwrappedPhase");
        dataHolder.ticks = tag.getInt("Ticks");
        dataHolder.isFirstPhaseP = tag.getBoolean("IsFirstPhaseP");
        dataHolder.isFirstPhaseS = tag.getBoolean("IsFirstPhaseS");
        dataHolder.phaseOrderP = tag.getByte("PhaseOrderP");
        dataHolder.phaseOrderS = tag.getByte("PhaseOrderS");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("PrevPeriodP", extraData.prevPeriodP);
        tag.putDouble("PrevPeriodS", extraData.prevPeriodS);
        tag.putDouble("PrevCrossP", extraData.prevCrossP);
        tag.putDouble("PrevCrossS", extraData.prevCrossS);
        tag.putDouble("PrevP", extraData.prevP);
        tag.putDouble("PrevS", extraData.prevS);
        tag.putDouble("PrevDiff", extraData.prevDiff);
        tag.putDouble("UnwrappedPhase", extraData.unwrappedPhase);
        tag.putInt("Ticks", extraData.ticks);
        tag.putDouble("PrevP1", extraData.prevP1);
        tag.putDouble("PrevS1", extraData.prevS1);
        if (extraData.isFirstPhaseP)
            tag.putBoolean("IsFirstPhaseP", true);
        if (extraData.isFirstPhaseS)
            tag.putBoolean("IsFirstPhaseS", true);
        tag.putByte("PhaseOrderP", extraData.phaseOrderP);
        tag.putByte("PhaseOrderS", extraData.phaseOrderS);
        return tag;
    }

    public static class DataHolder {
        public SynchroscopeBlockEntity be;

        public double prevPeriodP = 0;
        public double prevPeriodS = 0;
        public double prevCrossP = 0;
        public double prevCrossS = 0;
        public double prevP = 0;
        public double prevS = 0;
        public double prevP1 = 0;
        public double prevS1 = 0;
        public double prevP2 = 0;
        public double prevS2 = 0;
        public double prevDiff = 0;
        public double unwrappedPhase = 0;
        public int ticks = 0;

        public boolean isFirstPhaseP;
        public boolean isFirstPhaseS;
        public byte phaseOrderP;
        public byte phaseOrderS;
    }
}
