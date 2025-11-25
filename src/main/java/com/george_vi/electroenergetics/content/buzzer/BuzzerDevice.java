package com.george_vi.electroenergetics.content.buzzer;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorDevice;
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

public class BuzzerDevice extends SimulatedDevice<BuzzerDevice.DataHolder> {
    public BuzzerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos)
                .resistor(0, 1, 1000);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof BuzzerBlockEntity be) {
                be.setVoltage(Math.abs(results.getVoltageAt(pos, 0, 1)));
            }
        }

        float loss = (float) results.getHeatLoss(pos, 0, 1);
        extraData.temp = updateTemp(extraData.temp, Math.min(loss, 10000));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 550) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 400)
            showOverheatingParticles(level, pos);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.temp = tag.getFloat("Temp");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Temp", extraData.temp);
        return tag;
    }

    public static class DataHolder {
        public float temp;
    }
}
