package com.george_vi.electroenergetics.devices.device;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record SimulatedDeviceType<T extends SimulatedDevice>(ResourceLocation id, SimulatedDeviceFactory<T> factory,
                                                             Collection<Block> validBlocks) {

    public static final Map<Block, SimulatedDeviceType<?>> BY_BLOCK = new HashMap<>();

    /**
     * @param validBlocks use if you want to add devices to blocks, that don't extend {@link DeviceBlock}
     */
    public SimulatedDeviceType(ResourceLocation id, SimulatedDeviceFactory<T> factory, Collection<Block> validBlocks) {
        this.id = id;
        this.factory = factory;
        this.validBlocks = validBlocks;
    }

    public SimulatedDeviceType(ResourceLocation id, SimulatedDeviceFactory<T> factory) {
        this(id, factory, Collections.emptyList());
    }


    public T create(ServerLevel level, BlockPos pos, DevicesSavedData sd) {
        return factory.create(this, level, pos, sd);
    }
}
