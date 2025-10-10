package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CreativeBatteryDevice extends SimulatedDevice {
    public CreativeBatteryDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        double voltage = extraData.contains("Voltage") ? extraData.getDouble("Voltage") : 12;

        // AC experiment
        // voltage = Math.sin(AnimationTickHolder.getTicks() / 5d) * voltage;

        bridges.builder(pos)
                .idealVoltageSource(0, 1, voltage);
    }
}
