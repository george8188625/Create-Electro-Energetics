package com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes;

import com.george_vi.electroenergetics.CEEEntityTypes;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Detached nodes, are nodes, which are {@link InWorldNode} but aren't attached to the grid.
 * <br>
 * These nodes can hold wires, act as any {@link InWorldNode}, but aren't fixed to a position.
 * They are positioned at pos {@code HORIZONTAL_POSITION}, so they can't be accessed normally by the player.
 * The position is handled separately. This class helps manage detached nodes.
 * <br>
 * Not to be confused with {@link com.george_vi.electroenergetics.foundation.nodes.AttachedNode}
 */
public class DetachedNodeHelper {
    private static final int HORIZONTAL_POSITION = 31000000;
    private static final BlockPos WORLD_POSITION = new BlockPos(HORIZONTAL_POSITION, 0, HORIZONTAL_POSITION);

    public static InWorldNode getFromId(int id) {
        return new InWorldNode(id, WORLD_POSITION);
    }

    public static boolean isDetached(InWorldNode node) {
        return node.sourcePos().equals(WORLD_POSITION);
    }

    public static boolean isDetached(BlockPos pos) {
        return pos.equals(WORLD_POSITION);
    }

    public static Vec3 tickPhysicsNode(InWorldNodeData nodeData, InfrastructureSavedData sd) {
        Vec3 pos = nodeData.getGlobalPos();
        UUID id = nodeData.detachedNodeEntityId;
        if (id == null)
            return pos;
        if (nodeData.detachedNodeType == DetachedNodeType.PHYSICS_UNINITIALIZED && !nodeData.adjacency.isEmpty()) {
            nodeData.detachedNodeType = DetachedNodeType.PHYSICS;
        } else if (nodeData.detachedNodeType == DetachedNodeType.PHYSICS && nodeData.adjacency.isEmpty()) {
            sd.removeNodeDefer(nodeData);
            return pos;
        }

        Entity e = sd.level.getEntity(id);
        if (!(e instanceof DetachedNodeEntity entity))
            return pos;
        return entity.position().add(0, 0.125, 0);
    }

    public static void createDetachedNodeEntity(InWorldNodeData nodeData, Vec3 pos, Level level) {
        DetachedNodeEntity entity = new DetachedNodeEntity(CEEEntityTypes.DETACHED_NODE.get(), level);
        entity.detachedNodeID = nodeData.node.id();
        entity.setPos(pos);
        level.addFreshEntity(entity);
        nodeData.detachedNodeEntityId = entity.getUUID();
    }
}
