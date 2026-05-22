package com.george_vi.electroenergetics.content.synchroscope;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class SynchroscopeDevice extends SimpleElectricalDevice {

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
    public int ticks = 0;

    public boolean isFirstPhaseP;
    public boolean isFirstPhaseS;
    public byte phaseOrderP;
    public byte phaseOrderS;
    public SynchroscopeDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .resistor(0, 1, 1_000_000)
                .resistor(1, 2, 1_000_000)
                .resistor(0, 3, 1_000_000)
                .resistor(1, 4, 1_000_000)
                .resistor(2, 5, 1_000_000);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof SynchroscopeBlockEntity be)
                this.be = be;

        if (be != null) {
            if (be.isRemoved())
                be = null;
            else {
                float v = calculatePhaseOffset(pos, results) % 360;
                boolean validConnection = (phaseOrderP & 0b1100) == (phaseOrderS & 0b1100);
                if ((ticks % 10 == 0 && Math.abs(be.phaseOffset - v) > 0.1) ||
                        be.validConnection != validConnection) {
                    be.phaseOffset = v;
                    be.validConnection = validConnection;
                    be.sendData();
                }
            }
        }
    }

    double[] p;
    double[] s;
    double[] p1;
    double[] s1;
    double[] p2;
    double[] s2;
    private float calculatePhaseOffset(BlockPos pos, SimulationResults results) {
        p = results.getVoltages(new InWorldNode(1, pos), p);
        s = results.getVoltages(new InWorldNode(4, pos), s);
        p1 = results.getVoltages(new InWorldNode(0, pos), p1);
        s1 = results.getVoltages(new InWorldNode(3, pos), s1);
        p2 = results.getVoltages(new InWorldNode(2, pos), p2);
        s2 = results.getVoltages(new InWorldNode(5, pos), s2);

        for (int i = 0; i < p.length; i++) {
            this.ticks++;
            double pi = p[i];
            double si = s[i];
            double pi1 = p1[i];
            double si1 = s1[i];
            double pi2 = p2[i];
            double si2 = s2[i];

            if (pi > 0 && this.prevP <= 0) {
                double interpolated = this.ticks + (-this.prevP / (pi - this.prevP));
                this.prevPeriodP = interpolated - this.prevCrossP;
                this.prevCrossP = interpolated;
                this.isFirstPhaseP = true;
                this.phaseOrderP <<= 2;
            }

            if (si > 0 && this.prevS <= 0) {
                double interpolated = this.ticks + (-this.prevS / (si - this.prevS));
                this.prevPeriodS = interpolated - this.prevCrossS;
                this.prevCrossS = interpolated;
                this.isFirstPhaseS = true;
                this.phaseOrderS <<= 2;
            }

            // The following mess is responsible for detecting if the synchroscope is wired correctly.
            // It checks if the phase ordering is the same, by checking which phases have zero crossings in what order.
            // For some reason I decided to do this very low level.
            // no idea why

            if (pi1 > 0 && this.prevP1 <= 0) {
                this.isFirstPhaseP = false;
            }

            if (si1 > 0 && this.prevS1 <= 0) {
                this.isFirstPhaseS = false;
            }

            if (pi2 > 0 && this.prevP2 <= 0) {
                if (this.isFirstPhaseP) {
                    this.isFirstPhaseP = false;
                    this.phaseOrderP |= 2;
                } else {
                    this.phaseOrderP |= 1;
                }
            }

            if (si2 > 0 && this.prevS2 <= 0) {
                if (this.isFirstPhaseS) {
                    this.isFirstPhaseS = false;
                    this.phaseOrderS |= 2;
                } else {
                    this.phaseOrderS |= 1;
                }
            }

            this.prevP = pi;
            this.prevS = si;
            this.prevP1 = pi1;
            this.prevS1 = si1;
            this.prevP2 = pi2;
            this.prevS2 = si2;
        }


        double diff = Mth.TWO_PI * (this.prevCrossS - this.prevCrossP) / this.prevPeriodP;

        double delta = Double.isNaN(diff) ? 0 : diff - this.prevDiff;
        diff = Double.isNaN(diff) ? 0 : diff;
        if (delta > 100 || delta < -100) // If delta would ever become Infinity, this would brick words. This prevents it.
            delta = 0;
        while (delta > Mth.PI) delta -= Mth.TWO_PI;
        while (delta < -Mth.PI) delta += Mth.TWO_PI;

        this.prevDiff = diff;

        return (float) Math.toDegrees(diff);
    }

    @Override
    public void read(CompoundTag tag) {
        prevPeriodP = tag.getDouble("PrevPeriodP");
        prevPeriodS = tag.getDouble("PrevPeriodS");
        prevCrossP = tag.getDouble("PrevCrossP");
        prevCrossS = tag.getDouble("PrevCrossS");
        prevP = tag.getDouble("PrevP");
        prevS = tag.getDouble("PrevS");
        prevP1 = tag.getDouble("PrevP1");
        prevS1 = tag.getDouble("PrevS1");
        prevP2 = tag.getDouble("PrevP2");
        prevS2 = tag.getDouble("PrevS2");
        prevDiff = tag.getDouble("PrevDiff");
        ticks = tag.getInt("Ticks");
        isFirstPhaseP = tag.getBoolean("IsFirstPhaseP");
        isFirstPhaseS = tag.getBoolean("IsFirstPhaseS");
        phaseOrderP = tag.getByte("PhaseOrderP");
        phaseOrderS = tag.getByte("PhaseOrderS");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("PrevPeriodP", prevPeriodP);
        tag.putDouble("PrevPeriodS", prevPeriodS);
        tag.putDouble("PrevCrossP", prevCrossP);
        tag.putDouble("PrevCrossS", prevCrossS);
        tag.putDouble("PrevP", prevP);
        tag.putDouble("PrevS", prevS);
        tag.putDouble("PrevDiff", prevDiff);
        tag.putInt("Ticks", ticks);
        tag.putDouble("PrevP1", prevP1);
        tag.putDouble("PrevS1", prevS1);
        if (isFirstPhaseP)
            tag.putBoolean("IsFirstPhaseP", true);
        if (isFirstPhaseS)
            tag.putBoolean("IsFirstPhaseS", true);
        tag.putByte("PhaseOrderP", phaseOrderP);
        tag.putByte("PhaseOrderS", phaseOrderS);
    }
}
