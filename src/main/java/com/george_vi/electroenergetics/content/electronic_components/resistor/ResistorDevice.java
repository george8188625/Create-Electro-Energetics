package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class ResistorDevice extends SimpleElectricalDevice {
    public final boolean creative;
    public float temp;
    public ElectricalProperties properties;

    public ResistorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, boolean creative) {
        super(level, pos, deviceSD, type);
        this.creative = creative;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.builder(pos)
                .connect(0, 1, this.properties);
    }

    @Override
    public void postTick(SimulationResults results) {
        if (creative)
            return;

        float loss = (float) results.getHeatLoss(pos, 0, 1);
        this.temp = updateTemp(this.temp, Math.min(loss, 10000));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (this.temp > 40000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (this.temp > 30000)
            showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        this.properties = ElectricalProperties.resistor(Mth.clamp(tag.getDouble("Resistance"), 0.01, 1_000_000));
        this.temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("Resistance", this.properties.resistance);
        tag.putFloat("Temp", this.temp);
    }
}

