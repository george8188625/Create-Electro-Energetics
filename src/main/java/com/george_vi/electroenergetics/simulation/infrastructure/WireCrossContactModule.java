package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.createmod.catnip.data.Couple;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireCrossContactModule {

    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireInfrastructure wireInfrastructure;
    public Map<Couple<AttachedNode>, CrossContactEntry> crossContacts = new HashMap<>();

    public WireCrossContactModule(InfrastructureSavedData sd, ServerLevel level, WireInfrastructure wireInfrastructure) {
        this.sd = sd;
        this.level = level;
        this.wireInfrastructure = wireInfrastructure;
    }

    public void onRebuild() {
        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireInfrastructure.connections.entrySet()) {
            computeCrossContactFor(e.getKey(), e.getValue(), true);
        }
    }

    public void onWireRemoved(InWorldNodeConnection connection, ConnectionEntry connectionData) {
        CrossContactEntry entry = null;
        AttachedNode node1 = null;
        AttachedNode node2 = null;
        InWorldNodeConnection connection2 = null;
        for (Map.Entry<Couple<AttachedNode>, CrossContactEntry> e : crossContacts.entrySet()) {
            CrossContactEntry contactEntry = e.getValue();
            if (contactEntry.wire1.equals(connection)) {
                entry = contactEntry;
                node1 = e.getKey().getFirst();
                node2 = e.getKey().getSecond();
                connection2 = contactEntry.wire2;
                break;
            }
            if (contactEntry.wire2.equals(connection)) {
                entry = contactEntry;
                node2 = e.getKey().getFirst();
                node1 = e.getKey().getSecond();
                connection2 = contactEntry.wire1;
                break;
            }
        }

        if (entry == null)
            return;

        crossContacts.remove(Couple.create(node1, node2));
        crossContacts.remove(Couple.create(node2, node1));

        ConnectionEntry connectionData2 = wireInfrastructure.getConnection(connection2);
        if (connectionData2 == null)
            return;

        connectionData.removeCut(node1);
        connectionData2.removeCut(node2);
    }

    public void onWireAdded(InWorldNodeConnection connection, ConnectionEntry connectionData) {
        computeCrossContactFor(connection, connectionData, false);
    }

    private void computeCrossContactFor(InWorldNodeConnection connection, ConnectionEntry connectionData, boolean trig) {
        AABB bb = connectionData.bb;
        List<Vec3> points = connectionData.points;
        WireData wireData = connectionData.wireData;
        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireInfrastructure.connections.entrySet()) {
            InWorldNodeConnection connection2 = e.getKey();
            ConnectionEntry connectionData2 = e.getValue();
            if (connectionData == connectionData2)
                continue;

            if (trig && connection.compareTo(connection2) < 0)
                continue;

            AABB bb2 = connectionData2.bb;
            if (!(bb.minX <= bb2.maxX && bb.maxX >= bb2.minX &&
                    bb.minY <= bb2.maxY && bb.maxY >= bb2.minY &&
                    bb.minZ <= bb2.maxZ && bb.maxZ >= bb2.minZ))
                continue;

            if (sd.getConnections(connection.node1()).contains(connection2) ||
                    sd.getConnections(connection.node2()).contains(connection2))
                continue;

            if (connectionData2.wireData instanceof CatenaryConnectionData) {
                if (connection.node1().sourcePos().equals(connection2.node1().sourcePos()) ||
                        connection.node2().sourcePos().equals(connection2.node1().sourcePos()) ||
                        connection.node1().sourcePos().equals(connection2.node2().sourcePos()) ||
                        connection.node2().sourcePos().equals(connection2.node2().sourcePos()))
                    continue;
            }

            List<Vec3> points2 = connectionData2.points;

            float bestPoint = 0;
            float bestPoint2 = 0;
            double bestDist = 999;
            Vec3 bestPos1 = null;
            Vec3 bestPos2 = null;

            for (int k = 0; k < points.size(); k++) {
                Vec3 point1 = points.get(k);
                for (int l = 0; l < points2.size(); l++) {
                    Vec3 point2 = points2.get(l);
                    double distance = point1.distanceToSqr(point2);
                    if (distance < bestDist) {
                        bestPoint = k / (float) points.size();
                        bestPoint2 = l / (float) points.size();
                        bestDist = distance;
                        bestPos1 = point1;
                        bestPos2 = point2;
                    }
                }
            }

            if (bestDist < 0.04) {

                WireData wireData2 = connectionData2.wireData;
                if (wireData.wireType().insulated() || wireData2.wireType().insulated())
                    continue;

                AttachedNode attachedNode1 = connectionData.addCut(bestPoint);
                AttachedNode attachedNode2 = connectionData2.addCut(bestPoint2);

                crossContacts.put(Couple.create(attachedNode1, attachedNode2), new CrossContactEntry(0.1, bestPos1, bestPos2, bestDist, connection, connection2));
            }
        }
    }

    public void buildCircuit(CircuitBuilder builder) {
        crossContacts.forEach((c, e) -> builder.connect(c.getFirst(), c.getSecond(), ElectricalProperties.resistor(e.resistance)));
    }

    public record CrossContactEntry(double resistance, Vec3 pos1, Vec3 pos2, double distance, InWorldNodeConnection wire1, InWorldNodeConnection wire2) {

    }
}
