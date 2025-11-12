package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendWireParticlesPacket;
import com.george_vi.electroenergetics.foundation.*;
import com.george_vi.electroenergetics.simulation.simulator.*;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.DoubleSupplier;

public class WireConnectionManager {
    final InfrastructureSavedData sd;
    final Level level;
    Map<NodeConnection, WireType> originalConnections = new HashMap<>();
    Map<NodeConnection, List<CutWireEntry>> cutConnections = new HashMap<>();
    // For performance, instead of Couple<AttachedNode>. A single long can hold 2 ints.
    // these ints are for IDs, OwnerIDs are always 'CEECutWire'
    // First -> First 4 bytes >> 32
    // Last -> Last 4 bytes
    Long2ObjectMap<DoubleSupplier> interWireShorts = new Long2ObjectOpenHashMap<>();
    Long2ObjectMap<Vec3> shortPositions = new Long2ObjectOpenHashMap<>();
    Long2DoubleMap shortDistances = new Long2DoubleOpenHashMap();
    Map<NodeConnection, AABB> wireBBs = new HashMap<>();
    int nodeID = 0;

    public WireConnectionManager(InfrastructureSavedData sd, Level level) {
        this.sd = sd;
        this.level = level;
    }

    public void buildCircuit(CircuitBuilder builder) {
        for (Map.Entry<NodeConnection, WireType> e : originalConnections.entrySet()) {
            NodeConnection connection = e.getKey();
            double resistance = SimulationTicker.getWireResistance(connection.node1(), connection.node2(), e.getValue());
            List<CutWireEntry> cuts = cutConnections.get(connection);
            if (cuts == null)
                builder.connect(connection.node1(), connection.node2(), ElectricalProperties.resistor(resistance));
            else {
//                cuts.sort(Comparator.comparing(CutWireEntry::point));
                float totalProgress = 0;
                Node lastNode = connection.node1();
                for (CutWireEntry cut : cuts) {
                    builder.addNode(cut.node);
                    float progress = cut.point - totalProgress + 0.0001f;
                    builder.connect(lastNode, cut.node, ElectricalProperties.resistor(resistance * progress));
                    lastNode = cut.node;
                }
                builder.connect(lastNode, connection.node2(), ElectricalProperties.resistor(resistance * (1.0001f - totalProgress)));
            }
        }
        interWireShorts.forEach((packed, r) -> builder.connect(createFirstNode(packed), createSecondNode(packed), ElectricalProperties.resistor(r.getAsDouble())));
    }


