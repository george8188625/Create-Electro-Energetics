package com.george_vi.electroenergetics.content.transmission_distribution.sf6_breaker;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.connector.InsulatorDevice;
import com.george_vi.electroenergetics.foundation.VirtualRedstoneDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SF6BreakerDevice extends SimulatedDevice<SF6BreakerDevice.DataHolder> implements VirtualRedstoneDevice<SF6BreakerDevice.DataHolder> {

    public static final ElectricalProperties CONNECTION = ElectricalProperties.resistor(0.01);

    public SF6BreakerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        BlockPos otherPos = pos.above(extraData.base ? 1 : -1);
        if (extraData.otherDevice == null || !extraData.otherDevice.isValid()) {
            extraData.otherDevice = bridges.getSD().getDevice(otherPos, DataHolder.class);
        }

        if (!extraData.base)
            return;

        BlockPos insulatorPos = pos.below();
        if (extraData.insulatorDevice == null || !extraData.insulatorDevice.isValid()) {
            extraData.insulatorDevice = bridges.getSD().getDevice(insulatorPos, InsulatorDevice.DataHolder.class);
        }

        if (extraData.insulatorDevice != null) {
            if (extraData.insulatorDevice2 == null || !extraData.insulatorDevice2.isValid()) {
                extraData.insulatorDevice2 = bridges.getSD().getDevice(insulatorPos, InsulatorDevice.DataHolder.class);
            }
        } else {
            extraData.insulatorDevice2 = null;
        }

        if (extraData.otherDevice != null && !extraData.powered && !extraData.otherDevice.extraData().powered) {
            if (extraData.insulatorDevice != null && extraData.insulatorDevice.extraData().powered)
                return;
            if (extraData.insulatorDevice2 != null && extraData.insulatorDevice2.extraData().powered)
                return;
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, otherPos), CONNECTION);
        }
    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        super.postTick(pos, level, results, extraData);
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.powered = tag.getBoolean("Closed");
        dataHolder.base = tag.getBoolean("Base");
        byte[] arr = tag.getByteArray("Powered");
        if (arr.length == dataHolder.power.length)
            dataHolder.power = arr;
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("Power", extraData.power);
        if (extraData.powered)
            tag.putBoolean("Powered", true);
        if (extraData.base)
            tag.putBoolean("Base", true);
        return tag;
    }

    @Override
    public void updateRedstoneInput(Level level, BlockPos pos, Direction direction, DataHolder extraData, int power) {
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
        public boolean base;
        public SimulatedDeviceInstance<DataHolder> otherDevice;
        public SimulatedDeviceInstance<InsulatorDevice.DataHolder> insulatorDevice;
        public SimulatedDeviceInstance<InsulatorDevice.DataHolder> insulatorDevice2;
        public byte[] power = new byte[Direction.values().length];
    }
}
