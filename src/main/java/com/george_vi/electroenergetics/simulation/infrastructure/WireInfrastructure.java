package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class WireInfrastructure {
    public final InfrastructureSavedData sd;
    public final Level level;
    public final Map<InWorldNodeConnection, ConnectionEntry> connections = new HashMap<>();
    public boolean rebuild = true;

    public WireInfrastructure(InfrastructureSavedData sd, Level level) {
        this.sd = sd;
        this.level = level;
    }

    void addConnection(InWorldNodeConnection connection, WireData wireData, boolean quiet) {
        if (wireData instanceof CatenaryConnectionData)
            throw new IllegalArgumentException("CatenaryConnectionData used as data for normal wire creation!");
        Vec3 pos1 = sd.getNodePosition(connection.node1());
        Vec3 pos2 = sd.getNodePosition(connection.node2());

        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(), 0.5f);

        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());
        AABB bb = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);
        ConnectionEntry connectionEntry = new ConnectionEntry(pos1, pos2, points, wireData, bb);
        connections.put(connection, connectionEntry);
        if (quiet)
            return;
        sd.wireCrossContactModule.onWireAdded(connection, connectionEntry);
    }

    ConnectionEntry removeConnection(InWorldNodeConnection connection) {
        ConnectionEntry v = connections.remove(connection);
        if (v != null)
            sd.wireCrossContactModule.onWireRemoved(connection, v);
        return v;
    }

    void addCatenaryConnection(InWorldNodeConnection connection, CatenaryConnectionData catenaryData, boolean quiet) {
        Vec3 pos1 = connection.node1().sourcePos().getBottomCenter();
        Vec3 pos2 = connection.node2().sourcePos().getBottomCenter();

        List<Vec3> points;
        if (catenaryData.isLow) {
             points = QuadraticWireHelper.cablePoints(pos1, pos2, 0, 0.5f);
        } else {
            List<Vec3> points1 = QuadraticWireHelper.cablePoints(pos1, pos2, 0, 0.5f);
            float distance = (float) pos1.distanceTo(pos2);
            List<Vec3> points2 = QuadraticWireHelper.cablePoints(pos1.add(0, 1.5, 0), pos2.add(0, 1.5, 0), 350f * (0.05f / distance), 0.5f);
            points = new ArrayList<>(points1.size() * 2);
            for (int i = 0; i < points1.size() * 2; i++) {
                points.add(i % 2 == 0 ? points1.get(i / 2) : points2.get((i - 1) / 2));
//                ((ServerLevel)level).sendParticles(ParticleTypes.SCRAPE, points.get(i).x, points.get(i).y, points.get(i).z, 3, 0, 0, 0, 0);
            }
        }


        AABB bb = new AABB(pos1, pos2).inflate(0.5);
        ConnectionEntry connectionEntry = new ConnectionEntry(pos1, pos2, points, catenaryData, bb);
        connections.put(connection, connectionEntry);
        if (quiet)
            return;
        sd.wireCrossContactModule.onWireAdded(connection, connectionEntry);
    }

    public ConnectionEntry getConnection(InWorldNodeConnection connection) {
        return connections.get(connection);
    }

    public void rebuild() {
        sd.wireCrossContactModule.onRebuild();
        rebuild = false;
    }
}
