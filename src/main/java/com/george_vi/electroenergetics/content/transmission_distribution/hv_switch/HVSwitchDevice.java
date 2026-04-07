package com.george_vi.electroenergetics.content.transmission_distribution.hv_switch;

import com.george_vi.electroenergetics.content.connector.ConnectorDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDeviceType;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class HVSwitchDevice extends SimpleElectricalDevice {
    public SwitchState state;
    public double airResistance;
    public BlockPos target;
    public float progress;
    public boolean isConnecting;
    public HVSwitchBlockEntity be;
    public ConnectorDevice targetDevice;
    
    public HVSwitchDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (target == null)
            return;

        if (targetDevice == null || !targetDevice.isValid() || !targetDevice.pos.equals(target) || !targetDevice.isHVSwitchTarget) {
            targetDevice = deviceSD.getDevice(target, ConnectorDevice.class);
            if (targetDevice != null && !targetDevice.isHVSwitchTarget)
                targetDevice = null;
        }

        if (state == SwitchState.ARCING)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, target), 500, 0, 0);
        else if (state == SwitchState.MOVING)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, target), airResistance < 1000 ? 1000 : airResistance, 0, 0);
        else if (state == SwitchState.CONNECTED)
            bridges.bridge(new InWorldNode(0, pos), new InWorldNode(0, target), 0.01, 0, 0);

    }

    @Override
    public void postTick(SimulationResults results) {
        if (target == null)
            return;

        double voltage = Math.abs(results.getVoltageAt(new InWorldNode(0, pos), new InWorldNode(0, target)));
        double current = Math.abs(results.getCurrentThrough(new InWorldNode(0, pos), new InWorldNode(0, target)));

        if (progress > 0.9)
            state = SwitchState.CONNECTED;
        else if (state == SwitchState.MOVING && progress >= 0.6) {
            if (voltage > Mth.lerp((progress - 0.6) * 2.5, 50000, 1)) {
                state = SwitchState.ARCING;
                airResistance = Mth.lerp(progress, 200000, 100);
            }
        } else if (progress > 0.6 && isConnecting)
            state = SwitchState.MOVING;
        else if (state == SwitchState.CONNECTED && progress < 0.8 && !isConnecting) {
            if (current > 0.05) {
                state = SwitchState.ARCING;
                airResistance = Mth.lerp(progress, 200000, 100);
            } else
                state = SwitchState.DISCONNECTED;
        } else if (state == SwitchState.ARCING) {
            if (!isConnecting) {
                if (voltage < Mth.lerp(progress, 3000, 1) ||
                        (progress < 0.1 && level.random.nextFloat() > 0.98)) {
                    state = SwitchState.DISCONNECTED;
                }
            }
            airResistance = Mth.lerp(progress, 200000, 100);
        }

        if (state == SwitchState.MOVING)
            airResistance = Mth.lerp((progress - 0.5) / 0.3, 99999, 999);

        progress = Mth.clamp(progress + (isConnecting ? 0.01f : -0.01f), 0, 1);

        if (be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof HVSwitchBlockEntity be)
                this.be = be;

        if (be != null) {
            if (be.isRemoved())
                be = null;
            else {
                be.progress = progress;
                be.connected = isConnecting;
                boolean shouldArc = state == SwitchState.ARCING;
                if (be.arcing != shouldArc) {
                    be.arcing = shouldArc;
                    be.sendData();
                }
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {

        state = SwitchState.values()[tag.getInt("State")];
        target = NBTHelper.readBlockPos(tag, "Target");
        isConnecting = tag.getBoolean("Connected");
        progress = tag.getFloat("Progress");
        airResistance = tag.getDouble("AirResistance");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("State", state.ordinal());
        tag.putDouble("AirResistance", airResistance);
        tag.putFloat("Progress", progress);
        tag.put("Target", NbtUtils.writeBlockPos(target));
        tag.putBoolean("Connected", isConnecting);
    }

    enum SwitchState {
        DISCONNECTED,
        MOVING,
        CONNECTED,
        ARCING;
    }
}
