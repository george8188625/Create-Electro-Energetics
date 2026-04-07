package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

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

public class TransformerDevice extends SimpleElectricalDevice {
    public TransformerDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    public TransformerBehaviour.TransformerBehaviourDataHolder transformerData;
    public float temp;
    public double ratio;
    public TransformerBlockEntity be;

    @Override
    public void preTick(BridgeCollector bridges) {
        double ratio = this.ratio;
        if (ratio == 0)
            ratio = 1;

        TransformerBehaviour.preTick(TransformerBehaviour.setupStandardNodes(pos), ratio, pos, bridges, this.transformerData);
    }

    @Override
    public void postTick(SimulationResults results) {
        double power = TransformerBehaviour.postTick(TransformerBehaviour.setupStandardNodes(pos), results, this.transformerData);

        this.temp = updateTemp(this.temp, (float) Math.min(70_000, Math.abs(power)) / 10);

        if (this.be == null)
            if (level.getBlockEntity(pos) instanceof TransformerBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.power = Math.abs(power);
                this.be.primaryVoltage = this.transformerData.lastPrimaryVoltage;
                this.be.secondaryVoltage = this.transformerData.lastSecondaryVoltage;
            }
        }

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (this.temp > 76000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (this.temp > 62000) {
            showOverheatingParticles(level, pos);
        }
    }

    @Override
    public void read(CompoundTag tag) {
        this.temp = tag.getFloat("Temp");
        this.ratio = tag.getDouble("Ratio");
        this.transformerData = new TransformerBehaviour.TransformerBehaviourDataHolder(tag.getCompound("TransformerData"));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putFloat("Temp", this.temp);
        tag.putDouble("Ratio", this.ratio);
        tag.put("TransformerData", this.transformerData.write());
    }
}


