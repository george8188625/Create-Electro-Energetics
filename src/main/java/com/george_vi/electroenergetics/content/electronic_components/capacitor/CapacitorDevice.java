package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.electrical_properties.CapacitorProperties;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.DoubleSupplier;

public class CapacitorDevice extends SimulatedDevice<CapacitorDevice.DataHolder> {
    public final DoubleSupplier maxVoltage;
    public CapacitorDevice(ResourceLocation id, DoubleSupplier maxVoltage) {
        super(id);
        this.maxVoltage = maxVoltage;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        extraData.properties.capacitance = extraData.capacitance;

        bridges.builder(pos)
                .node(2)
                .connect(0, 2, extraData.properties)
                .resistor(1, 2, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        extraData.lastVoltage = extraData.properties.lastVoltage;

        double voltage = results.getVoltageAt(pos, 0, 2);

        extraData.temp = updateTemp(extraData.temp, (float) ((Math.abs(voltage) * 500) / maxVoltage.getAsDouble()));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 17000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 14000)
            showOverheatingParticles(level, pos);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.lastVoltage = tag.getDouble("LastVoltage");
        dataHolder.capacitance = tag.getDouble("Capacitance");
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.properties = new CapacitorProperties();
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("LastVoltage", extraData.lastVoltage);
        tag.putDouble("Capacitance", extraData.capacitance);
        tag.putFloat("Temp", extraData.temp);
        return tag;
    }

    public static class DataHolder {
        public double lastVoltage;
        public double capacitance;
        public float temp;
        public CapacitorProperties properties;
    }
}
