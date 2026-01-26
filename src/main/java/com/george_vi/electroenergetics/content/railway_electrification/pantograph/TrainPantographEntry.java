package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class TrainPantographEntry {
    private static int globalIDs = 0;
    public final BlockPos originalPos;
    public final PantographType type;
    public final BlockPos rotatedPos;
    public boolean active;
    public boolean facingForward;
    public AttachedNode node;
    public Vec3 pos = null;
    public Vec3 prevPos = null;
    public double lastCurrent;

    public TrainPantographEntry(BlockPos originalPos, BlockPos rotatedPos, PantographType type, boolean active, boolean facingForward) {
        this.originalPos = originalPos;
        this.rotatedPos = rotatedPos;
        this.type = type;
        this.active = active;
        this.facingForward = facingForward;
        node = new AttachedNode(globalIDs++, "CEEPantographNode");
    }
}
