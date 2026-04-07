package com.george_vi.electroenergetics.content.relay;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class RelayDevice extends SimpleElectricalDevice {
    public boolean inverted;
    public boolean closed;
    public float temp;

    // 0, 1 - coil
    // 2, 3 - switched load
    public RelayDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .resistor(0, 1, 1000);
        if (closed ^ inverted)
            bridges.builder(pos)
                    .resistor(2, 3, 0.1);
    }

    @Override
    public void postTick(SimulationResults results) {

        double voltage = Math.abs(results.getVoltageAt(pos, 0, 1));
        boolean oldClosed = closed;
        if (oldClosed != (voltage > 4)) {
            if (level.isLoaded(pos))
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1);
            closed = voltage > 4;
        }

        float loss = (float) results.getHeatLoss(pos, 0, 1);
        temp = updateTemp(temp, Math.min(loss, 10000));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 550) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 400)
            showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        inverted = tag.getBoolean("Inverted");
        closed = tag.getBoolean("Closed");
        temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        if (closed)
            tag.putBoolean("Closed", true);
        if (inverted)
            tag.putBoolean("Inverted", true);
        tag.putFloat("Temp", temp);
    }


}
