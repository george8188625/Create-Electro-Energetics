package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class HVSwitchDevice extends SimulatedDevice {
    public HVSwitchDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (!extraData.contains("Target"))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
        SwitchState state = SwitchState.values()[extraData.getInt("State")];
        BlockPos targetPos = NBTHelper.readBlockPos(extraData, "Target");

        InfrastructureSavedData.SimulatedDeviceInstance instance = sd.getDevice(targetPos);
        if (instance == null ||
            instance.simulatedDevice() != CEESimulatedDevices.CONNECTOR ||
            !instance.extraData().getBoolean("HVSwitchTarget"))
            return;

        if (state == SwitchState.ARCING)
            bridges.bridge(new Node(0, pos), new Node(0, instance.pos()), 500, 0);
        else if (state == SwitchState.MOVING)
            bridges.bridge(new Node(0, pos), new Node(0, instance.pos()), 999999999, 0);
        else if (state == SwitchState.CONNECTED)
            bridges.bridge(new Node(0, pos), new Node(0, instance.pos()), 0.01, 0);

    }

    @Override
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {
        if (!extraData.contains("Target"))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
        SwitchState state = SwitchState.values()[extraData.getInt("State")];
        BlockPos targetPos = NBTHelper.readBlockPos(extraData, "Target");

        InfrastructureSavedData.SimulatedDeviceInstance instance = sd.getDevice(targetPos);
        if (instance == null ||
                instance.simulatedDevice() != CEESimulatedDevices.CONNECTOR ||
                !instance.extraData().getBoolean("HVSwitchTarget"))
            return;

        float progress = extraData.getFloat("Progress");
        double v1 = voltages.get(new Node(0, pos));
        double v2 = sd.getVoltageAt(new Node(0, instance.pos()));

        if ((progress > 0.57 && extraData.getBoolean("Connected")) || (progress > 0.8 && !extraData.getBoolean("Connected")))
            state = SwitchState.CONNECTED;
        else if (progress < 0.05 && (state != SwitchState.ARCING || level.random.nextFloat() > 0.90))
            state = SwitchState.DISCONNECTED;
        else if (state != SwitchState.ARCING && (state != (extraData.getBoolean("Connected") ? SwitchState.DISCONNECTED : SwitchState.MOVING)))
            state = extraData.getBoolean("Connected") ? SwitchState.DISCONNECTED : SwitchState.MOVING;
        else if (state == SwitchState.MOVING) {
            if (Math.abs(v1 - v2) > Mth.lerp(progress, 500000, 1000))
                state = SwitchState.ARCING;
            else
                state = SwitchState.DISCONNECTED;
        } else if (state == SwitchState.ARCING) {
            if (Math.abs(v1 - v2) < Mth.lerp(progress, 200, 50))
                state = SwitchState.DISCONNECTED;
        }

        extraData.putInt("State", state.ordinal());

        progress = Mth.clamp(progress + (extraData.getBoolean("Connected") ? 0.01f : -0.01f), 0, 1);
        extraData.putFloat("Progress", progress);
        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof HVSwitchBlockEntity be) {
                be.progress = progress;
                be.connected = extraData.getBoolean("Connected");
                boolean shouldArc = state == SwitchState.ARCING || (progress > 0.60 && progress < 0.95 && Math.abs(v1 - v2) > 0.0001 && extraData.getBoolean("Connected"));
                if (be.arcing != shouldArc) {
                    be.arcing = shouldArc;
                    be.sendData();
                }

            }
        }

    }

    enum SwitchState {
        DISCONNECTED,
        MOVING,
        CONNECTED,
        ARCING;
    }
}
