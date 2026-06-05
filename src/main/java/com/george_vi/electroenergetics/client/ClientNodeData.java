package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Purely a client-side mutable object for node data
 */
public class ClientNodeData {
    public String label;
    public InWorldNode node;
    public Vec3 targetPosition;
    public Vec3 position;
    public Vec3 lastPosition;

    public ClientNodeData(InWorldNode node) {
        this.node = node;
    }

    public @Nullable Vec3 getPos(float partialTicks) {
        if (lastPosition == null || position == null)
            return null;
        return VecHelper.lerp(partialTicks, lastPosition, position);
    }

    public void tick() {
        lastPosition = position;
        if (position == null || targetPosition == null)
            position = targetPosition;
        else
            position = VecHelper.lerp(1f, position, targetPosition);
    }
}
