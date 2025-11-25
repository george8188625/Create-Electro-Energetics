package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CapacitorDevice extends SimulatedDevice<CapacitorDevice.DataHolder> {
    public CapacitorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double capacitance = extraData.capacitance + 0.000001;
        double timeStep = 0.05;

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * extraData.lastVoltage;

        bridges.builder(pos)
                .node(2)
                .connect(0, 2, new ElectricalProperties(1 / conductance, 0, -historyCurrent))
                .resistor(1, 2, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double voltage = results.getVoltageAt(pos, 0, 2);
        extraData.lastVoltage = voltage;

        extraData.temp = updateTemp(extraData.temp, (float) Math.abs(voltage));

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
    }
}
