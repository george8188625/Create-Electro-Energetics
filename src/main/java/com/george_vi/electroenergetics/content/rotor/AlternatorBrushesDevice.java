package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.foundation.base.GeneratingDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;

public class AlternatorBrushesDevice extends GeneratingDevice {
    public float voltage;
    public float stress;
    public BlockPos otherBrush;
    public AlternatorBrushesBlockEntity be;

    public AlternatorBrushesDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (level.isLoaded(pos)) {

            AlternatorBrushesDevice device = this.otherBrush == null ? null : deviceSD.getDevice(this.otherBrush, AlternatorBrushesDevice.class);
            if (device == null)
                super.preTick(bridges);
            else {
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, this.otherBrush), ElectricalProperties.resistor(0.05));
                bridges.bridge(new InWorldNode(1, pos), new InWorldNode(1, this.otherBrush), ElectricalProperties.resistor(0.05));
                if (this.otherBrush.compareTo(pos) > 0)
                    super.preTick(bridges);
            }
        }
    }


    @Override
    public void postTick(SimulationResults results) {
        if (this.otherBrush == null || this.otherBrush.compareTo(pos) > 0)
            super.postTick(results);

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
    protected double getVoltage() {
        return voltage;
    }

    @Override
    protected double getPower() {
        return stress;
    }

    @Override
    public void read(CompoundTag tag) {
        this.stress = tag.getFloat("Stress");
        this.voltage = tag.getFloat("Voltage");
        this.storedEnergy = tag.getDouble("StoredEnergy");
        this.otherBrush = tag.contains("OtherBrush") ? NBTHelper.readBlockPos(tag, "OtherBrush") : null;
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putFloat("Stress", this.stress);
        tag.putFloat("Voltage", this.voltage);
        tag.putDouble("StoredEnergy", this.storedEnergy);
        if (this.otherBrush != null)
            tag.put("OtherBrush", NbtUtils.writeBlockPos(this.otherBrush));
    }
}
