package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.config.CEEConfigs;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TransformerDevice extends SimulatedDevice {
    public TransformerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double ratio = extraData.getDouble("Ratio");
        if (ratio == 0)
            ratio = 1;

        TransformerBehaviour.preTick(TransformerBehaviour.setupStandardNodes(pos), ratio, pos, bridges, extraData);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double power = TransformerBehaviour.postTick(TransformerBehaviour.setupStandardNodes(pos), results, extraData);

        float temp = updateTemp(extraData.getFloat("Temp"), (float) Math.abs(power) / 10);
        extraData.putFloat("Temp", temp);

        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof TransformerBlockEntity be)
                be.power = Math.abs(power);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 76000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 62000) {
            showOverheatingParticles(level, pos);
        }
    }
}

