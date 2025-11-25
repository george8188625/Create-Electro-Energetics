package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.List;

public record SimulatedDeviceInstance<T>(SimulatedDevice<T> simulatedDevice, BlockPos pos, T extraData,
                                         List<InWorldNode> nodes) {
    public CompoundTag write() {
        return simulatedDevice.write(extraData);
    }
}
