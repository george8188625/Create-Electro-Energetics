package com.george_vi.electroenergetics.content.connector;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.transmission_distribution.sf6_breaker.SF6BreakerDevice;
import com.george_vi.electroenergetics.foundation.VirtualRedstoneDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class InsulatorDevice extends SimulatedDevice<InsulatorDevice.DataHolder> implements VirtualRedstoneDevice<InsulatorDevice.DataHolder> {
    public InsulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public boolean ticks() {
        return false;
    }

    @Override
    public void updateRedstoneInput(Level level, BlockPos pos, Direction direction, InsulatorDevice.DataHolder extraData, int power) {
        byte[] powerLevels = extraData.power;
        powerLevels[direction.ordinal()] = (byte) power;
        boolean prevPowered = extraData.powered;
        boolean p = false;
        for (byte b : powerLevels) {
            if (b > 0) {
                p = true;
                break;
            }
        }
        extraData.powered = p;

        if (p != prevPowered) {
            Vec3 pPos = Vec3.atCenterOf(pos);
            level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.SF6_TRIP.get(), SoundSource.BLOCKS, 0.5f, 1f);
        }
    }

    public static class DataHolder {
        public boolean powered;
        public byte[] power = new byte[Direction.values().length];
    }
}
