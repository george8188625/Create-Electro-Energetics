package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class ResistorDevice extends SimpleElectricalDevice {
    public final boolean creative;
    public float temp;
    public boolean oilLogged;
    public ElectricalProperties properties;

    public ResistorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, boolean creative) {
        super(level, pos, deviceSD, type);
        this.creative = creative;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .connect(0, 1, this.properties);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (creative)
            return;

        float loss = (float) results.getHeatLoss(pos, 0, 1);
        this.temp = ElectricalDevice.updateTemp(this.temp, Math.min(loss, 10000));
        if (oilLogged)
            ElectricalDevice.handleTemp(level, pos, deviceSD, temp, 60_000, 80_000);
        else
            ElectricalDevice.handleTemp(level, pos, deviceSD, temp, 30_000, 40_000);
    }

    @Override
    public void update() {
        super.update();

        oilLogged = level.getFluidState(pos).is(CEEFluids.TRANSFORMER_OIL.get().getSource());
    }

    @Override
    public void read(CompoundTag tag) {
        this.properties = ElectricalProperties.resistor(Mth.clamp(tag.getDouble("Resistance"), 0.01, 1_000_000));
        this.temp = tag.getFloat("Temp");
        this.oilLogged = tag.getBoolean("OilLogged");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("Resistance", this.properties.resistance);
        tag.putFloat("Temp", this.temp);
        if (oilLogged)
            tag.putBoolean("OilLogged", true);
    }
}

