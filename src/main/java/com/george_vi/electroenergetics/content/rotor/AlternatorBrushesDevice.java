package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity;
import com.george_vi.electroenergetics.foundation.base.GeneratingDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class AlternatorBrushesDevice extends GeneratingDevice<AlternatorBrushesDevice.DataHolder> {
    public AlternatorBrushesDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (level.isLoaded(pos)) {

            SimulatedDeviceInstance<?> deviceInstance = extraData.otherBrush == null ? null : bridges.getSD().getDevice(extraData.otherBrush);
            if (deviceInstance == null)
                super.preTick(pos, level, bridges, extraData);
            else {
                bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, extraData.otherBrush), ElectricalProperties.resistor(0.05));
                bridges.bridge(new InWorldNode(1, pos), new InWorldNode(1, extraData.otherBrush), ElectricalProperties.resistor(0.05));
                if (extraData.otherBrush.compareTo(pos) > 0)
                    super.preTick(pos, level, bridges, extraData);
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
    protected double getVoltage(BlockPos pos, Level level, DataHolder extraData) {
        return extraData.voltage;
    }

    @Override
    protected double getPower(BlockPos pos, Level level, DataHolder extraData) {
        return extraData.stress;
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.stress = tag.getFloat("Stress");
        dataHolder.voltage = tag.getFloat("Voltage");
        dataHolder.storedEnergy = tag.getDouble("StoredEnergy");
        dataHolder.otherBrush = tag.contains("OtherBrush") ? NBTHelper.readBlockPos(tag, "OtherBrush") : null;
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Stress", extraData.stress);
        tag.putFloat("Voltage", extraData.voltage);
        tag.putDouble("StoredEnergy", extraData.storedEnergy);
        if (extraData.otherBrush != null)
            tag.put("OtherBrush", NbtUtils.writeBlockPos(extraData.otherBrush));
        return tag;
    }

    public static class DataHolder extends GeneratingDevice.DataHolder {
        public float voltage;
        public float stress;
        public BlockPos otherBrush;
        public AlternatorBrushesBlockEntity be;
    }
}
