package com.george_vi.electroenergetics.content;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeConfigurator {
    protected final List<Vec3> nodes;

    public NodeConfigurator(List<Vec3> nodes) {
        this.nodes = List.copyOf(nodes);
    }

    public Vec3 getNodePos(Direction direction, int id) {
        List<Vec3> nodes = getRotatedNodes(direction == Direction.UP ? 0 : (Direction.Plane.VERTICAL.test(direction) ? 180 : 90),
                (float) ((Math.max(direction.get2DDataValue(), 0) & 3) * -90),
                0);
        return nodes.get(id) == null ? Vec3.ZERO : nodes.get(id);
    }

    public Map<Vec3, Integer> getNodes(Direction direction) {
        Map<Vec3, Integer> result = new HashMap<>();
        List<Vec3> nodes = getRotatedNodes(direction == Direction.UP ? 0 : (Direction.Plane.VERTICAL.test(direction) ? 180 : 90),
                (float) ((Math.max(direction.get2DDataValue(), 0) & 3) * -90),
                0);
        for (int i = 0; i < nodes.size(); i++)
            result.put(nodes.get(i), i);

        return result;
    }

    protected List<Vec3> getRotatedNodes(float x, float y, float z) {
        // idk cool effect for fun do not enable
//        x += AnimationTickHolder.getRenderTime();
//        y += AnimationTickHolder.getRenderTime();
//        z += AnimationTickHolder.getRenderTime();
        List<Vec3> result = new ArrayList<>();
        for (Vec3 node : nodes)
            result.add(VecHelper.rotate(node.subtract(VecHelper.CENTER_OF_ORIGIN), x, y, z).add(VecHelper.CENTER_OF_ORIGIN));

        return result;
    }


    public static class Builder {
        List<Vec3> nodes = new ArrayList<>();

        public Builder add(Vec3 pos) {
            nodes.add(pos);
            return this;
        }

        public NodeConfigurator simple() {
            return new NodeConfigurator(nodes);
        }
    }
}
