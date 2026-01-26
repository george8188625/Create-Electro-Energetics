package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import net.minecraft.world.level.block.state.BlockState;

public interface IPantographBlock {
    PantographType getPantographType(BlockState state);
}
