package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AccumulatorDevice extends SimulatedDevice<AccumulatorDevice.DataHolder> {
    public AccumulatorDevice(ResourceLocation id) {
        super(id);
    }

    double capacitance = 30;
    double timeStep = 0.05;

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double diff = Math.abs(extraData.lastTotalVoltage - extraData.lastVoltage);

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * extraData.lastVoltage;

        bridges.builder(pos)
                .node(2)
                .resistor(2, 1, extraData.lastTotalVoltage == 0 ? 100 : (diff / 50) + 0.01);
        bridges.bridge(new InWorldNode(0, pos), new InWorldNode(2, pos), 1 / conductance, 0, -historyCurrent);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        extraData.lastVoltage = results.getVoltageAt(pos, 0, 2);
        extraData.lastTotalVoltage = results.getVoltageAt(pos, 0, 1);
        double v = Math.abs(extraData.lastVoltage);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof AccumulatorBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.energy = (float) ((capacitance / 2) * (v*v)) / 3600;
            }
        }

        float loss = (float) Math.abs(results.getCurrentThrough(pos, 1, 2) * results.getVoltageAt(pos, 0, 1) * results.getVoltageAt(pos, 0, 1)) / 10000;

        extraData.temp = updateTemp(extraData.temp, loss);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 150000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 120000)
            showOverheatingParticles(level, pos);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.lastTotalVoltage = tag.getDouble("LastTotalVoltage");
        dataHolder.lastVoltage = tag.getDouble("LastVoltage");
        dataHolder.temp = tag.getFloat("Temp");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("LastTotalVoltage", extraData.lastTotalVoltage);
        tag.putDouble("LastVoltage", extraData.lastVoltage);
        tag.putFloat("Temp", extraData.temp);
        return tag;
    }

    public static class DataHolder {
        public double lastVoltage;
        public double lastTotalVoltage;
        public float temp;
        public AccumulatorBlockEntity be;
    }
}
