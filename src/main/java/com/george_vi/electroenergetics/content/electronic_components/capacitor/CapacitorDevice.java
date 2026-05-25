package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.electrical_properties.CapacitorProperties;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.DoubleSupplier;

public class CapacitorDevice extends SimpleElectricalDevice {
    public final DoubleSupplier maxVoltage;

    public double lastVoltage;
    public double capacitance;
    public float temp;
    public CapacitorProperties properties;

    public CapacitorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, DoubleSupplier maxVoltage) {
        super(level, pos, deviceSD, type);
        this.maxVoltage = maxVoltage;
    }
    
    @Override
    public void preTick(BridgeCollector bridges) {
        properties.capacitance = capacitance;
        properties.lastVoltage = lastVoltage;

        bridges.builder(pos)
                .node(2)
                .connect(0, 2, properties)
                .resistor(1, 2, 0.1);
        super.preTick(bridges);
    }

    @Override
    public void postTick(SimulationResults results) {
        lastVoltage = properties.lastVoltage;

        double voltage = results.getVoltageAt(pos, 0, 2);

        temp = ElectricalDevice.updateTemp(temp, (float) ((Math.abs(voltage) * 500) / maxVoltage.getAsDouble()));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 17000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 14000)
            ElectricalDevice.showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        this.lastVoltage = tag.getDouble("LastVoltage");
        this.capacitance = tag.getDouble("Capacitance");
        this.temp = tag.getFloat("Temp");
        this.properties = new CapacitorProperties();
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("LastVoltage", this.lastVoltage);
        tag.putDouble("Capacitance", this.capacitance);
        tag.putFloat("Temp", this.temp);
    }

}
