package com.george_vi.electroenergetics.content.relay;

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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class RelayDevice extends SimulatedDevice {
    public RelayDevice(ResourceLocation id) {
        super(id);
    }

    // 0, 1 - coil
    // 2, 3 - switched load
    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        bridges.builder(pos)
                .resistor(0, 1, 1000);
        if (extraData.getBoolean("Closed") ^ extraData.getBoolean("Inverted"))
            bridges.builder(pos)
                    .resistor(2, 3, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double voltage = Math.abs(results.getVoltageAt(pos, 0, 1));
        boolean oldClosed = extraData.getBoolean("Closed");
        if (oldClosed != (voltage > 4)) {
            if (level.isLoaded(pos))
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            extraData.putBoolean("Closed", voltage > 4);
        }

        float loss = (float) results.getHeatLoss(pos, 0, 1);
        float temp = updateTemp(extraData.getFloat("Temp"), Math.min(loss, 10000));
        extraData.putFloat("Temp", temp);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 550) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 400)
            showOverheatingParticles(level, pos);
    }
}
