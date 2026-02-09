package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEDamageTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendQuadraticParticlesPacket;
import com.george_vi.electroenergetics.content.wire.SendWireParticlesPacket;
import com.george_vi.electroenergetics.foundation.*;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.PositionedAttachedNode;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WireConnectionManager {
    final InfrastructureSavedData sd;
    final Level level;
    Map<InWorldNodeConnection, ConnectionData> connections = new HashMap<>();

    Map<Couple<AttachedNode>, WireShortEntry> wireShorts = new HashMap<>();

    Map<Entity, ElectrocutionEntry> electrocutions = new HashMap<>();
    Long2ObjectMap<List<Pair<InWorldNodeConnection, ConnectionData>>> chunks = new Long2ObjectOpenHashMap<>();
    int nodeID = 0;

    static String cutWireString = "CEECutWire";
    static String electrocutionCutString = "CEEElectrocutionCut";
    static String electrocutionString = "CEEElectrocutionNode";
    static String electrocutionGroundString = "CEEElectrocutionGroundNode";

    public WireConnectionManager(InfrastructureSavedData sd, Level level) {
        this.sd = sd;
        this.level = level;
    }

    public void buildCircuit(CircuitBuilder builder) {
        electrocutions.clear();
        Map<InWorldNodeConnection, List<CutWireEntry>> electrocutionCuts = new Object2ObjectOpenHashMap<>();
        if (CEEConfigs.server().enableElectrocution.get())
            computeElectrocutions(builder, electrocutionCuts);

        for (Map.Entry<InWorldNodeConnection, ConnectionData> e : connections.entrySet()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionData connectionData = e.getValue();
            double resistance = SimulationTicker.getWireResistance(connection.node1(), connection.node2(), connectionData.wireType);
            List<CutWireEntry> cuts = connectionData.cuts;
            if (cuts != null)
                cuts = new ArrayList<>(cuts);

            List<CutWireEntry> wireElectrocutionCuts = electrocutionCuts.get(connection);

            if (wireElectrocutionCuts != null) {
                if (cuts == null)
                    cuts = new ArrayList<>();
                cuts.addAll(wireElectrocutionCuts);
            }

            if (cuts == null || cuts.isEmpty())
                builder.connect(connection.node1(), connection.node2(), ElectricalProperties.resistor(resistance));
            else {
                cuts.sort(Comparator.comparing(CutWireEntry::point));
                float totalProgress = 0;
                Node lastNode = connection.node1();
                for (CutWireEntry cut : cuts) {
                    if (cut.node.equals(lastNode))
                        continue;
                    float progress = cut.point - totalProgress + 0.0001f;
                    builder.connect(lastNode, cut.node, ElectricalProperties.resistor(resistance * progress));
                    lastNode = cut.node;
                }
                builder.connect(lastNode, connection.node2(), ElectricalProperties.resistor(resistance * (1.0001f - totalProgress)));
            }
        }
        wireShorts.forEach((c, e) -> builder.connect(c.getFirst(), c.getSecond(), ElectricalProperties.resistor(e.resistance)));
    }

    private void computeElectrocutions(CircuitBuilder builder, Map<InWorldNodeConnection, List<CutWireEntry>> electrocutionCuts) {
        int cutElectrocutionNodeID = 0;
        AtomicInteger electrocutionNodeID = new AtomicInteger();
        for (Entity entity : ((ServerLevel) level).getAllEntities()) {
            if (!(entity instanceof Mob || entity instanceof ServerPlayer))
                continue;
            if (entity instanceof ServerPlayer p && (p.gameMode.getGameModeForPlayer() == GameType.CREATIVE || p.gameMode.getGameModeForPlayer() == GameType.SPECTATOR))
                continue;
            AABB bb1 = entity.getBoundingBox();
            int xChunkPos = entity.chunkPosition().x;
            int zChunkPos = entity.chunkPosition().z;
            List<Pair<InWorldNodeConnection, ConnectionData>> wiresToCheck = new ArrayList<>();
            List<Pair<InWorldNodeConnection, ConnectionData>> addedWires = chunks.get(DataPacker.pack(xChunkPos + 1, zChunkPos + 1));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos, zChunkPos + 1));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos - 1, zChunkPos + 1));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos + 1, zChunkPos));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos, zChunkPos));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos - 1, zChunkPos));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos + 1, zChunkPos - 1));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos, zChunkPos - 1));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);
            addedWires = chunks.get(DataPacker.pack(xChunkPos - 1, zChunkPos - 1));
            if (addedWires != null)
                wiresToCheck.addAll(addedWires);

            if (wiresToCheck.isEmpty())
                continue;

            ElectrocutionEntry electrocutionEntry = null;
            for (Pair<InWorldNodeConnection, ConnectionData> e : wiresToCheck) {
                InWorldNodeConnection connection = e.getFirst();
                ConnectionData connectionData = e.getSecond();

                AABB bb2 = connectionData.dangerousBB;
                double dangerousDistance = connectionData.dangerousDistance;

                if (!(bb1.minX <= bb2.maxX) || !(bb1.maxX >= bb2.minX) ||
                        !(bb1.minY <= bb2.maxY) || !(bb1.maxY >= bb2.minY) ||
                        !(bb1.minZ <= bb2.maxZ) || !(bb1.maxZ >= bb2.minZ))
                    continue;

                Vec3 pos1 = sd.getNodePosition(connection.node1());
                Vec3 pos2 = sd.getNodePosition(connection.node2());
                WireData wireData = sd.getConnectionData(connection);
                if (!connectionData.isOvervolted)
                    continue;

                List<Vec3> points = connectionData.points;
                Vec3 bestPoint = null;
                float bestProgress = 0;
                double bestResistance = 1e+11d;
                for (int i = 0; i < points.size(); i++) {
                    Vec3 point = points.get(i);
                    double distanceX = Math.max(0, Math.max(bb1.minX - point.x, point.x - bb1.maxX));
                    double distanceY = Math.max(0, Math.max(bb1.minY - point.y, point.y - bb1.maxY));
                    double distanceZ = Math.max(0, Math.max(bb1.minZ - point.z, point.z - bb1.maxZ));
                    double distanceSqr = (distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ);
                    if (distanceSqr < dangerousDistance) {
                        double resistance = wireData.wireType().insulationResistance() + 1444;
                        if (distanceSqr > 0.1f)
                            resistance += distanceSqr * 1000;
                        if (resistance < bestResistance) {
                            bestPoint = point;
                            bestProgress = (float) i/points.size();
                            bestResistance = resistance;
                        }
                    }
                }

                if (bestPoint != null) {
                    if (electrocutionEntry == null)
                        electrocutionEntry = electrocutions.computeIfAbsent(entity, k -> {
                            ElectrocutionEntry ee = new ElectrocutionEntry(new AttachedNode(electrocutionNodeID.getAndIncrement(), electrocutionString), new Object2DoubleArrayMap<>(16), new Object2ObjectArrayMap<>(16));
                            if (entity.onGround()) {
                                PositionedAttachedNode groundNode = new PositionedAttachedNode(electrocutionNodeID.getAndIncrement(), electrocutionGroundString, entity.position());
                                ee.nodes.put(groundNode, 10);
                                ee.positions().put(groundNode, entity.position());
                                builder.connect(ee.centralNode, groundNode, ElectricalProperties.resistor(10));
                                builder.ground(groundNode, 1 / 2333d);
                            }
                            return ee;
                        });
                    AttachedNode cutNode = new AttachedNode(cutElectrocutionNodeID++, electrocutionCutString);
                    electrocutionEntry.nodes.put(cutNode, bestResistance);
                    electrocutionEntry.positions.put(cutNode, bestPoint);
                    electrocutionCuts.computeIfAbsent(connection, k -> new ArrayList<>()).add(new CutWireEntry(bestProgress, cutNode, null, null));
                    builder.connect(electrocutionEntry.centralNode, cutNode, ElectricalProperties.resistor(bestResistance));
                }
            }
        }
    }

    public void rebuild() {
        Map<InWorldNodeConnection, WireData> allConnections = sd.getAllConnections();
        connections.clear();
        wireShorts.clear();
        chunks.clear();

        for (Map.Entry<InWorldNodeConnection, WireData> e : allConnections.entrySet()) {
            InWorldNodeConnection connection = e.getKey();
            WireData wireData = e.getValue();

            Vec3 pos1 = sd.getNodePosition(connection.node1());
            Vec3 pos2 = sd.getNodePosition(connection.node2());
            List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.wireType().getSag(), 0.5f);
            double miny = pos1.y;
            for (Vec3 point : points)
                miny = Math.min(miny, point.y());
            AABB bb = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);
            ConnectionData connectionData = new ConnectionData(bb, points, wireData.wireType());
            connections.put(connection, connectionData);
        }


        // Check for wire shorts
        // It's slow but only done on reload
        for (Map.Entry<InWorldNodeConnection, ConnectionData> e1 : connections.entrySet()) {

            InWorldNodeConnection connection1 = e1.getKey();
            ConnectionData data1 = e1.getValue();
            List<Vec3> points1 = data1.points;
            AABB bb1 = data1.bb;
            for (Map.Entry<InWorldNodeConnection, ConnectionData> e2 : connections.entrySet()) {

                InWorldNodeConnection connection2 = e2.getKey();
                ConnectionData data2 = e2.getValue();
                AABB bb2 = data2.bb;

                // Check if the wires intersect
                if (!(bb1.minX <= bb2.maxX) || !(bb1.maxX >= bb2.minX) ||
                        !(bb1.minY <= bb2.maxY) || !(bb1.maxY >= bb2.minY) ||
                        !(bb1.minZ <= bb2.maxZ) || !(bb1.maxZ >= bb2.minZ))
                    continue;
                if (sd.getConnections(connection1.node1()).contains(connection2) ||
                        sd.getConnections(connection1.node2()).contains(connection2))
                    continue;

                List<Vec3> points2 = data2.points;

                float bestPoint = 0;
                double bestDist = 999;
                Vec3 bestPos1 = null;
                Vec3 bestPos2 = null;

                for (int k = 0; k < points1.size(); k++) {
                    Vec3 point1 = points1.get(k);
                    for (Vec3 point2 : points2) {
                        double distance = point1.distanceToSqr(point2);
                        if (distance < bestDist) {
                            bestPoint = k / (float) points1.size();
                            bestDist = distance;
                            bestPos1 = point1;
                            bestPos2 = point2;
                        }
                    }
                }

                if (bestDist < 0.04) {

                    WireData wireData1 = sd.getConnectionData(connection1);
                    WireData wireData2 = sd.getConnectionData(connection2);
                    if (wireData1 == null || wireData2 == null || wireData1.wireType().insulated() || wireData2.wireType().insulated())
                        continue;

                    AttachedNode attachedNode1 = new AttachedNode(nodeID++, cutWireString);
                    AttachedNode attachedNode2 = new AttachedNode(nodeID++, cutWireString);
                    // Wires touch

                    data1.cuts.add(new CutWireEntry(bestPoint, attachedNode1, attachedNode2, connection2));
                    data2.cuts.add(new CutWireEntry(bestPoint, attachedNode2, attachedNode1, connection1));

                    wireShorts.put(Couple.create(attachedNode1, attachedNode2), new WireShortEntry(0.1d, bestPos1, bestPos2, bestDist));
                    wireShorts.put(Couple.create(attachedNode2, attachedNode1), new WireShortEntry(0.1d, bestPos1, bestPos2, bestDist));
                }
            }

            // Put into chunks
            ChunkPos prevChunk = null;
            for (Vec3 point : data1.points) {
                ChunkPos chunkPos = new ChunkPos(BlockPos.containing(point));
                if (chunkPos.equals(prevChunk))
                    continue;
                prevChunk = chunkPos;
                chunks.computeIfAbsent(DataPacker.pack(chunkPos.x, chunkPos.z), k -> new ArrayList<>()).add(Pair.of(connection1, data1));
            }
        }
    }

    public void finish(SimulationResults results) {
        SimulationTicker.profiler.push("updateWireTemperature");

        // Wire breaking
        if (CEEConfigs.server().wiresBreak.get()) {
            InWorldNodeConnection longestWireToBreak = null;
            WireType longestWireTypeToBreak = null;
            for (Map.Entry<InWorldNodeConnection, ConnectionData> e : connections.entrySet()) {
                InWorldNodeConnection connection = e.getKey();
                ConnectionData connectionData = e.getValue();
                WireType wireType = connectionData.wireType;
                List<CutWireEntry> cuts = connectionData.cuts;

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

                double lastWireVoltage = Math.max(Math.abs(sd.getVoltageAt(connection.node1())), Math.abs(sd.getVoltageAt(connection.node2())));
                double unsafeDistance = Mth.clamp(Math.log(0.006d * lastWireVoltage + 1) / 8.1d + 0.000011d * lastWireVoltage -0.6d, 0.1, 3);
                connectionData.dangerousDistance = unsafeDistance * unsafeDistance;

                connectionData.dangerousBB = connectionData.bb.inflate(connectionData.dangerousDistance);
                connectionData.isOvervolted = lastWireVoltage > connectionData.wireType.maxInsulationVoltage.getAsDouble();

                double finalCurrent = current;
                float newTemp = sd.updateConnectionTemperature(connection, temp -> {
                    float nTemp = (float) (Math.min(finalCurrent, 1000));
                    nTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
                    nTemp = Math.max(temp - 33.3f + nTemp, 0);
                    return nTemp;
                });

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
                }

            }
            if (longestWireToBreak != null && sd.level.random.nextFloat() > 0.96f) {
                WireType replaceWith = longestWireTypeToBreak.overheatedReplacement();
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

        wireShorts.forEach((c, e) -> {
            AttachedNode node1 = c.getFirst();
            AttachedNode node2 = c.getSecond();
            double current = Math.abs(results.getCurrentThrough(node1, node2));
            if (current > 0.1) {

                if (level.random.nextFloat() > 0.7f && e.distance > 0.03d) {
                    Vec3 middlePos = VecHelper.lerp(0.5f, e.startPosition, e.endPosition);
                    CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, middlePos, 30, new SendSparkPacket(middlePos, SendSparkPacket.SparkSize.SMALL));
                }
            }
        });

        if (electrocutions.isEmpty() || !CEEConfigs.server().enableElectrocution.get()) {
            SimulationTicker.profiler.pop();
            return;
        }

        SimulationTicker.profiler.popPush("electrocute");

        Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        DamageSource damageSource = new DamageSource(registry.getHolderOrThrow(CEEDamageTypes.ELECTROCUTION));
        DamageSource hvDamageSource = new DamageSource(registry.getHolderOrThrow(CEEDamageTypes.HV_ELECTROCUTION));
        for (Map.Entry<Entity, ElectrocutionEntry> e : electrocutions.entrySet()) {
            Entity entity = e.getKey();
            ElectrocutionEntry electrocutionEntry = e.getValue();
            AttachedNode centralNode = electrocutionEntry.centralNode;
            double highestCurrent = 0d;
            double highestVoltage = results.getVoltageAt(centralNode);
            for (Object2DoubleMap.Entry<AttachedNode> e1 : electrocutionEntry.nodes.object2DoubleEntrySet()) {
                AttachedNode node = e1.getKey();
                double resistance = e1.getDoubleValue();
                double current = Math.abs(results.getVoltageAt(centralNode, node)) / resistance;
                double voltage = Math.abs(results.getVoltageAt(node));
                if (current > 0.03) {
                    Vec3 pos1 = electrocutionEntry.positions.get(node);
                    Vec3 pos2 = entity.position();
                    if (!(entity instanceof Player p && p.isCreative()))
                        CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos1, 40, new SendQuadraticParticlesPacket(pos1, pos2, ParticleTypes.BUBBLE_POP, -2f, 0.7f));
                }
                highestCurrent = Math.max(highestCurrent, current);
                highestVoltage = Math.max(highestVoltage, voltage);
            }

            if (highestCurrent > 0.03) {
                float damage = highestVoltage < 14_000 ? (float) (2 * (Math.log(highestCurrent * 10.5d) * 1.9d + 3.6d + 4.84d * highestCurrent)) :
                        (float) (highestVoltage / 3_000);

                entity.hurt(highestVoltage > 9_900 ? hvDamageSource : damageSource, damage);
            }
        }

        SimulationTicker.profiler.pop();
    }

    public void wireRemoved(InWorldNodeConnection connection) {
        ConnectionData connectionData = connections.get(connection);
        if (connectionData == null)
            return;
        List<CutWireEntry> cuts = connectionData.cuts;

        for (int i = 0; i < cuts.size(); i++) {
            CutWireEntry cut = cuts.get(i);
            InWorldNodeConnection neighbourConnection = cut.neighbourWire;
            ConnectionData neighbourData = connections.get(neighbourConnection);
            List<CutWireEntry> neighbourCuts = neighbourData.cuts;
            for (int j = 0; j < neighbourCuts.size(); j++) {
                AttachedNode neighbourNode = neighbourCuts.get(j).neighbourNode;
                if (!neighbourNode.equals(cut.node))
                    continue;

                neighbourCuts.remove(j);
                wireShorts.remove(Couple.create(cut.neighbourNode, cut.node));
                wireShorts.remove(Couple.create(cut.node, cut.neighbourNode));
                break;
            }


        }
        connections.remove(connection);

        List<Long> chunkPositionToRemove = new LinkedList<>();
        chunks.forEach((packedChunkPos, connections) -> {
            connections.removeIf(c -> c.getFirst().equals(connection));
            if (connections.isEmpty())
                chunkPositionToRemove.add(packedChunkPos);
        });

        for (long packedChunkPos : chunkPositionToRemove)
            chunks.remove(packedChunkPos);
    }

    public void wireAdded(InWorldNodeConnection connection1, WireData wireData) {
        Vec3 pos1 = sd.getNodePosition(connection1.node1());
        Vec3 pos2 = sd.getNodePosition(connection1.node2());
        List<Vec3> points1 = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.wireType().getSag(), 0.5f);
        double miny = pos1.y;
        for (Vec3 point : points1)
            miny = Math.min(miny, point.y());
        AABB bb1 = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);
        ConnectionData connectionData1 = new ConnectionData(bb1, points1, wireData.wireType());

        for (Map.Entry<InWorldNodeConnection, ConnectionData> e : connections.entrySet()) {
            InWorldNodeConnection connection2 = e.getKey();
            ConnectionData connectionData2 = e.getValue();
            AABB bb2 = connectionData2.bb;
            if (!(bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX &&
                    bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY &&
                    bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ))
                continue;
            if (sd.getConnections(connection1.node1()).contains(connection2) ||
                    sd.getConnections(connection1.node2()).contains(connection2))
                continue;

            List<Vec3> points2 = connectionData2.points;

            float bestPoint = 0;
            double bestDist = 999;
            Vec3 bestPos1 = null;
            Vec3 bestPos2 = null;

            for (int k = 0; k < points1.size(); k++) {
                Vec3 point1 = points1.get(k);
                for (int l = 0; l < points2.size(); l++) {
                    Vec3 point2 = points2.get(l);
                    double distance = point1.distanceToSqr(point2);
                    if (distance < bestDist) {
                        bestPoint = k / (float) points1.size();
                        bestDist = distance;
                        bestPos1 = point1;
                        bestPos2 = point2;
                    }
                }
            }

            if (bestDist < 0.04) {

                WireData wireData2 = sd.getConnectionData(connection2);
                if (wireData2 == null || wireData.wireType().insulated() || wireData2.wireType().insulated())
                    continue;

                AttachedNode attachedNode1 = new AttachedNode(nodeID++, cutWireString);
                AttachedNode attachedNode2 = new AttachedNode(nodeID++, cutWireString);
                // Wires touch
                connectionData1.cuts.add(new CutWireEntry(bestPoint, attachedNode1, attachedNode2, connection2));
                connectionData2.cuts.add(new CutWireEntry(bestPoint, attachedNode2, attachedNode1, connection1));
                wireShorts.put(Couple.create(attachedNode1, attachedNode2), new WireShortEntry(0.1, bestPos1, bestPos2, bestDist));
            }
        }
        connections.put(connection1, connectionData1);

        ChunkPos prevChunk = null;
        for (Vec3 point : points1) {
            ChunkPos chunkPos = new ChunkPos(BlockPos.containing(point));
            if (chunkPos.equals(prevChunk))
                continue;
            prevChunk = chunkPos;
            chunks.computeIfAbsent(DataPacker.pack(chunkPos.x, chunkPos.z), k -> new ArrayList<>()).add(Pair.of(connection1, connectionData1));
        }
    }

    record CutWireEntry(Float point, AttachedNode node, AttachedNode neighbourNode, InWorldNodeConnection neighbourWire) {

    }

    record ElectrocutionEntry(AttachedNode centralNode, Object2DoubleMap<AttachedNode> nodes, Map<AttachedNode, Vec3> positions) {

    }

    static class ConnectionData {
        final public List<Vec3> points;
        final public List<CutWireEntry> cuts = new ArrayList<>();
        final public AABB bb;
        public AABB dangerousBB;
        final public WireType wireType;
        public double dangerousDistance;
        public boolean isOvervolted;

        public ConnectionData(AABB bb, List<Vec3> points, WireType wireType) {
            this.bb = bb;
            this.points = points;
            this.wireType = wireType;
            this.dangerousBB = bb;
        }
    }

    record WireShortEntry(double resistance, Vec3 startPosition, Vec3 endPosition, double distance) {

    }
}
