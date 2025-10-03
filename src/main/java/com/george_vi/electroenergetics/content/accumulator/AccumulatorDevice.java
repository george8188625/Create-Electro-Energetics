package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.Node;
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

public class AccumulatorDevice extends SimulatedDevice {
    public AccumulatorDevice(ResourceLocation id) {
        super(id);
    }

    double capacitance = 100;
    double timeStep = 0.05;

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double lastVoltage = extraData.getDouble("LastVoltage");
        double lastTotalVoltage = extraData.getDouble("LastTotalVoltage");
        double diff = Math.abs(lastTotalVoltage - lastVoltage);

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * lastVoltage;

        bridges.builder(pos)
                .node(2)
                .resistor(2, 1, lastTotalVoltage == 0 ? 100 : (diff / 50) + 0.01);
        bridges.bridge(new Node(0, pos), new Node(2, pos), 1 / conductance, 0, -historyCurrent);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        extraData.putDouble("LastVoltage", results.getVoltageAt(pos, 0, 2));
        extraData.putDouble("LastTotalVoltage", results.getVoltageAt(pos, 0, 1));
        double v = Math.abs(results.getVoltageAt(pos, 0, 2));
        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof AccumulatorBlockEntity be) {
                be.energy = (float) ((capacitance / 2) * (v*v)) / 3600;
            }
        }

        float loss = (float) Math.abs(results.getCurrentThrough(pos, 1, 2) * results.getVoltageAt(pos, 0, 1) * results.getVoltageAt(pos, 0, 1)) / 10000;

        float temp = updateTemp(extraData.getFloat("Temp"), loss);
        extraData.putFloat("Temp", temp);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 150000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 120000)
            showOverheatingParticles(level, pos);
    }
}
