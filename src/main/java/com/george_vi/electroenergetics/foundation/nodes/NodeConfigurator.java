package com.george_vi.electroenergetics.foundation.nodes;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class NodeConfigurator {
    protected final Int2ObjectMap<Vec3> nodes;
    protected final Direction origin;

    public NodeConfigurator(Int2ObjectMap<Vec3> nodes, Direction origin) {
        this.nodes = new Int2ObjectArrayMap<>(nodes);
        this.origin = origin;
    }

    public NodeConfigurator rotate(Vec3 vec) {
        return new NodeConfigurator(getRotatedNodes(vec), origin);
    }

    public NodeConfigurator scale(double x, double y, double z) {
        return new NodeConfigurator(getScaledNodes(x, y, z), origin);
    }

    public Vec3 getNodePos(Direction direction, int id) {
        Int2ObjectMap<Vec3> nodes = rotate(origin, direction);
        return nodes.get(id);
    }

    public Map<Integer, Vec3> getNodes(Direction direction) {
        return rotate(origin, direction);
    }

    protected Int2ObjectMap<Vec3> rotate(Direction origin, Direction direction) {
        return getRotatedNodes(rotationValues(origin).reverse().add(rotationValues(direction)));
    }

    protected Int2ObjectMap<Vec3> getRotatedNodes(Vec3 vec) {
        Int2ObjectMap<Vec3> result = new Int2ObjectArrayMap<>();
        nodes.forEach((id, node) ->
                result.put(id.intValue(),
                        VecHelper.rotate(node.subtract(VecHelper.CENTER_OF_ORIGIN), vec.x, vec.y, vec.z)
                                .add(VecHelper.CENTER_OF_ORIGIN)));

        return result;
    }

    protected Int2ObjectMap<Vec3> getScaledNodes(double x, double y, double z) {
        Int2ObjectMap<Vec3> result = new Int2ObjectArrayMap<>();
        nodes.forEach((id, node) ->
                result.put(id.intValue(), node
                        .subtract(VecHelper.CENTER_OF_ORIGIN)
                        .multiply(x, y, z)
                        .add(VecHelper.CENTER_OF_ORIGIN)));

        return result;
    }


    public static class Builder {
        Int2ObjectMap<Vec3> nodes = new Int2ObjectArrayMap<>();
        int id = 0;

        public Builder add(Vec3 pos) {
            nodes.put(id++, pos);
            return this;
        }

        public Builder add(float x, float y, float z) {
            nodes.put(id++, new Vec3(x / 16f, y / 16f, z / 16f));
            return this;
        }

        public Builder skip(int ids) {
            id += ids;
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
