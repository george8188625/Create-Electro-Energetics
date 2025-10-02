package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CapacitorDevice extends SimulatedDevice {
    public CapacitorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double capacitance = extraData.getDouble("Capacitance") + 0.000001;
        double timeStep = 0.05;

        double lastVoltage = extraData.getDouble("LastVoltage");

        double conductance = capacitance / timeStep;
        double historyCurrent = conductance * lastVoltage;

        bridges.builder(pos)
                .node(2)
                .connect(0, 2, new ElectricalProperties(1 / conductance, 0, -historyCurrent))
                .resistor(1, 2, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double voltage = results.getVoltageAt(pos, 0, 2);
        extraData.putDouble("LastVoltage", voltage);

        float temp = updateTemp(extraData.getFloat("Temp"), (float) Math.abs(voltage));
        extraData.putFloat("Temp", temp);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 17000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 14000)
            showOverheatingParticles(level, pos);
    }
}
