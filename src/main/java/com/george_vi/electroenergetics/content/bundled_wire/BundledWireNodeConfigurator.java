package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class BundledWireNodeConfigurator {
    public static final NodeConfigurator baseConfigurator = new NodeConfigurator.Builder()
            .add(0x1000, 8, 2, 12)
            .add(0x1010, 12, 2, 8)
            .add(0x1020, 8, 2, 4)
            .add(0x1030, 4, 2, 8)
            .add(0x1001, 10.25f, 2, 9)
            .add(0x1002, 5.755f, 2, 9)
            .add(0x1011, 9, 2, 10.25f)
            .add(0x1012, 9, 2, 5.755f)
            .add(0x1021, 10.25f, 2, 7)
            .add(0x1022, 5.755f, 2, 7)
            .add(0x1031, 7, 2, 10.25f)
            .add(0x1032, 7, 2, 5.755f)
            .simple();

    public static Map<Integer, Vec3> getNodesFor(Direction direction, boolean roll, boolean flip) {
        Int2ObjectMap<Vec3> out = new Int2ObjectOpenHashMap<>();
        int side = 0;
        if (flip) side = 2;
        if (roll) side++;
        for (Map.Entry<Integer, Vec3> e : baseConfigurator.getNodes(direction).entrySet()) {
            int id = e.getKey();
            Vec3 pos = e.getValue();
            int nodeSide = (id & 0xf0) >> 4;

            if (side == nodeSide) {
                out.put(id, pos);
            }
        }
        return out;
    }

    public static Vec3 getPos(Direction direction, int id) {
        return baseConfigurator.getNodePos(direction, id);
    }

    public static boolean isAccessible(int id) {
        return (id & 15) != 0;
    }
}
