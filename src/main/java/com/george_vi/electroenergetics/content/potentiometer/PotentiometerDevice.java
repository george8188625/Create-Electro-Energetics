package com.george_vi.electroenergetics.content.potentiometer;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
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

public class PotentiometerDevice extends SimulatedDevice<PotentiometerDevice.DataHolder> {
    public PotentiometerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double resistance = extraData.resistance + 0.0001;

        bridges.builder(pos)
                .resistor(0, 1, Math.max(0.001, extraData.progress * resistance))
                .resistor(1, 2, Math.max(0.001, (1 - extraData.progress) * resistance));
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        float loss = (float) results.getHeatLoss(pos, 0, 1);
        loss += (float) results.getHeatLoss(pos, 1, 2);
        extraData.temp = updateTemp(extraData.temp, Math.min(loss, 10000));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 40000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 30000)
            showOverheatingParticles(level, pos);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.resistance = tag.getDouble("Resistance");
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.progress = tag.getFloat("Progress");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Resistance", extraData.resistance);
        tag.putFloat("Temp", extraData.temp);
        tag.putFloat("Progress", extraData.progress);
        return tag;
    }

    public static class DataHolder {
        public double resistance;
        public float progress;
        public float temp;
    }
}
