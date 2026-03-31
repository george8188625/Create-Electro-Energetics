package com.george_vi.electroenergetics.content.varistor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.DoubleSupplier;

public class VaristorDevice extends SimulatedDevice<VaristorDevice.DataHolder> {
    public final DoubleSupplier maxVoltage;
    public VaristorDevice(ResourceLocation id, DoubleSupplier maxVoltage) {
        super(id);
        this.maxVoltage = maxVoltage;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        bridges.builder(pos)
                .connect(0, 1, extraData.properties);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double voltage = results.getVoltageAt(pos, 0,1);

        extraData.voltageAtVaristor = results.getVoltageAt(pos,0);

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
        dataHolder.voltageAtOneAmp = tag.getInt("VoltageAtOneAmp");
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.voltageAtVaristor = tag.getDouble("VoltageAtVaristor");
        dataHolder.properties = new VaristorProperties(dataHolder);
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("VoltageAtOneAmp", extraData.voltageAtOneAmp);
        tag.putFloat("Temp", extraData.temp);
        tag.putDouble("VoltageAtVaristor", extraData.voltageAtVaristor);
        return tag;
    }

    public static class DataHolder {

        /** Voltage at which a current of One Ampere will flow through the Varistor
         Referred to as C (Don't ask me why).
         Beware that Datasheets sometimes use a current of 1mA for this value
        */
        public int voltageAtOneAmp;
        public float temp;
        public double voltageAtVaristor;
        public VaristorProperties properties=new VaristorProperties(this);

    }


}
