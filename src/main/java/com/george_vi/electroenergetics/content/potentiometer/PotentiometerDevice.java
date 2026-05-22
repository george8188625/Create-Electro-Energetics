package com.george_vi.electroenergetics.content.potentiometer;

import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PotentiometerDevice extends SimpleElectricalDevice {
    public double resistance;
    public float progress;
    public boolean oilLogged;
    public float temp;

    public PotentiometerDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double resistance = Math.max(this.resistance, 0.001);

        bridges.builder(pos)
                .resistor(0, 1, Math.max(0.001, progress * resistance))
                .resistor(1, 2, Math.max(0.001, (1 - progress) * resistance));
    }

    @Override
    public void postTick(SimulationResults results) {
        float loss = (float) results.getHeatLoss(pos, 0, 1);
        loss += (float) results.getHeatLoss(pos, 1, 2);

        temp = updateTemp(temp, Math.min(loss, 10000));
        if (oilLogged)
            handleTemp(level, pos, deviceSD, temp, 60_000, 80_000);
        else
            handleTemp(level, pos, deviceSD, temp, 30_000, 40_000);
    }

    @Override
    public void update() {
        super.update();

        oilLogged = level.getFluidState(pos).is(CEEFluids.TRANSFORMER_OIL.get().getSource());
    }

    @Override
    public void read(CompoundTag tag) {
        this.resistance = tag.getDouble("Resistance");
        this.temp = tag.getFloat("Temp");
        this.progress = tag.getFloat("Progress");
        this.oilLogged = tag.getBoolean("OilLogged");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("Resistance", resistance);
        tag.putFloat("Temp", temp);
        tag.putFloat("Progress", progress);
        if (oilLogged)
            tag.putBoolean("OilLogged", true);
    }
}
