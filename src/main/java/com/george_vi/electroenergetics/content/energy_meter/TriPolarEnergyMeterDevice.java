package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TriPolarEnergyMeterDevice extends SimpleElectricalDevice {
    public SwitchingBehaviour behaviour1;
    public SwitchingBehaviour behaviour2;
    public SwitchingBehaviour behaviour3;
    public double totalEnergy;
    public boolean isClosed;
    public EnergyMeterBlockEntity be;

    public TriPolarEnergyMeterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r1 = behaviour1.resistance();
        double r2 = behaviour2.resistance();
        double r3 = behaviour3.resistance();
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (r1 < 1e+10d)
            builder.resistor(0, 3, r1);
        if (r2 < 1e+10d)
            builder.resistor(2, 5, r2);
        if (r3 < 1e+10d)
            builder.resistor(1, 4, r3);
        builder.resistor(0, 1, 9999);
        builder.resistor(1, 2, 9999);
    }

    double[] v0s;
    double[] v1s;
    double[] v2s;
    double[] v3s;
    double[] v5s;
    @Override
    public void postTick(SimulationResults results) {
        v0s = results.getVoltages(new InWorldNode(0, pos), v0s);
        v1s = results.getVoltages(new InWorldNode(1, pos), v1s);
        v2s = results.getVoltages(new InWorldNode(2, pos), v2s);
        v3s = results.getVoltages(new InWorldNode(3, pos), v3s);
        v5s = results.getVoltages(new InWorldNode(5, pos), v5s);
        double power = 0;
        int length = Math.min(Math.min(v0s.length, Math.min(v1s.length, v2s.length)), Math.min(v3s.length, v5s.length));

        for (int i = 0; i < length; i++) {

            double amps1 = (v0s[i] - v3s[i]) / behaviour1.resistance();
            double amps2 = (v2s[i] - v5s[i]) / behaviour2.resistance();

            if (Math.abs(amps1) > 0.01) {
                double vd = v0s[i] - v1s[i];
                double thisPower = amps1 * vd;
                this.totalEnergy += (thisPower / 72000) / (1000 * length);
                power += thisPower;
            }

            if (Math.abs(amps2) > 0.01) {
                double vd = v2s[i] - v1s[i];
                double thisPower = amps2 * vd;
                this.totalEnergy += (thisPower / 72000) / (1000 * length);
                power += thisPower;
            }
        }

        power /= length;

        double v1 = results.getVoltageAt(pos, 0, 3);
        double v2 = results.getVoltageAt(pos, 2, 5);
        double v3 = results.getVoltageAt(pos, 1, 4);
        behaviour1.isClosed = behaviour2.isClosed = behaviour3.isClosed = isClosed;

        boolean loaded = level.isLoaded(pos);
        Vec3 pPos = loaded ? pos.getCenter() : null;

        behaviour1.postTickNoParticles(v1, pPos, level);
        behaviour2.postTickNoParticles(v2, pPos, level);
        behaviour3.postTickNoParticles(v3, pPos, level);

        if (!loaded)
            return;

        if (this.be == null)
            if (level.getBlockEntity(pos) instanceof EnergyMeterBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.setTotalEnergy((float) this.totalEnergy);
                this.be.activePower = this.isClosed ? power : 0;
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        this.totalEnergy = tag.getDouble("TotalEnergy");
        this.isClosed = tag.getBoolean("Closed");
        behaviour1 = new SwitchingBehaviour(tag.getCompound("Behaviour1"));
        behaviour2 = new SwitchingBehaviour(tag.getCompound("Behaviour2"));
        behaviour3 = new SwitchingBehaviour(tag.getCompound("Behaviour3"));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("TotalEnergy", this.totalEnergy);
        tag.putBoolean("Closed", this.isClosed);
        tag.put("Behaviour1", behaviour1.write());
        tag.put("Behaviour2", behaviour2.write());
        tag.put("Behaviour3", behaviour3.write());
    }
}
