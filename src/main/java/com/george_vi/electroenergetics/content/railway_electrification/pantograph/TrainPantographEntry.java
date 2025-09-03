package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import net.minecraft.core.BlockPos;

public record TrainPantographEntry(BlockPos originalPos, BlockPos rotatedPos, boolean active, boolean facingForward) {
}
