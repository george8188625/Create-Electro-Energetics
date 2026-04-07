package com.george_vi.electroenergetics.content.buzzer;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BuzzerDevice extends SimpleElectricalDevice {
    public float temp;

    public BuzzerDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .resistor(0, 1, 1000);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof BuzzerBlockEntity be) {
                be.setVoltage(Math.abs(results.getVoltageAt(pos, 0, 1)));
            }
        }

        float loss = (float) results.getHeatLoss(pos, 0, 1);
        this.temp = updateTemp(this.temp, Math.min(loss, 10000));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (this.temp > 550) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }

            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (this.temp > 400)
            showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putFloat("Temp", this.temp);
    }
}