    public void rebuild() {
        Map<NodeConnection, WireData> allConnections = sd.getAllConnections();
        originalConnections.clear();
        cutConnections.clear();
        interWireShorts.clear();
        shortPositions.clear();
        wireBBs.clear();

        // It feels sketchy coupling by index like this, but well, it should work just fine ig.
        List<Pair<NodeConnection, List<Vec3>>> allPoints = new ArrayList<>(allConnections.size());
        List<AABB> allWireBBs = new ArrayList<>(allConnections.size());

        for (Map.Entry<NodeConnection, WireData> e : allConnections.entrySet()) {
            NodeConnection connection = e.getKey();
            WireData wireData = e.getValue();
            originalConnections.put(connection, wireData.wireType());
            Vec3 pos1 = sd.getNodePosition(connection.node1());
            Vec3 pos2 = sd.getNodePosition(connection.node2());
            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.wireType().getSag(), 0.5f);
            double miny = pos1.y;
            for (Vec3 point : points)
                miny = Math.min(miny, point.y());
            allPoints.add(Pair.of(connection, points));
            AABB bb = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);
            allWireBBs.add(bb);
            wireBBs.put(connection, bb);
        }


        // Check for wire shorts
        // 4 nested loops, seems super inefficient, but the first two just check if the wires are within bounds, which are quick operations, and the inner two are only executed when a wire is within bounds.
        // also it's recalculated only on reload
        for (int i = 0; i < allPoints.size(); i++) {
            NodeConnection connection1 = allPoints.get(i).getFirst();
            List<Vec3> points1 = allPoints.get(i).getSecond();
            AABB bb1 = allWireBBs.get(i);
            for (int j = 0; j < i; j++) {

                NodeConnection connection2 = allPoints.get(j).getFirst();
                AABB bb2 = allWireBBs.get(j);


                // Check if the wires intersect
                if (bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX &&
                        bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY &&
                        bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ) {
                    if (sd.getConnections(connection1.node1()).contains(connection2) ||
                            sd.getConnections(connection1.node2()).contains(connection2))
                        continue;
                    List<Vec3> points2 = allPoints.get(j).getSecond();

                    float bestPoint = 0;
                    double bestDist = 999;
                    Vec3 bestPos = null;

                    for (int k = 0; k < points1.size(); k++) {
                        Vec3 point1 = points1.get(k);
                        for (int l = 0; l < points2.size(); l++) {
                            Vec3 point2 = points2.get(l);
                            double distance = point1.distanceToSqr(point2);
                            if (distance < bestDist) {
                                bestPoint = k / (float) points1.size();
                                bestDist = distance;
                                bestPos = point1;
                            }
                        }
                    }

                    if (bestDist < 0.04) {

                        WireData wireData1 = sd.getConnectionData(connection1);
                        WireData wireData2 = sd.getConnectionData(connection2);
                        if (wireData1 == null || wireData2 == null || wireData1.wireType().insulated() || wireData2.wireType().insulated())
                            continue;

                        AttachedNode attachedNode1 = createNode(nodeID++);
                        AttachedNode attachedNode2 = createNode(nodeID++);
                        // Wires touch
                        cutConnections.computeIfAbsent(connection1, c -> new ArrayList<>()).add(new CutWireEntry(bestPoint, attachedNode1, attachedNode2, connection2));
                        cutConnections.computeIfAbsent(connection2, c -> new ArrayList<>()).add(new CutWireEntry(bestPoint, attachedNode2, attachedNode1, connection1));
                        long packedShortConnection = DataPacker.pack(attachedNode1.id, attachedNode2.id);
                        long invertedPackedShortConnection = DataPacker.pack(attachedNode2.id, attachedNode1.id);
                        interWireShorts.put(packedShortConnection, () -> 0.1d);
                        shortDistances.put(packedShortConnection, bestDist);
                        shortPositions.put(packedShortConnection, bestPos);

                        interWireShorts.put(invertedPackedShortConnection, () -> 0.1d);
                        shortDistances.put(invertedPackedShortConnection, bestDist);
                        shortPositions.put(invertedPackedShortConnection, bestPos);
                    }
                }
            }
        }
    }

    public void finish(SimulationResults results) {
        SimulationTicker.profiler.push("updateWireTemperature");

        // Wire breaking
        if (CEEConfigs.server().wiresBreak.get()) {
            NodeConnection longestWireToBreak = null;
            WireType longestWireTypeToBreak = null;
            boolean increase = false;
            for (Map.Entry<NodeConnection, WireType> e : originalConnections.entrySet()) {
                NodeConnection connection = e.getKey();
                WireType wireType = e.getValue();

                List<CutWireEntry> cuts = cutConnections.get(connection);
                double current = 0;
                double wholeWireResistance = SimulationTicker.getWireResistance(connection.node1(), connection.node2(), wireType);
                if (cuts == null) {
                    double vd = Math.abs(results.getVoltageAt(connection.node1(), connection.node2()));
                    current = vd / wholeWireResistance;
                } else {
                    float prevPoint = 0;
                    Node prevNode = connection.node1();
                    for (CutWireEntry cut : cuts) {
                        float dist = cut.point - prevPoint;
                        prevPoint = cut.point;
                        double vd = Math.abs(results.getVoltageAt(prevNode, cut.node));
                        prevNode = cut.node;
                        current = Math.max(current, vd / (wholeWireResistance * dist));
                    }
                }

                float temp = sd.getConnectionTemperature(connection);

                float newTemp = (float) (Math.min(current, 1000));
                newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
                newTemp = Math.max(temp - 33.3f + newTemp, 0);
                sd.setConnectionTemperature(connection, newTemp);

                if (newTemp > wireType.getMaxTemperature() * 0.6 && level.isLoaded(connection.node1().sourcePos())) {
                    // Smoke particles
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, VecHelper.lerp(0.5f, connection.node1().sourcePos().getCenter(), connection.node2().sourcePos().getCenter()),
                            connection.node1().sourcePos().getCenter().distanceTo(connection.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(connection.node1(), connection.node2(), ParticleTypes.SMOKE, wireType.getSag(), 0.2f));
                }

                if (newTemp > wireType.getMaxTemperature()) {
                    if (longestWireToBreak == null) {
                        longestWireToBreak = connection;
                        longestWireTypeToBreak = wireType;

                    }
                    else if (SimulationTicker.getWireResistance(longestWireToBreak.node1(), longestWireToBreak.node2(), wireType) < SimulationTicker.getWireResistance(connection.node1(), connection.node2(), wireType)) {
                        longestWireToBreak = connection;
                        longestWireTypeToBreak = wireType;
                    }
                    increase = newTemp > temp;
                }

            }
            if (longestWireToBreak != null && increase && sd.level.random.nextFloat() > 0.96f) {
                WireType replaceWith = longestWireTypeToBreak.replaceOverheatedWith();
                if (replaceWith == null) {
                    sd.removeConnection(longestWireToBreak);
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, VecHelper.lerp(0.5f, longestWireToBreak.node1().sourcePos().getCenter(), longestWireToBreak.node2().sourcePos().getCenter()),
                            longestWireToBreak.node1().sourcePos().getCenter().distanceTo(longestWireToBreak.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(longestWireToBreak.node1(), longestWireToBreak.node2(), ParticleTypes.BUBBLE_POP, longestWireTypeToBreak.getSag(), 4));
                } else {
                    WireData wireConnectionData = sd.getConnectionData(longestWireToBreak);
                    sd.setConnectionData(longestWireToBreak, new WireData(replaceWith, wireConnectionData.temperature(), wireConnectionData.attachments()));
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, VecHelper.lerp(0.5f, longestWireToBreak.node1().sourcePos().getCenter(), longestWireToBreak.node2().sourcePos().getCenter()),
                            longestWireToBreak.node1().sourcePos().getCenter().distanceTo(longestWireToBreak.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(longestWireToBreak.node1(), longestWireToBreak.node2(), ParticleTypes.LARGE_SMOKE, longestWireTypeToBreak.getSag(), 4));
                }
            }
        }
        SimulationTicker.profiler.popPush("showWireShorts");

        for (long packedConnection : interWireShorts.keySet()) {
            AttachedNode node1 = createFirstNode(packedConnection);
            AttachedNode node2 = createSecondNode(packedConnection);
            double current = Math.abs(results.getCurrentThrough(node1, node2));
            if (current > 0.1) {
                Vec3 pos = shortPositions.get(packedConnection);
                if (pos == null)
                    continue;
                if (level.random.nextFloat() > 0.7f && shortDistances.getOrDefault(packedConnection, 0d) > 0.03d)
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos, 30, new SendSparkPacket(pos, SendSparkPacket.SparkSize.SMALL));
            }
        }
        SimulationTicker.profiler.pop();
    }

    public void wireRemoved(NodeConnection connection) {
        List<CutWireEntry> cuts = cutConnections.get(connection);
        if (cuts != null) {

            for (int i = 0; i < cuts.size(); i++) {
                CutWireEntry cut = cuts.get(i);
                NodeConnection neighbourConnection = cut.neighbourWire;
                List<CutWireEntry> neighbourCuts = cutConnections.get(neighbourConnection);
                if (neighbourCuts == null)
                    continue;
                for (int j = 0; j < neighbourCuts.size(); j++) {
                    AttachedNode neighbourNode = neighbourCuts.get(j).neighbourNode;
                    if (neighbourNode.equals(cut.node)) {
                        neighbourCuts.remove(j);
                        long short1 = DataPacker.pack(cut.node.id, neighbourNode.id);
                        long short2 = DataPacker.pack(neighbourNode.id, cut.node.id);
                        interWireShorts.remove(short1);
                        interWireShorts.remove(short2);
                        shortDistances.remove(short1);
                        shortDistances.remove(short2);
                        shortPositions.remove(short1);
                        shortPositions.remove(short2);
                        if (neighbourCuts.isEmpty())
                            cutConnections.remove(neighbourConnection);
                        break;
                    }
                }


            }
            cutConnections.remove(connection);
        }

        originalConnections.remove(connection);
        wireBBs.remove(connection);
    }

    public void wireAdded(NodeConnection connection, WireData wireData) {
        Vec3 pos1 = sd.getNodePosition(connection.node1());
        Vec3 pos2 = sd.getNodePosition(connection.node2());
        List<Vec3> points1 = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.wireType().getSag(), 0.5f);
        double miny = pos1.y;
        for (Vec3 point : points1)
            miny = Math.min(miny, point.y());
        AABB bb1 = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);

        for (Map.Entry<NodeConnection, AABB> e : wireBBs.entrySet()) {
            NodeConnection neighbourConnection = e.getKey();
            AABB bb2 = e.getValue();
            if (!(bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX &&
                    bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY &&
                    bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ))
                continue;
            if (sd.getConnections(connection.node1()).contains(neighbourConnection) ||
                    sd.getConnections(connection.node2()).contains(neighbourConnection))
                continue;
            Vec3 neighbourPos1 = sd.getNodePosition(neighbourConnection.node1());
            Vec3 neighbourPos2 = sd.getNodePosition(neighbourConnection.node2());

            List<Vec3> points2 = QuadraticWireHelper.cablePoints(neighbourPos1, neighbourPos2, wireData.wireType().getSag(), 0.5f);

            float bestPoint = 0;
            double bestDist = 999;
            Vec3 bestPos = null;

            for (int k = 0; k < points1.size(); k++) {
                Vec3 point1 = points1.get(k);
                for (int l = 0; l < points2.size(); l++) {
                    Vec3 point2 = points2.get(l);
                    double distance = point1.distanceToSqr(point2);
                    if (distance < bestDist) {
                        bestPoint = k / (float) points1.size();
                        bestDist = distance;
                        bestPos = point1;
                    }
                }
            }

            if (bestDist < 0.04) {

                WireData wireData2 = sd.getConnectionData(neighbourConnection);
                if (wireData2 == null || wireData.wireType().insulated() || wireData2.wireType().insulated())
                    continue;

                AttachedNode attachedNode1 = createNode(nodeID++);
                AttachedNode attachedNode2 = createNode(nodeID++);
                // Wires touch
                cutConnections.computeIfAbsent(connection, c -> new ArrayList<>()).add(new CutWireEntry(bestPoint, attachedNode1, attachedNode2, neighbourConnection));
                cutConnections.computeIfAbsent(neighbourConnection, c -> new ArrayList<>()).add(new CutWireEntry(bestPoint, attachedNode2, attachedNode1, connection));
                long packedConnection = DataPacker.pack(attachedNode1.id, attachedNode2.id);
                long invertedPackedConnection = DataPacker.pack(attachedNode2.id, attachedNode1.id);
                interWireShorts.put(packedConnection, () -> 0.1d);
                shortPositions.put(packedConnection, bestPos);
                shortDistances.put(packedConnection, bestDist);

                interWireShorts.put(invertedPackedConnection, () -> 0.1d);
                shortPositions.put(invertedPackedConnection, bestPos);
                shortDistances.put(invertedPackedConnection, bestDist);
            }
        }
        originalConnections.put(connection, wireData.wireType());
        wireBBs.put(connection, bb1);
    }

    record CutWireEntry(Float point, AttachedNode node, AttachedNode neighbourNode, NodeConnection neighbourWire) { }

    private static AttachedNode createNode(int id) {
        return new AttachedNode(id, "CEECutWire");
    }

    private static AttachedNode createFirstNode(long packed) {
        return new AttachedNode((int) ((packed >> 32) & 0xFFFFFFFFL), "CEECutWire");
    }

    private static AttachedNode createSecondNode(long packed) {
        return new AttachedNode((int) (packed & 0xFFFFFFFFL), "CEECutWire");
    }
}
