package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.simulator.MicroTickingElectricalProperties;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.DoubleSupplier;

public class CapacitorDevice extends SimulatedDevice<CapacitorDevice.DataHolder> {
    public final DoubleSupplier maxVoltage;
    public CapacitorDevice(ResourceLocation id, DoubleSupplier maxVoltage) {
        super(id);
        this.maxVoltage = maxVoltage;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos)
                .node(2)
                .connect(0, 2, extraData.properties)
                .resistor(1, 2, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double voltage = results.getVoltageAt(pos, 0, 2);

        extraData.temp = updateTemp(extraData.temp, (float) ((Math.abs(voltage) * 500) / maxVoltage.getAsDouble()));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 17000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
            sd.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (extraData.temp > 14000)
            showOverheatingParticles(level, pos);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.lastVoltage = tag.getDouble("LastVoltage");
        dataHolder.capacitance = tag.getDouble("Capacitance");
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.properties = new CapacitorProperties(dataHolder);
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("LastVoltage", extraData.lastVoltage);
        tag.putDouble("Capacitance", extraData.capacitance);
        tag.putFloat("Temp", extraData.temp);
        return tag;
    }

    public static class DataHolder {
        public double lastVoltage;
        public double capacitance;
        public float temp;
        public CapacitorProperties properties;
    }

    static class CapacitorProperties extends MicroTickingElectricalProperties {
        final DataHolder extraData;

        CapacitorProperties(DataHolder extraData) {
            this.extraData = extraData;
        }

        @Override
        public void tick(double[] allVoltages, int microTick, int microTickBits, int totalMicroTicks, int n1, int n2) {
            tickCapacitor(totalMicroTicks);
        }

        @Override
        public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int microTickBits, int totalMicroTicks) {
            extraData.lastVoltage = allVoltages[(n1 << microTickBits) | (microTick)] - allVoltages[(n2 << microTickBits) | (microTick)];
        }

        private void tickCapacitor(int totalMicroTicks) {
            double capacitance = extraData.capacitance + 0.000001;
            double timeStep = 0.05 / totalMicroTicks;

            double conductance = capacitance / timeStep;
            double historyCurrent = conductance * extraData.lastVoltage;

            this.resistance = 1 / conductance;
            this.currentSource = -historyCurrent;
        }
    }
}
