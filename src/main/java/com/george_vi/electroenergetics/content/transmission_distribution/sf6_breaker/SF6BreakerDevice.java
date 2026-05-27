package com.george_vi.electroenergetics.content.transmission_distribution.sf6_breaker;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.connector.InsulatorDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.devices.device.VirtualRedstoneDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SF6BreakerDevice extends SimpleElectricalDevice implements VirtualRedstoneDevice {

    public boolean powered;
    public boolean base;
    public SF6BreakerDevice otherDevice;
    public InsulatorDevice insulatorDevice;
    public InsulatorDevice insulatorDevice2;
    public byte[] power = new byte[Direction.values().length];
    public int cooldown = 0;
    public boolean closed = true;

    public static final ElectricalProperties CONNECTION = ElectricalProperties.resistor(0.01);

    public SF6BreakerDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        BlockPos otherPos = pos.above(this.base ? 1 : -1);
        if (this.otherDevice == null || !this.otherDevice.isValid()) {
            this.otherDevice = deviceSD.getDevice(otherPos, SF6BreakerDevice.class);
        }

        if (!this.base)
            return;

        if (cooldown > 0)
            cooldown--;
        else {
            BlockPos insulatorPos = pos.below();
            if (this.insulatorDevice == null || !this.insulatorDevice.isValid()) {
                this.insulatorDevice = deviceSD.getDevice(insulatorPos, InsulatorDevice.class);
            }

            if (this.insulatorDevice != null) {
                if (this.insulatorDevice2 == null || !this.insulatorDevice2.isValid()) {
                    this.insulatorDevice2 = deviceSD.getDevice(insulatorPos.below(), InsulatorDevice.class);
                }
            } else {
                this.insulatorDevice2 = null;
            }

            boolean prevClosed = closed;
            if (this.otherDevice != null && !this.powered && !this.otherDevice.powered)
                closed = (this.insulatorDevice == null || !this.insulatorDevice.powered) &&
                        (this.insulatorDevice2 == null || !this.insulatorDevice2.powered);
            else
                closed = false;
            if (prevClosed != closed) {
                cooldown = 20;
                Vec3 pPos = Vec3.atCenterOf(pos);
                level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.SF6_TRIP.get(), SoundSource.BLOCKS, 1, 1f);
            }
        }

        if (closed)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, otherPos), CONNECTION);
    }

    @Override
    public void read(CompoundTag tag) {
        this.powered = tag.getBoolean("Closed");
        this.base = tag.getBoolean("Base");
        byte[] arr = tag.getByteArray("Powered");
        if (arr.length == this.power.length)
            this.power = arr;
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putByteArray("Power", this.power);
        if (this.powered)
            tag.putBoolean("Powered", true);
        if (this.base)
            tag.putBoolean("Base", true);
    }


    @Override
    public void updateRedstoneInput(int power, Direction direction) {
        byte[] powerLevels = this.power;
        powerLevels[direction.ordinal()] = (byte) power;
        boolean p = false;
        for (byte b : powerLevels) {
            if (b > 0) {
                p = true;
                break;
            }
        }
        this.powered = p;
    }
}

