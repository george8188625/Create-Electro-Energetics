package com.george_vi.electroenergetics.content.electronic_components.inductor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.electrical_properties.InductorProperties;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.DoubleSupplier;

public class InductorDevice extends SimpleElectricalDevice {
    public final DoubleSupplier maxVoltage;

    public double lastCurrent;
    public double inductance;
    public float temp;
    public InductorProperties properties;
    
    public InductorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, DoubleSupplier maxVoltage) {
        super(level, pos, deviceSD, type);
        this.maxVoltage = maxVoltage;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        properties.inductance = inductance;
        properties.lastCurrent = lastCurrent;

        bridges.builder(pos)
                .node(2)
                .connect(0, 2, properties)
                .resistor(1, 2, 0.1);
    }

    @Override
    public void postTick(SimulationResults results) {
        lastCurrent = properties.lastCurrent;

        double voltage = results.getVoltageAt(pos, 0, 2);

        temp = updateTemp(temp, (float) ((Math.abs(voltage) * 500) / maxVoltage.getAsDouble()));

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
            showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        lastCurrent = tag.getDouble("LastCurrent");
        inductance = tag.getDouble("Inductance");
        temp = tag.getFloat("Temp");
        properties = new InductorProperties();
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("LastCurrent", lastCurrent);
        tag.putDouble("Inductance", inductance);
        tag.putFloat("Temp", temp);
    }
}
