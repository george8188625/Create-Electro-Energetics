package com.george_vi.electroenergetics.foundation;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class QuadraticWireHelper {
    public static Vec3 posAt(Vec3 pos1, Vec3 pos2, float point) {
        return posAt(pos1, pos2, point, 1);
    }

    public static Vec3 posAt(Vec3 pos1, Vec3 pos2, float point, float dip) {
        float distance = (float) pos1.distanceTo(pos2);
        float resolution = (2 * distance);
        float x = point * resolution;

        float a = (0.05f / distance) * dip;
        float yOffset = a * x * (x - resolution);

        Vec3 linear = pos1.add(pos2.subtract(pos1).multiply(point, point, point));

        return linear.add(0, yOffset, 0);
    }

    public static List<Vec3> cablePoints(Vec3 pos1, Vec3 pos2, float dip) {
        return cablePoints(pos1, pos2, dip, 1);
    }

    public static List<Vec3> cablePoints(Vec3 pos1, Vec3 pos2, float dip, Vec3 position) {
        float distance = (float) pos1.distanceTo(pos2);
        float wireLength = (float) pos1.distanceTo(pos2);

        double resolution = (distance * 2);
        float a = (0.05f / distance) * dip;

        float shortestDistance = 9999;
        for (int x = 0; x < resolution; x++) {
            float particleLevel = (float) (a * x * (x - resolution));
            Vec3 point = pos1.add((pos2.subtract(pos1)).multiply(1 / resolution, 1 / resolution, 1 / resolution).multiply(x, x, x)).add(0, particleLevel, 0);
            shortestDistance = (float) Math.min(shortestDistance, position.distanceTo(point));
        }
        float detail;
        if (shortestDistance < 10 || wireLength < 10)
            detail = 1;
        else if (shortestDistance < 20 || wireLength < 20)
            detail = 2;
        else if (shortestDistance < 40 || wireLength < 30)
            detail = 10;
        else
            detail = 20;

        return cablePoints(pos1, pos2, dip, detail);
    }

    public static List<Vec3> cablePoints(Vec3 pos1, Vec3 pos2, float dip, float detail) {
        List<Vec3> points = new ArrayList<>();
        float distance = (float) pos1.distanceTo(pos2);

        double resolution = (distance * 2) / detail;
        float a = (0.05f / distance) * dip * detail * detail;
        for (int x = 0; x < resolution; x++) {
            float particleLevel = (float) (a * x * (x - resolution));
            Vec3 point = pos1.add((pos2.subtract(pos1)).multiply(1 / resolution, 1 / resolution, 1 / resolution).multiply(x, x, x)).add(0, particleLevel, 0);
            points.add(point);
        }
        return points;
    }

    public static float pointElevationInDegrees(Vec3 pos1, Vec3 pos2, float point, float sag) {
        Vec3 pointAt1 = posAt(pos1, pos2, point, sag);
        Vec3 pointAt2 = posAt(pos1, pos2, point + 0.001f, sag);
        Vec3 directionVector = pointAt1.subtract(pointAt2);

        double dx = directionVector.x;
        double dy = directionVector.y;
        double dz = directionVector.z;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        return (float) Math.toDegrees(Math.atan2(dy, horizontalDistance));
    }

    public static float pointElevationInDegrees(Vec3 pos1, Vec3 pos2, float point) {
       return pointElevationInDegrees(pos1, pos2, point, 1);
    }
}
