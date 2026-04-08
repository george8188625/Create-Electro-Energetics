package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AccumulatorDevice extends SimpleElectricalDevice {
    double capacitance = 30;
    double timeStep = 0.05;

    public double lastVoltage;
    public double lastTotalVoltage;
    public float temp;
    public AccumulatorBlockEntity be;

    public AccumulatorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        
        double diff = Math.abs(this.lastTotalVoltage - this.lastVoltage);

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * this.lastVoltage;

        bridges.builder(pos)
                .node(2)
                .resistor(2, 1, this.lastTotalVoltage == 0 ? 100 : (diff / 50) + 0.01);
        bridges.bridge(new InWorldNode(0, pos), new InWorldNode(2, pos), 1 / conductance, 0, -historyCurrent);
    }

    @Override
    public void postTick(SimulationResults results) {
        this.lastVoltage = results.getVoltageAt(pos, 0, 2);
        this.lastTotalVoltage = results.getVoltageAt(pos, 0, 1);
        double v = Math.abs(this.lastVoltage);

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof AccumulatorBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.energy = (float) ((capacitance / 2) * (v*v)) / 3600;
            }
        }

        float loss = (float) Math.abs(results.getCurrentThrough(pos, 1, 2) * results.getVoltageAt(pos, 0, 1) * results.getVoltageAt(pos, 0, 1)) / 10000;

        this.temp = updateTemp(this.temp, loss);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (this.temp > 150000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (this.temp > 120000)
            showOverheatingParticles(level, pos);
    }


    @Override
    public void read(CompoundTag tag) {
        this.lastTotalVoltage = tag.getDouble("LastTotalVoltage");
        this.lastVoltage = tag.getDouble("LastVoltage");
        this.temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("LastTotalVoltage", this.lastTotalVoltage);
        tag.putDouble("LastVoltage", this.lastVoltage);
        tag.putFloat("Temp", this.temp);
    }
}
