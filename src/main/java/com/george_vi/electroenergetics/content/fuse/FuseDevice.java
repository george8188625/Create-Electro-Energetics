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

public class FuseDevice extends SimulatedDevice {
    public FuseDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (!extraData.getBoolean("Broken"))
            bridges.builder(pos).resistor(0, 1, 0.1);
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        double current = Math.abs(results.getCurrentThrough(pos, 0, 1));
        float temp = extraData.getFloat("Temp");

        float newTemp = (float) (Math.min(current, 500));
        newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
        newTemp = Math.max(temp - 33.3f + newTemp, 0);
        temp = newTemp;

        if (current < 1 || extraData.getBoolean("Broken"))
            temp = 0;
        extraData.putFloat("Temp", temp);


        if (temp > CEEWireTypes.STANDARD.get().getMaxTemperature() * 2/3) {
            extraData.putBoolean("Broken", true);

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
            if (state.getBlock() instanceof FuseBlock fb && fb.broken != extraData.getBoolean("Broken"))
                level.setBlockAndUpdate(pos, (fb.broken ? CEEBlocks.FUSE.get() : CEEBlocks.BROKEN_FUSE.get()).withPropertiesOf(state));
        }
    }
}
