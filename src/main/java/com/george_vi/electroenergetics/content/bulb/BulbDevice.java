package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorDevice;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BulbDevice extends SimulatedDevice<BulbDevice.DataHolder> {
    public BulbDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (!extraData.destroyed)
            bridges.builder(pos).resistor(0, 1, CEEConfigs.server().resistanceValues.bulbResistance.get());
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double vd = Math.abs(results.getVoltageAt(pos, 0) - results.getVoltageAt(pos, 1));
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (extraData.destroyed) {
                if (CEEBlocks.BULB.has(state)) {
                    level.setBlockAndUpdate(pos, CEEBlocks.BROKEN_BULB.get().withPropertiesOf(state));
                    Vec3 pPos = Vec3.atCenterOf(pos);
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pPos, 40, new SendSparkPacket(pPos, SendSparkPacket.SparkSize.SMALL));
                }
                return;
            }

            int light = getLightLevel(vd);

            if (state.getBlock() instanceof BulbBlock) {
                int blockLight = state.getValue(BulbBlock.LIGHT);
                if (blockLight != light)
                    level.setBlockAndUpdate(pos, state.setValue(BulbBlock.LIGHT, light));

                if (extraData.be == null)
                    if (level.getBlockEntity(pos) instanceof BulbBlockEntity be)
                        extraData.be = be;

                if (extraData.be != null) {
                    if (extraData.be.isRemoved())
                        extraData.be = null;
                    else {
                        float newLight = (float) Math.min(1, vd / 500);
                        newLight = 1 - (1 - newLight) * (1 - newLight);

                        if (Math.abs(extraData.be.light - newLight) > 0.025) {
                            extraData.be.light = newLight;
                            extraData.be.sendData();
                        }
                    }
                }
            }
        }
        float loss = (float) results.getHeatLoss(pos, 0, 1);
        extraData.temp = updateTemp(extraData.temp, loss / 200);

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (extraData.temp > 50) {
            extraData.destroyed = true;
            extraData.temp = 0;
        }
    }

    private int getLightLevel(double vd) {
        return Mth.floor(Math.min(Math.round(vd / 19), 15));
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.destroyed = tag.getBoolean("Destroyed");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Temp", extraData.temp);
        tag.putBoolean("Destroyed", extraData.destroyed);
        return tag;
    }

    public static class DataHolder {
        public float temp;
        boolean destroyed;
        public BulbBlockEntity be;
    }
}
