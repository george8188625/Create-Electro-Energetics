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

public class EnergyMeterDevice extends SimpleElectricalDevice {
    public SwitchingBehaviour behaviour1;
    public SwitchingBehaviour behaviour2;
    public double totalEnergy;
    public boolean isClosed;
    public EnergyMeterBlockEntity be;
    
    public EnergyMeterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r1 = behaviour1.resistance();
        double r2 = behaviour2.resistance();
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (r1 < 1e+10d)
            builder.resistor(0, 2, r1);
        if (r2 < 1e+10d)
            builder.resistor(1, 3, r2);
        builder.resistor(0, 1, 9999);
    }

    double[] v0s;
    double[] v1s;
    double[] v2s;

    @Override
    public void postTick(SimulationResults results) {
        v0s = results.getVoltages(new InWorldNode(0, pos), v0s);
        v1s = results.getVoltages(new InWorldNode(1, pos), v1s);
        v2s = results.getVoltages(new InWorldNode(2, pos), v2s);
        double power = 0;

        int length = Math.min(v0s.length, Math.min(v1s.length, v2s.length));

        for (int i = 0; i < length; i++) {

            double amps = (v0s[i] - v2s[i]) / behaviour1.resistance();

            if (Math.abs(amps) > 0.01) {
//                energy += amps * (v0s[i] - v1s[i]) * (0.05/8);
                double vs = v0s[i] - v1s[i];
                double thisPower = amps * vs;
                this.totalEnergy += (thisPower / 72000) / (1000 * length);
                power += thisPower;
            }
        }

        power /= length;

        double v1 = results.getVoltageAt(pos, 0, 2);
        double v2 = results.getVoltageAt(pos, 1, 3);
        behaviour1.isClosed = behaviour2.isClosed = isClosed;

        boolean loaded = level.isLoaded(pos);
        Vec3 pPos = loaded ? pos.getCenter() : null;

        behaviour1.postTickNoParticles(v1, pPos, level);
        behaviour2.postTickNoParticles(v2, pPos, level);

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
        totalEnergy = tag.getDouble("TotalEnergy");
        isClosed = tag.getBoolean("Closed");
        behaviour1 = new SwitchingBehaviour(tag.getCompound("Behaviour1"));
        behaviour2 = new SwitchingBehaviour(tag.getCompound("Behaviour2"));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putBoolean("Closed", isClosed);
        tag.put("Behaviour1", behaviour1.write());
        tag.put("Behaviour2", behaviour2.write());
    }
}
