package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeHelper;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * This class hold the data for {@link InWorldNode} nodes inside the world's infrastructure data structures.
 * <br>
 * On the logical server, each node that actually exists must have one of these objects.
 * <br>
 * @see InWorldNode
 */
@SuppressWarnings("unused")
public class InWorldNodeData {
    public static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);

    public final int id;
    public final InWorldNode node;

    private boolean valid = true;
    public final Int2ObjectArrayMap<WireData> adjacency = new Int2ObjectArrayMap<>(4);
    Vec3 localPos;
    Vec3 globalPos;
    boolean isDynamic;
    public String label = null;
    public DetachedNodeType detachedNodeType = null;
    public UUID detachedNodeEntityId;

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

    /**
     * If other than -1, it's a detached node.
     * @see com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeHelper
     */
    public int getDetachedNodeId() {
        if (DetachedNodeHelper.isDetached(node))
            return node.id();
        return -1;
    }
}
