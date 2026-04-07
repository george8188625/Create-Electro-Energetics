package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FuseDevice extends SimpleElectricalDevice {
    public boolean isBroken;
    public float temp;

    public FuseDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (!this.isBroken)
            bridges.builder(pos).resistor(0, 1, 0.1);
    }

    @Override
    public void postTick(SimulationResults results) {
        double current = Math.abs(results.getCurrentThrough(pos, 0, 1));

        float newTemp = (float) (Math.min(current, 500));
        newTemp *= Math.min(this.temp < 0 ? 0 : 1 / (1 + (this.temp / 1000)), 1);
        newTemp = Math.max(this.temp - 33.3f + newTemp, 0);
        this.temp = newTemp;

        if (current < 1 || this.isBroken)
            this.temp = 0;


        if (this.temp > CEEWireTypes.STANDARD.get().getMaxTemperature() * 2/3) {
            this.isBroken = true;

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
            if (state.getBlock() instanceof FuseBlock fb && fb.broken != this.isBroken)
                level.setBlockAndUpdate(pos, (fb.broken ? CEEBlocks.FUSE.get() : CEEBlocks.BROKEN_FUSE.get()).withPropertiesOf(state));
        }
    }

    @Override
    public void read(CompoundTag tag) {
        isBroken = tag.getBoolean("Broken");
        temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putBoolean("Broken", this.isBroken);
        tag.putFloat("Temp", this.temp);
    }

    @Override
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() != newState.getBlock().getClass();
    }
}
