package com.george_vi.electroenergetics.content.transmission_distribution.hv_switch;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.connector.ConnectorDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class HVSwitchDevice extends SimulatedDevice<HVSwitchDevice.DataHolder> {
    public HVSwitchDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        if (extraData.target == null)
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);

        SimulatedDeviceInstance<?> instance = sd.getDevice(extraData.target);
        if (instance == null ||
                instance.simulatedDevice() != CEESimulatedDevices.CONNECTOR ||
                !((ConnectorDevice.DataHolder)instance.extraData()).isHVSwitchTarget)
            return;
        double airResistance = extraData.airResistance;

        if (extraData.state == SwitchState.ARCING)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, instance.pos()), 500, 0, 0);
        else if (extraData.state == SwitchState.MOVING)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, instance.pos()), airResistance < 1000 ? 1000 : airResistance, 0, 0);
        else if (extraData.state == SwitchState.CONNECTED)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, instance.pos()), 0.01, 0, 0);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        if (extraData.target == null)
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);

        SimulatedDeviceInstance<?> instance = sd.getDevice(extraData.target);
        if (instance == null ||
                instance.simulatedDevice() != CEESimulatedDevices.CONNECTOR ||
                !((ConnectorDevice.DataHolder)instance.extraData()).isHVSwitchTarget)
            return;

        double voltage = Math.abs(results.getVoltageAt(new InWorldNode(0, pos), new InWorldNode(0, instance.pos())));
        double current = Math.abs(results.getCurrentThrough(new InWorldNode(0, pos), new InWorldNode(0, instance.pos())));

        if (extraData.progress > 0.9)
            extraData.state = SwitchState.CONNECTED;
        else if (extraData.state == SwitchState.MOVING && extraData.progress >= 0.6) {
            if (voltage > Mth.lerp((extraData.progress - 0.6) * 2.5, 50000, 1)) {
                extraData.state = SwitchState.ARCING;
                extraData.airResistance = Mth.lerp(extraData.progress, 200000, 100);
            }
        } else if (extraData.progress > 0.6 && extraData.isConnecting)
            extraData.state = SwitchState.MOVING;
        else if (extraData.state == SwitchState.CONNECTED && extraData.progress < 0.8 && !extraData.isConnecting) {
            if (current > 0.05) {
                extraData.state = SwitchState.ARCING;
                extraData.airResistance = Mth.lerp(extraData.progress, 200000, 100);
            } else
                extraData.state = SwitchState.DISCONNECTED;
        } else if (extraData.state == SwitchState.ARCING) {
            if (!extraData.isConnecting) {
                if (voltage < Mth.lerp(extraData.progress, 3000, 1) ||
                        (extraData.progress < 0.1 && level.random.nextFloat() > 0.98)) {
                    extraData.state = SwitchState.DISCONNECTED;
                }
            }
            extraData.airResistance = Mth.lerp(extraData.progress, 200000, 100);
        }

        if (extraData.state == SwitchState.MOVING)
            extraData.airResistance = Mth.lerp((extraData.progress - 0.5) / 0.3, 99999, 999);

        extraData.progress = Mth.clamp(extraData.progress + (extraData.isConnecting ? 0.01f : -0.01f), 0, 1);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof HVSwitchBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.progress = extraData.progress;
                extraData.be.connected = extraData.isConnecting;
                boolean shouldArc = extraData.state == SwitchState.ARCING;
                if (extraData.be.arcing != shouldArc) {
                    extraData.be.arcing = shouldArc;
                    extraData.be.sendData();
                }
            }
        }
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.state = SwitchState.values()[tag.getInt("State")];
        dataHolder.target = NBTHelper.readBlockPos(tag, "Target");
        dataHolder.isConnecting = tag.getBoolean("Connected");
        dataHolder.progress = tag.getFloat("Progress");
        dataHolder.airResistance = tag.getDouble("AirResistance");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("State", extraData.state.ordinal());
        tag.putDouble("AirResistance", extraData.airResistance);
        tag.putFloat("Progress", extraData.progress);
        tag.put("Target", NbtUtils.writeBlockPos(extraData.target));
        tag.putBoolean("Connected", extraData.isConnecting);
        return tag;
    }

    public static class DataHolder {
        public SwitchState state;
        public double airResistance;
        public BlockPos target;
        public float progress;
        public boolean isConnecting;
        public HVSwitchBlockEntity be;
    }

    enum SwitchState {
        DISCONNECTED,
        MOVING,
        CONNECTED,
        ARCING;
    }
}
