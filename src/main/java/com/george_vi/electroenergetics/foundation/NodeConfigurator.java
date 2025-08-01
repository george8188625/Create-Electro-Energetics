package com.george_vi.electroenergetics.foundation;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeConfigurator {
    protected final List<Vec3> nodes;
    protected final Direction origin;

    public NodeConfigurator(List<Vec3> nodes, Direction origin) {
        this.nodes = List.copyOf(nodes);
        this.origin = origin;
    }

    public Vec3 getNodePos(Direction direction, int id) {
        List<Vec3> nodes = rotate(origin, direction);
        if (id >= nodes.size())
            return Vec3.ZERO;
        return nodes.get(id);
    }

    public Map<Vec3, Integer> getNodes(Direction direction) {
        Map<Vec3, Integer> result = new HashMap<>();
        List<Vec3> nodes = rotate(origin, direction);
        for (int i = 0; i < nodes.size(); i++)
            result.put(nodes.get(i), i);

        return result;
    }

    protected List<Vec3> rotate(Direction origin, Direction direction) {
        return getRotatedNodes(rotationValues(origin).reverse().add(rotationValues(direction)));
    }

    protected List<Vec3> getRotatedNodes(Vec3 vec) {
        List<Vec3> result = new ArrayList<>();
        for (Vec3 node : nodes)
            result.add(VecHelper.rotate(node.subtract(VecHelper.CENTER_OF_ORIGIN), vec.x, vec.y, vec.z).add(VecHelper.CENTER_OF_ORIGIN));

        return result;
    }


    public static class Builder {
        List<Vec3> nodes = new ArrayList<>();

        public Builder add(Vec3 pos) {
            nodes.add(pos);
            return this;
        }

        public NodeConfigurator simple() {
            return simple(Direction.UP);
        }

        public NodeConfigurator simple(Direction origin) {
            return new NodeConfigurator(nodes, origin);
        }
    }

    protected static float horizontalAngleFromDirection(Direction direction) {
        return (float) ((Math.max(direction.get2DDataValue(), 0) & 3) * 90);
    }

    protected static Vec3 rotationValues(Direction direction) {
        return new Vec3(direction == Direction.UP ? 0 : (Direction.Plane.VERTICAL.test(direction) ? 180 : 90),
                -horizontalAngleFromDirection(direction), 0);
    }
}
