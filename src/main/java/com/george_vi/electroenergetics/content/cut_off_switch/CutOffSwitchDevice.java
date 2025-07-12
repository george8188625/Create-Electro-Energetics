package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CutOffSwitchDevice extends SimulatedDevice {
    final int lines;
    public CutOffSwitchDevice(ResourceLocation id, int lines) {
        super(id);
        this.lines = lines;
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {
        if (extraData.getBoolean("Closed")) {
            for (int i = 0; i < lines; i++)
                bridges.builder(pos).resistor(i, (lines) + i, 0.001);
        }
    }
}
