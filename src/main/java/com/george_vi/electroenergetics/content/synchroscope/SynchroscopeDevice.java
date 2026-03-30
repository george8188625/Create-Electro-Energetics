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

import java.util.ArrayDeque;

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
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                float v = calculatePhaseOffset(pos, results, extraData) % 360;
                if (Math.abs(extraData.be.phaseOffset - v) > 1) {
                    extraData.be.phaseOffset = v;
                    extraData.be.sendData();
                }
            }
        }
    }

    private float calculatePhaseOffset(BlockPos pos, SimulationResults results, DataHolder extraData) {
        double[] p = results.getVoltages(new InWorldNode(0, pos));
        double[] s = results.getVoltages(new InWorldNode(3, pos));

        for (int i = 0; i < p.length; i++) {
            extraData.ticks++;
            double pi = p[i];
            double si = s[i];

            if (pi > 0 && extraData.prevP <= 0) {
                double interpolated = extraData.ticks + (-extraData.prevP / (pi - extraData.prevP));
                extraData.prevPeriodP = interpolated - extraData.prevCrossP;
                extraData.prevCrossP = interpolated;
            }

            if (si > 0 && extraData.prevS <= 0) {
                double interpolated = extraData.ticks + (-extraData.prevS / (si - extraData.prevS));
                extraData.prevPeriodS = interpolated - extraData.prevCrossS;
                extraData.prevCrossS = interpolated;
            }

            extraData.prevP = pi;
            extraData.prevS = si;
        }


        double diff = Mth.TWO_PI * (extraData.prevCrossS - extraData.prevCrossP) / extraData.prevPeriodP;


        double delta = diff - extraData.prevDiff;
        if (delta > 100 || delta < -100) // If delta would ever become Infinity, this would brick words. This prevents it.
            delta = 0;
        while (delta > Mth.PI) delta -= Mth.TWO_PI;
        while (delta < -Mth.PI) delta += Mth.TWO_PI;

        extraData.unwrappedPhase += delta;
        extraData.prevDiff = diff;
        if (Double.isNaN(extraData.unwrappedPhase))
            extraData.unwrappedPhase = delta;
        if (Math.abs(extraData.unwrappedPhase - diff) > 0.1d)
            extraData.unwrappedPhase = diff;
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
        dataHolder.prevDiff = tag.getDouble("PrevDiff");
        dataHolder.unwrappedPhase = tag.getDouble("UnwrappedPhase");
        dataHolder.ticks = tag.getInt("Ticks");
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
        public double prevDiff = 0;
        public double unwrappedPhase = 0;
        public int ticks = 0;
    }
}
