package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import com.google.common.util.concurrent.AtomicDouble;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreePhaseAlternatorBrushesDevice extends SimpleElectricalDevice {

    public float voltage;
    public float stress;
    public float rpmSpeed;
    public AtomicDouble storedEnergy = new AtomicDouble();
    public AtomicInteger workCounter = new AtomicInteger();
    public VirtualRotor virtualRotor = new VirtualRotor();
    public BlockPos otherBrush;
    public boolean fast;
    public boolean slow;
    public float controlModifier;
    public AlternatorBrushesBlockEntity be;
    public PhaseWindingProperties phaseA = new PhaseWindingProperties(0,
            storedEnergy, workCounter, virtualRotor);
    public PhaseWindingProperties phaseB = new PhaseWindingProperties(120,
            storedEnergy, workCounter, virtualRotor);
    public PhaseWindingProperties phaseC = new PhaseWindingProperties(240,
            storedEnergy, workCounter, virtualRotor);
    
    public ThreePhaseAlternatorBrushesDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (level.isLoaded(pos)) {
            ThreePhaseAlternatorBrushesDevice device = this.otherBrush == null ? null :
                    deviceSD.getDevice(this.otherBrush, ThreePhaseAlternatorBrushesDevice.class);
            ElectricalProperties wire = ElectricalProperties.resistor(0.05);
            this.phaseA.targetVoltage = this.voltage;
            this.phaseB.targetVoltage = this.voltage;
            this.phaseC.targetVoltage = this.voltage;

            this.workCounter.set(0);
            this.phaseA.resistance = this.phaseB.resistance = this.phaseC.resistance = 1;
            this.phaseA.currentSource = this.phaseB.currentSource = this.phaseC.currentSource = 0;
            this.phaseA.stress = this.phaseB.stress = this.phaseC.stress = this.stress;
            this.virtualRotor.totalMicroTicks = bridges.microTicks();
            boolean reversed = this.rpmSpeed < 0;
            this.virtualRotor.rpm = Math.abs(this.rpmSpeed) +
                    RandomSource.create(pos.asLong()).nextFloat() * 0.009f +
                    this.controlModifier;

            this.virtualRotor.stress = this.stress;
            if (device == null || this.otherBrush.compareTo(pos) > 0) {
                // Swaps phase A and C if reversed, since speed is passed as an absolute value
                bridges.builder(pos)
                        .connect(1, 0, reversed ? this.phaseC : this.phaseA)
                        .connect(2, 0, this.phaseB)
                        .connect(3, 0, reversed ? this.phaseA : this.phaseC);
            } else {
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, this.otherBrush), wire);
                bridges.bridge(new InWorldNode(1, pos), new InWorldNode(1, this.otherBrush), wire);
                bridges.bridge(new InWorldNode(2, pos), new InWorldNode(2, this.otherBrush), wire);
                bridges.bridge(new InWorldNode(3, pos), new InWorldNode(3, this.otherBrush), wire);
            }
        }

        if (this.slow == this.fast) {
            this.controlModifier *= 0.995f;
        } else {
            float v = this.slow ? -0.01f : 0.01f;
            this.controlModifier = Mth.clamp(this.controlModifier + v, -1f, 1f);
        }
    }

    @Override
    public void postTick(SimulationResults results) {
        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof AlternatorBrushesBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                float v = (float) results.getVoltageAt(pos, 1, 0);
                if (Math.abs(this.be.voltage - v) > 2) {
                    this.be.voltage = v;
                    this.be.sendData();
                }
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        this.controlModifier = tag.getFloat("ControlModifier");
        this.stress = tag.getFloat("Stress");
        this.voltage = tag.getFloat("Voltage");
        this.rpmSpeed = tag.getFloat("RPM");
        this.storedEnergy.set(tag.getFloat("StoredEnergy"));
        this.virtualRotor.angle = tag.getFloat("Angle");
        this.otherBrush = tag.contains("OtherBrush") ? NBTHelper.readBlockPos(tag, "OtherBrush") : null;
        this.fast = tag.getBoolean("Fast");
        this.slow = tag.getBoolean("Slow");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putFloat("ControlModifier", this.controlModifier);
        tag.putFloat("Stress", this.stress);
        tag.putFloat("Voltage", this.voltage);
        tag.putFloat("RPM", this.rpmSpeed);
        tag.putFloat("Angle", (float) this.virtualRotor.angle);
        tag.putDouble("StoredEnergy", this.storedEnergy.get());
        if (this.otherBrush != null)
            tag.put("OtherBrush", NbtUtils.writeBlockPos(this.otherBrush));
        if (this.slow)
            tag.putBoolean("Slow", true);
        if (this.fast)
            tag.putBoolean("Fast", true);
    }

    public static class PhaseWindingProperties extends MicroTickingElectricalProperties {
        final float offset;
        public double targetVoltage;
        public double v;
        public double stress;
        final AtomicDouble storedEnergy;
        final AtomicInteger workCounter;
        final VirtualRotor virtualRotor;


        public PhaseWindingProperties(float offset, AtomicDouble storedEnergy, AtomicInteger workCounter,
                                      VirtualRotor virtualRotor) {
            this.offset = offset;
            this.storedEnergy = storedEnergy;
            this.workCounter = workCounter;
            this.virtualRotor = virtualRotor;
        }

        @Override
        public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
            if (workCounter.getAndIncrement() % 3 == 0) {
                virtualRotor.advance();
            }
            double angle = virtualRotor.angle + offset;
            v = targetVoltage * Math.sin(Math.toRadians(angle));

            double energy = storedEnergy.get();
            if (energy <= 0) {
                this.resistance = 1e3;
                this.currentSource = 0;
                return;
            }

            double maxPower = energy / 3.0;
            double rs = (v * v) / (4.0 * maxPower);
            rs = Math.max(rs, 0.01);

            this.resistance = rs;
            this.currentSource = v / rs;
        }

        @Override
        public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {
            double angle = virtualRotor.angle + offset;
            double vd = allVoltages[(n2 << microTickBits) | (microTick)] - allVoltages[(n1 << microTickBits) | (microTick)];
            double current = (v - vd) / this.resistance;

            double powerDelivered = Math.abs(current * vd);
            storedEnergy.addAndGet(-powerDelivered / totalMicroTicks);

            double torque = -current * Math.cos(Math.toRadians(angle));

            virtualRotor.torqueAccumulated += torque;

            int iig = workCounter.incrementAndGet();
            if (iig % 6 == 0) { // if this is the last one executed in this micro tick
                storedEnergy.updateAndGet(e -> Mth.clamp(e + (stress / totalMicroTicks), 0, stress * 10));
            }

            if (iig == totalMicroTicks * 6) { // if this is the last one executed in this game tick
                virtualRotor.swing();
            }
        }
    }
}
