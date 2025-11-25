package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FuseDevice extends SimulatedDevice<FuseDevice.DataHolder> {
    public FuseDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (!extraData.isBroken)
            bridges.builder(pos).resistor(0, 1, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        double current = Math.abs(results.getCurrentThrough(pos, 0, 1));

        float newTemp = (float) (Math.min(current, 500));
        newTemp *= Math.min(extraData.temp < 0 ? 0 : 1 / (1 + (extraData.temp / 1000)), 1);
        newTemp = Math.max(extraData.temp - 33.3f + newTemp, 0);
        extraData.temp = newTemp;

        if (current < 1 || extraData.isBroken)
            extraData.temp = 0;


        if (extraData.temp > CEEWireTypes.STANDARD.get().getMaxTemperature() * 2/3) {
            extraData.isBroken = true;

            if (level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof FuseBlock fb && !fb.broken) {
                    Vec3 pPos = Vec3.atCenterOf(pos);
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pPos, 40, new SendSparkPacket(pPos, SendSparkPacket.SparkSize.SMALL));
                }
            }
        }


        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof FuseBlock fb && fb.broken != extraData.isBroken)
                level.setBlockAndUpdate(pos, (fb.broken ? CEEBlocks.FUSE.get() : CEEBlocks.BROKEN_FUSE.get()).withPropertiesOf(state));
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.isBroken = tag.getBoolean("Broken");
        dataHolder.temp = tag.getFloat("Temp");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Broken", extraData.isBroken);
        tag.putFloat("Temp", extraData.temp);
        return tag;
    }

    public static class DataHolder {
        public boolean isBroken;
        public float temp;
    }
}
