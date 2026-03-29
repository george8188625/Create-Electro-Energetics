package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import com.google.common.util.concurrent.AtomicDouble;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreePhaseAlternatorBrushesDevice extends SimulatedDevice<ThreePhaseAlternatorBrushesDevice.DataHolder> {
    public ThreePhaseAlternatorBrushesDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (level.isLoaded(pos)) {

            SimulatedDeviceInstance<?> deviceInstance = extraData.otherBrush == null ? null : bridges.getSD().getDevice(extraData.otherBrush);
            ElectricalProperties wire = ElectricalProperties.resistor(0.05);
            extraData.rpmSpeedModifier.set(RandomSource.create(pos.asLong()).nextFloat() * 0.05);
            extraData.phaseA.targetVoltage = extraData.voltage;
            extraData.phaseB.targetVoltage = extraData.voltage;
            extraData.phaseC.targetVoltage = extraData.voltage;

            extraData.workCounter.set(0);
            extraData.phaseA.resistance = extraData.phaseB.resistance = extraData.phaseC.resistance = 1;
            extraData.phaseA.currentSource = extraData.phaseB.currentSource = extraData.phaseC.currentSource = 0;
            extraData.phaseA.stress = extraData.phaseB.stress = extraData.phaseC.stress = extraData.stress;
            if (deviceInstance == null || extraData.otherBrush.compareTo(pos) > 0) {
                bridges.builder(pos)
                        .connect(1, 0, extraData.phaseA)
                        .connect(2, 0, extraData.phaseB)
                        .connect(3, 0, extraData.phaseC);
            } else {
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, extraData.otherBrush), wire);
                bridges.bridge(new InWorldNode(1, pos), new InWorldNode(1, extraData.otherBrush), wire);
                bridges.bridge(new InWorldNode(2, pos), new InWorldNode(2, extraData.otherBrush), wire);
                bridges.bridge(new InWorldNode(3, pos), new InWorldNode(3, extraData.otherBrush), wire);
            }
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        if (extraData.otherBrush == null || extraData.otherBrush.compareTo(pos) > 0)
            super.postTick(pos, level, results, extraData);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof AlternatorBrushesBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                float v = (float) results.getVoltageAt(pos, 1, 0);
                if (Math.abs(extraData.be.voltage - v) > 2) {
                    extraData.be.voltage = v;
                    extraData.be.sendData();
                }
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.stress = tag.getFloat("Stress");
        dataHolder.voltage = tag.getFloat("Voltage");
        dataHolder.rpmSpeedModifier.set(tag.getFloat("RPMOffset"));
        dataHolder.rpmSpeed.set(tag.getFloat("RPMSpeed"));
        dataHolder.currentRotation.set(tag.getFloat("CurrentRotation"));
        dataHolder.storedEnergy.set(tag.getFloat("StoredEnergy"));
        dataHolder.otherBrush = tag.contains("OtherBrush") ? NBTHelper.readBlockPos(tag, "OtherBrush") : null;
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Stress", extraData.stress);
        tag.putFloat("Voltage", extraData.voltage);
        tag.putDouble("RPMOffset", extraData.rpmSpeedModifier.get());
        tag.putDouble("RPMSpeed", extraData.rpmSpeed.get());
        tag.putDouble("CurrentRotation", extraData.currentRotation.get());
        tag.putDouble("StoredEnergy", extraData.storedEnergy.get());
        if (extraData.otherBrush != null)
            tag.put("OtherBrush", NbtUtils.writeBlockPos(extraData.otherBrush));
        return tag;
    }

    public static class DataHolder {
        public float voltage;
        public float stress;
        public AtomicDouble storedEnergy = new AtomicDouble();
        public AtomicDouble rpmSpeed = new AtomicDouble();
        public AtomicDouble rpmSpeedModifier = new AtomicDouble();
        public AtomicDouble currentRotation = new AtomicDouble();
        public AtomicInteger workCounter = new AtomicInteger();
        public BlockPos otherBrush;
        public AlternatorBrushesBlockEntity be;
        public PhaseWindingProperties phaseA = new PhaseWindingProperties(0, currentRotation, rpmSpeed, rpmSpeedModifier, storedEnergy, workCounter);
        public PhaseWindingProperties phaseB = new PhaseWindingProperties(120, currentRotation, rpmSpeed, rpmSpeedModifier, storedEnergy, workCounter);
        public PhaseWindingProperties phaseC = new PhaseWindingProperties(240, currentRotation, rpmSpeed, rpmSpeedModifier, storedEnergy, workCounter);
    }

    public static class PhaseWindingProperties extends MicroTickingElectricalProperties {
        final float offset;
        public double targetVoltage;
        public double v;
        public double stress;
        final AtomicDouble currentAngle;
        final AtomicDouble rpmSpeed;
        final AtomicDouble rpmSpeedModifier;
        final AtomicDouble storedEnergy;
        final AtomicInteger workCounter;


        public PhaseWindingProperties(float offset, AtomicDouble currentAngle, AtomicDouble rpmSpeed,
                                      AtomicDouble rpmSpeedModifier, AtomicDouble storedEnergy, AtomicInteger workCounter) {
            this.offset = offset;
            this.currentAngle = currentAngle;
            this.rpmSpeed = rpmSpeed;
            this.rpmSpeedModifier = rpmSpeedModifier;
            this.storedEnergy = storedEnergy;
            this.workCounter = workCounter;
        }

        @Override
        public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
            v = targetVoltage * Math.sin(Math.toRadians((currentAngle.get() + offset) % 360));

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
            double vd = allVoltages[(n2 << microTickBits) | (microTick)] - allVoltages[(n1 << microTickBits) | (microTick)];
            double current = (vd - v) / this.resistance;

            double powerDelivered = Math.abs(current * vd);
            storedEnergy.addAndGet(-powerDelivered / 3.0);

            if (workCounter.incrementAndGet() % 3 == 0) { // if this is the last one executed in this micro tick
                storedEnergy.updateAndGet(e -> Mth.clamp(e + (stress / totalMicroTicks), 0, stress * 10));
                currentAngle.updateAndGet(s -> {
                    double delta = (((rpmSpeed.floatValue() + rpmSpeedModifier.floatValue()) / 128f) * 360 / totalMicroTicks);
                    return (s + delta) % 360;
                });
            }
        }
    }
}
