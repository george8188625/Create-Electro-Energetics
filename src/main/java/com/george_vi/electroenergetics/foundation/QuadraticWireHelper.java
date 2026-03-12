package com.george_vi.electroenergetics.foundation;

import net.minecraft.util.Mth;
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

        Vec3 linear = pos1.add((pos2.x - pos1.x) * point,
                               (pos2.y - pos1.y) * point,
                               (pos2.z - pos1.z) * point);
        return linear.add(0, yOffset, 0);
    }

    public static List<Vec3> cablePoints(Vec3 pos1, Vec3 pos2, float dip) {
        return cablePoints(pos1, pos2, dip, 1);
    }

    public static List<Vec3> cablePoints(Vec3 pos1, Vec3 pos2, float dip, Vec3 position) {
        float distance = (float) pos1.distanceTo(pos2);
        float wireLength = (float) pos1.distanceTo(pos2);

        double resolution = (distance * 2);
        double invResolution = 1 / resolution;
        float a = (0.05f / distance) * dip;

        float shortestDistanceSqr = 9999;
        for (int x = 0; x < resolution; x++) {
            float particleLevel = (float) (a * x * (x - resolution));
            double pX = (pos2.x - pos1.x) * (invResolution) * x + pos1.x;
            double pY = (pos2.y - pos1.y) * (invResolution) * x + pos1.y + particleLevel;
            double pZ = (pos2.z - pos1.z) * (invResolution) * x + pos1.z;
            shortestDistanceSqr = (float) Math.min(shortestDistanceSqr, position.distanceToSqr(pX, pY, pZ));
        }
        float detail;
        if (shortestDistanceSqr < 100 || wireLength < 10)
            detail = 1;
        else if (shortestDistanceSqr < 400 || wireLength < 20)
            detail = 2;
        else if (shortestDistanceSqr < 1600 || wireLength < 30)
            detail = 10;
        else
            detail = 20;

        return cablePoints(pos1, pos2, dip, detail);
    }

    public static List<Vec3> cablePoints(Vec3 pos1, Vec3 pos2, float dip, float detail) {
        float distance = (float) pos1.distanceTo(pos2);

        double resolution = Math.ceil((distance * 2) / detail);
        double invResolution = 1 / resolution;
        List<Vec3> points = new ArrayList<>(Mth.ceil(resolution));
        float a = (0.05f / distance) * dip * detail * detail;
        for (int x = 0; x < resolution; x++) {
            float particleLevel = (float) (a * x * (x - resolution));
            double pX = (pos2.x - pos1.x) * (invResolution) * x + pos1.x;
            double pY = (pos2.y - pos1.y) * (invResolution) * x + pos1.y + particleLevel;
            double pZ = (pos2.z - pos1.z) * (invResolution) * x + pos1.z;
            Vec3 point = new Vec3(pX, pY, pZ);
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
        return (float) Mth.atan2(dy, horizontalDistance) * Mth.RAD_TO_DEG;
    }

    public static float pointElevationInDegrees(Vec3 pos1, Vec3 pos2, float point) {
       return pointElevationInDegrees(pos1, pos2, point, 1);
    }
}
