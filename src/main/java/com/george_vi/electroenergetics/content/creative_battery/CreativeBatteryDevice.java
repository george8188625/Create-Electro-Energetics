package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class CreativeBatteryDevice extends SimulatedDevice {
    public CreativeBatteryDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double voltage = extraData.contains("Voltage") ? extraData.getDouble("Voltage") : 2.4;

        // AC experiment
        // voltage = Math.sin(AnimationTickHolder.getTicks() / 5d) * voltage;

        bridges.builder(pos)
                .idealVoltageSource(0, 1, voltage);
    }
}
