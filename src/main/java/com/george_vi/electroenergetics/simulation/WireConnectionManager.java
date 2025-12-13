package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEDamageTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendQuadraticParticlesPacket;
import com.george_vi.electroenergetics.content.wire.SendWireParticlesPacket;
import com.george_vi.electroenergetics.foundation.*;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.PositionedAttachedNode;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.MicroTickedSimulationTicker;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
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
    Map<Entity, ElectrocutionEntry> electrocutions = new HashMap<>();
    // Instead of a ChunkPos, which contains two ints: x and z, this is a long, which holds both of them.
    // First -> X
    // Second -> Z
    // I don't know if it's going to have such a great impact on performance, but it should be better.
    Long2ObjectMap<List<NodeConnection>> chunks = new Long2ObjectRBTreeMap<>();
    int nodeID = 0;

    public WireConnectionManager(InfrastructureSavedData sd, Level level) {
        this.sd = sd;
        this.level = level;
    }

    public void buildCircuit(CircuitBuilder builder) {
        electrocutions.clear();
        AtomicInteger electrocutionNodeID = new AtomicInteger();
        int cutElectrocutionNodeID = 0;
        Map<NodeConnection, List<CutWireEntry>> electrocutionCuts = new Object2ObjectOpenHashMap<>();
        if (CEEConfigs.server().enableElectrocution.get())
            for (Entity entity : ((ServerLevel) level).getAllEntities()) {
                if (!(entity instanceof Mob || entity instanceof Player p))
                    continue;
                if (entity instanceof ServerPlayer p && (p.gameMode.getGameModeForPlayer() == GameType.CREATIVE || p.gameMode.getGameModeForPlayer() == GameType.SPECTATOR))
                    continue;
                AABB bb1 = entity.getBoundingBox();
                int xChunkPos = entity.chunkPosition().x;
                int zChunkPos = entity.chunkPosition().z;
                List<NodeConnection> wiresToCheck = new LinkedList<>();
                List<NodeConnection> addedWires = chunks.get(DataPacker.pack(xChunkPos + 1, zChunkPos + 1));
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
                for (NodeConnection connection : wiresToCheck) {

                    double lastWireVoltage = Math.max(Math.abs(sd.getVoltageAt(connection.node1())), Math.abs(sd.getVoltageAt(connection.node2())));
                    double unsafeDistance = Mth.clamp(Math.log(0.006d * lastWireVoltage + 1) / 8.1d + 0.000011d * lastWireVoltage -0.6d, 0.1, 3);
                    double unsafeDistanceSqr = unsafeDistance * unsafeDistance;
                    AABB bb2 = lastWireVoltage < 1000 ? wireBBs.get(connection) : wireBBs.get(connection).inflate(unsafeDistance);

                    if (bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX &&
                            bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY &&
                            bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ) {

                        Vec3 pos1 = sd.getNodePosition(connection.node1());
                        Vec3 pos2 = sd.getNodePosition(connection.node2());
                        WireData wireData = sd.getConnectionData(connection);
                        if (wireData == null || wireData.wireType().maxInsulationVoltage() > lastWireVoltage)
                            continue;
                        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.wireType().getSag(), 0.5f);
                        Vec3 bestPoint = null;
                        float bestProgress = 0;
                        double bestResistance = 1e+11d;
                        for (int i = 0; i < points.size(); i++) {
                            Vec3 point = points.get(i);
                            double distanceX = Math.max(0, Math.max(bb1.minX - point.x, point.x - bb1.maxX));
                            double distanceY = Math.max(0, Math.max(bb1.minY - point.y, point.y - bb1.maxY));
                            double distanceZ = Math.max(0, Math.max(bb1.minZ - point.z, point.z - bb1.maxZ));
                            double distanceSqr = (distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ);
                            if (distanceSqr < unsafeDistanceSqr) {
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
                                    ElectrocutionEntry ee = new ElectrocutionEntry(new AttachedNode(electrocutionNodeID.getAndIncrement(), "ElectrocutionNode"), new Object2DoubleArrayMap<>(16), new Object2ObjectArrayMap<>(16));
                                    if (entity.onGround()) {
                                        PositionedAttachedNode groundNode = new PositionedAttachedNode(electrocutionNodeID.getAndIncrement(), "ElectrocutionGroundNode", entity.position());
                                        ee.nodes.put(groundNode, 10);
                                        ee.positions().put(groundNode, entity.position());
                                        builder.connect(ee.centralNode, groundNode, ElectricalProperties.resistor(10));
                                        builder.ground(groundNode, 1 / 2333d);
                                    }
                                    return ee;
                                });
                            AttachedNode cutNode = new AttachedNode(cutElectrocutionNodeID, "CEEElectrocutionCut");
                            electrocutionEntry.nodes.put(cutNode, bestResistance);
                            electrocutionEntry.positions.put(cutNode, bestPoint);
                            electrocutionCuts.computeIfAbsent(connection, k -> new ArrayList<>()).add(new CutWireEntry(bestProgress, cutNode, null, null));
                            builder.connect(electrocutionEntry.centralNode, cutNode, ElectricalProperties.resistor(bestResistance));
                        }
                    }
                }
            }

        for (Map.Entry<NodeConnection, WireType> e : originalConnections.entrySet()) {
            NodeConnection connection = e.getKey();
            double resistance = MicroTickedSimulationTicker.getWireResistance(connection.node1(), connection.node2(), e.getValue());
            List<CutWireEntry> cuts = cutConnections.get(connection);
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
//                    builder.addNode(cut.node);
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
        chunks.clear();

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
        // It's slow but only done on reload
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

        for (Pair<NodeConnection, List<Vec3>> pointPair : allPoints) {
            ChunkPos prevChunk = null;
            for (Vec3 point : pointPair.getSecond()) {
                ChunkPos chunkPos = new ChunkPos(BlockPos.containing(point));
                if (chunkPos.equals(prevChunk))
                    continue;
                prevChunk = chunkPos;
                chunks.computeIfAbsent(DataPacker.pack(chunkPos.x, chunkPos.z), k -> new ArrayList<>()).add(pointPair.getFirst());
            }
        }
    }

    public void finish(SimulationResults results) {
        MicroTickedSimulationTicker.profiler.push("updateWireTemperature");

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
                double wholeWireResistance = MicroTickedSimulationTicker.getWireResistance(connection.node1(), connection.node2(), wireType);
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
                    else if (MicroTickedSimulationTicker.getWireResistance(longestWireToBreak.node1(), longestWireToBreak.node2(), wireType) < MicroTickedSimulationTicker.getWireResistance(connection.node1(), connection.node2(), wireType)) {
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
        MicroTickedSimulationTicker.profiler.popPush("showWireShorts");

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

        if (electrocutions.isEmpty() || !CEEConfigs.server().enableElectrocution.get()) {
            MicroTickedSimulationTicker.profiler.pop();
            return;
        }

        MicroTickedSimulationTicker.profiler.popPush("electrocute");

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

        MicroTickedSimulationTicker.profiler.pop();
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

        List<Long> chunkPositionToRemove = new LinkedList<>();
        chunks.forEach((packedChunkPos, connections) -> {
            connections.remove(connection);
            if (connections.isEmpty())
                chunkPositionToRemove.add(packedChunkPos);
        });

        for (long packedChunkPos : chunkPositionToRemove)
            chunks.remove(packedChunkPos);
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

        ChunkPos prevChunk = null;
        for (Vec3 point : points1) {
            ChunkPos chunkPos = new ChunkPos(BlockPos.containing(point));
            if (chunkPos.equals(prevChunk))
                continue;
            prevChunk = chunkPos;
            chunks.computeIfAbsent(DataPacker.pack(chunkPos.x, chunkPos.z), k -> new ArrayList<>()).add(connection);
        }
    }

    record CutWireEntry(Float point, AttachedNode node, AttachedNode neighbourNode, NodeConnection neighbourWire) { }

    record ElectrocutionEntry(AttachedNode centralNode, Object2DoubleMap<AttachedNode> nodes, Map<AttachedNode, Vec3> positions) { }

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
