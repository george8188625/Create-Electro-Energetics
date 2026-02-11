package com.george_vi.electroenergetics.content.transformer;

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

public class TransformerDevice extends SimulatedDevice<TransformerDevice.DataHolder> {
    public TransformerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        double ratio = extraData.ratio;
        if (ratio == 0)
            ratio = 1;

        TransformerBehaviour.preTick(TransformerBehaviour.setupStandardNodes(pos), ratio, pos, bridges, extraData.transformerData);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double power = TransformerBehaviour.postTick(TransformerBehaviour.setupStandardNodes(pos), results, extraData.transformerData);

        extraData.temp = updateTemp(extraData.temp, (float) Math.min(70_000, Math.abs(power)) / 10);

        if (extraData.be == null)
            if (level.getBlockEntity(pos) instanceof TransformerBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.power = Math.abs(power);
                extraData.be.primaryVoltage = extraData.transformerData.lastPrimaryVoltage;
                extraData.be.secondaryVoltage = extraData.transformerData.lastSecondaryVoltage;
            }
        }

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 76000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 62000) {
            showOverheatingParticles(level, pos);
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.ratio = tag.getDouble("Ratio");
        dataHolder.transformerData = new TransformerBehaviour.TransformerBehaviourDataHolder(tag.getCompound("TransformerData"));
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Temp", extraData.temp);
        tag.putDouble("Ratio", extraData.ratio);
        tag.put("TransformerData", extraData.transformerData.write());
        return tag;
    }

    public static class DataHolder {
        public TransformerBehaviour.TransformerBehaviourDataHolder transformerData;
        public float temp;
        public double ratio;
        public TransformerBlockEntity be;
    }
}

