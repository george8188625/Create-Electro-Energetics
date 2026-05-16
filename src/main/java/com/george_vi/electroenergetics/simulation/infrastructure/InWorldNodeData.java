package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class InWorldNodeData {
    public static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);

    public final int id;
    public final InWorldNode node;

    private boolean valid = true;
    Int2ObjectArrayMap<WireData> adjacency = new Int2ObjectArrayMap<>(4);
    Vec3 localPos;
    Vec3 globalPos;
    boolean isDynamic;
    public String label = null;

    // for WireSync
    public long lastChunk;

    public InWorldNodeData(int id, InWorldNode node) {
        this.id = id;
        this.node = node;
    }

    void invalidate() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public Vec3 getLocalPos() {
        return localPos == null ? CENTER : localPos;
    }

    public Vec3 getGlobalPos() {
        return globalPos == null ? node.sourcePos().getCenter() : globalPos;
    }

}
