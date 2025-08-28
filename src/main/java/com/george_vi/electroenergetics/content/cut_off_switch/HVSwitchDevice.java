package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulationResults;
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
        double airResistance = extraData.getDouble("AirResistance");

        if (state == SwitchState.ARCING)
            bridges.bridge(new Node(0, pos), new Node(0, instance.pos()), 500, 0, 0);
        else if (state == SwitchState.MOVING)
            bridges.bridge(new Node(0, pos), new Node(0, instance.pos()), airResistance < 1000 ? 1000 : airResistance, 0, 0);
        else if (state == SwitchState.CONNECTED)
            bridges.bridge(new Node(0, pos), new Node(0, instance.pos()), 0.01, 0, 0);

    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, CompoundTag extraData) {
        if (!extraData.contains("Target"))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) level);
        BlockPos targetPos = NBTHelper.readBlockPos(extraData, "Target");

        InfrastructureSavedData.SimulatedDeviceInstance instance = sd.getDevice(targetPos);
        if (instance == null ||
                instance.simulatedDevice() != CEESimulatedDevices.CONNECTOR ||
                !instance.extraData().getBoolean("HVSwitchTarget"))
            return;

        SwitchState state = SwitchState.values()[extraData.getInt("State")];
        float progress = extraData.getFloat("Progress");
        boolean connecting = extraData.getBoolean("Connected");
        double airResistance = extraData.getDouble("AirResistance");
        double v1 = results.getVoltageAt(pos, 0);
        double v2 = results.getVoltageAt(instance.pos(), 0);
        double current = results.getCurrentThrough(new Node(0, pos), new Node(0, instance.pos()));

        if (progress > 0.8)
            state = SwitchState.CONNECTED;
        else if (progress > 0.5 && connecting && state == SwitchState.DISCONNECTED) {
            state = SwitchState.MOVING;
        } else if (state == SwitchState.MOVING) {
            if (Math.abs((v1 - v2) * current) > 100)
                state = SwitchState.ARCING;
        } else if (state == SwitchState.ARCING) {
            if (Math.abs((v1 - v2) * current) < Mth.lerp(progress * progress, 700000, 2000))
                state = connecting ? SwitchState.MOVING : SwitchState.DISCONNECTED;
            else if (progress < 0.05 && level.random.nextFloat() > 0.95)
                state = SwitchState.DISCONNECTED;
        } else if (progress < 0.8 && !connecting) {
            if (Math.abs(current) > 0.001)
                state = SwitchState.ARCING;
            else
                state = SwitchState.DISCONNECTED;
        }

        if (state == SwitchState.MOVING)
            airResistance = Mth.lerp((progress - 0.5) / 0.3, 99999, 999);

        progress = Mth.clamp(progress + (connecting ? 0.01f : -0.01f), 0, 1);
        extraData.putFloat("Progress", progress);
        extraData.putDouble("AirResistance", airResistance);
        extraData.putInt("State", state.ordinal());

        if (level.isLoaded(pos)) {
            if (level.getBlockEntity(pos) instanceof HVSwitchBlockEntity be) {
                be.progress = progress;
                be.connected = extraData.getBoolean("Connected");
                boolean shouldArc = state == SwitchState.ARCING;
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
