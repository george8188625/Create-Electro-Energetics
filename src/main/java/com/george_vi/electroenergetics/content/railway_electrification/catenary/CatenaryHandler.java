package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.UpdateElectricTrainSoundPacket;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.AttachedNode;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CatenaryHandler {

    static List<Pair<Train, Couple<AttachedNode>>> trains = new ArrayList<>();
    static Map<Train, Double> trainSpeeds = new HashMap<>();

    public static void addToGraph(AddToElectricGraphEvent event) {
        trains.clear();

        Map<Train, List<Pair<Float, Couple<BlockPos>>>> catenaryConnections = new HashMap<>();
        Map<Couple<BlockPos>, Integer> cutConnections = new HashMap<>();
        List<Couple<BlockPos>> allCatenaryConnections = event.sd.getAllCatenaryConnections();

        for (Train train : Create.RAILWAYS.trains.values()) {
            for (Carriage carriage : train.carriages) {
                List<TrainPantographEntry> pantographs = ((IPantographList)carriage).getPantographList();
                if (carriage.presentInMultipleDimensions() ||
                    !carriage.getPresentDimensions().getFirst().equals(event.level.dimension()) ||
                    !((IPantographList)carriage).hasElectricMotor())
                    continue;

                Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(carriage.getPresentDimensions().getFirst());
                Vec3 positionVec = dce.rotationAnchors.getFirst();
                Vec3 coupledVec = dce.rotationAnchors.getSecond();
                if (positionVec == null || coupledVec == null)
                    continue;

                double diffX = positionVec.x - coupledVec.x;
                double diffY = positionVec.y - coupledVec.y;
                double diffZ = positionVec.z - coupledVec.z;

                float yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI) + 180;
                float pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI) * -1;

                Vec3 pivotPosition = carriage.isOnTwoBogeys() ? positionVec : VecHelper.lerp(0.5f, positionVec, coupledVec);

                for (TrainPantographEntry pantograph : pantographs) {
                    if (!pantograph.active())
                        continue;

                    // Here, all active pantographs are checked if they touch a catenary wire.

                    Vec3 pantographPos = Vec3.atLowerCornerOf(pantograph.rotatedPos()).add(pantograph.facingForward() ? 0.5 : -0.5, 1.5, 0);

                    pantographPos = VecHelper.rotate(pantographPos, -pitch, Direction.Axis.Z);
                    pantographPos = VecHelper.rotate(pantographPos, -yaw + 180, Direction.Axis.Y);

                    pantographPos = pivotPosition.add(pantographPos);
//                    event.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pantographPos.x, pantographPos.y, pantographPos.z, 3, 0, 0, 0, 0);
                    for (Couple<BlockPos> connection : allCatenaryConnections) {
                        Vec3 start = connection.getFirst().getBottomCenter();
                        Vec3 end = connection.getSecond().getBottomCenter();

                        double t = 0;

                        Vec3 ab = end.subtract(start);
                        Vec3 ap = pantographPos.subtract(start);
                        double denom = ab.lengthSqr();
                        if (denom != 0) {
                            t = ap.dot(ab) / denom;
                            t = Math.max(0, Math.min(1, t));
                        }

                        Vec3 closest = VecHelper.lerp((float) t, start, end);

                        Vec3 distance = pantographPos.subtract(closest);
                        distance = VecHelper.rotate(distance, yaw + 180, Direction.Axis.Y);
                        distance = VecHelper.rotate(distance, -pitch, Direction.Axis.X);

                        if (!(Math.abs(distance.z()) > 2) && !(Math.abs(distance.x()) > 0.5) && !(Math.abs(distance.y()) > 1.75)) {
                            catenaryConnections.computeIfAbsent(train, k -> new ArrayList<>());
                            catenaryConnections.get(train).add(Pair.of((float) t, connection));
                            cutConnections.compute(connection, (k, v) -> v == null ? 1 : v + 1);
                            break;
                        }
                    }
                }
            }
        }

        int i = 0;
        Map<Train, List<AttachedNode>> trainPantographs = new HashMap<>();
        for (Couple<BlockPos> connection : allCatenaryConnections) {
            if (cutConnections.containsKey(connection)) {

                // Catenary wires are cut, and a node is inserted, if a pantograph is in the middle, and shipped off to the simulator.

                List<Pair<Float, Train>> connectedTrains = new ArrayList<>();
                for (Map.Entry<Train, List<Pair<Float, Couple<BlockPos>>>> e : catenaryConnections.entrySet()) {
                    Train train = e.getKey();
                    for (Pair<Float, Couple<BlockPos>> e1 : e.getValue()) {
                        float progress = e1.getFirst();
                        Couple<BlockPos> con = e1.getSecond();
                        if (connection.equals(con))
                            connectedTrains.add(Pair.of(progress, train));
                    }
                }
                InWorldNode startNode = new InWorldNode(0, connection.getFirst());
                InWorldNode endNode = new InWorldNode(0, connection.getSecond());
                float totalProgress = 0.01f;
                double totalResistance = SimulationTicker.getWireResistance(startNode, endNode, CEEWireTypes.COPPER.get());
                Node lastNode = startNode;


                for (Pair<Float, Train> e : connectedTrains.stream().sorted(Comparator.comparingDouble(Pair::getFirst)).toList()) {
                    float progress = e.getFirst();
                    Train train = e.getSecond();
                    double resistance = totalResistance * (progress - totalProgress);
                    AttachedNode pantographNode = new AttachedNode(i, "CeePantographNode");
                    event.builder.addNode(pantographNode);
                    if (!trainPantographs.containsKey(train))
                        trainPantographs.put(train, new ArrayList<>(List.of(pantographNode)));
                    else
                        trainPantographs.get(train).add(pantographNode);
                    i++;
                    event.builder.connect(lastNode, pantographNode, ElectricalProperties.resistor(resistance));
                    lastNode = pantographNode;
                }
                event.builder.connect(lastNode, endNode, ElectricalProperties.resistor(totalResistance * (1.01 - totalProgress)));

            } else {
                InWorldNode node1 = new InWorldNode(0, connection.getFirst());
                InWorldNode node2 = new InWorldNode(0, connection.getSecond());
                event.builder.connect(node1, node2, ElectricalProperties.resistor(SimulationTicker.getWireResistance(node1, node2, CEEWireTypes.COPPER.get())));
            }
        }

        // Create train circuit, all pantographs merge into one node, that node is connected to ground through a resistance
        i = 0;
        for (Map.Entry<Train, List<AttachedNode>> e : trainPantographs.entrySet()) {
            Train train = e.getKey();
            double trainSpeed = Math.abs(train.speed);
            float acceleration = (float) (trainSpeed - trainSpeeds.getOrDefault(train, trainSpeed));
            AttachedNode groundNode = new AttachedNode(i, "CeePantographGroundNode");
            AttachedNode trainNode = new AttachedNode(i, "CEETrainNode");
            event.builder.addNode(groundNode);
            event.builder.addNode(trainNode);
            event.builder.ground(groundNode, 10);
            event.builder.connect(groundNode, trainNode, ElectricalProperties.resistor(Math.abs(train.speed) > 0.01 ?
                    acceleration > 0.001 ? CEEConfigs.server().resistanceValues.electricTrainAccelerationResistance.get() : CEEConfigs.server().resistanceValues.electricTrainCruiseResistance.get() : 9999));

            for (AttachedNode node : e.getValue())
                event.builder.connect(node, trainNode, ElectricalProperties.resistor(CEEConfigs.server().resistanceValues.wireResistance.get()));
            trains.add(Pair.of(train, Couple.create(trainNode, groundNode)));
        }

    }

    public static void finishSimulation(FinishElectricSimulationEvent event) {
        List<Train> visitedTrains = new ArrayList<>();
        for (Pair<Train, Couple<AttachedNode>> e : trains) {
            Train train = e.getFirst();
            visitedTrains.add(train);

            AttachedNode node1 = e.getSecond().getFirst();
            AttachedNode node2 = e.getSecond().getSecond();

            double voltage = Math.abs(event.results.getVoltageAt(node1, node2));
            boolean active = voltage > CEEConfigs.server().trainMinVoltage.get();

            double trainSpeed = Math.abs(train.speed);

            Map<Integer, Vec3> positions = new HashMap<>();
            for (Carriage carriage : train.carriages) {
                if (((IPantographList)carriage).hasElectricMotor()) {
                    Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(event.level);

                    if (carriage.isOnTwoBogeys()) {
                        positions.put(carriage.id, dce.trailingAnchor());
                        positions.put(-carriage.id, dce.leadingAnchor());
                    } else
                        positions.put(carriage.id, dce.trailingAnchor());
                }
            }
            float acceleration = (float) (trainSpeed - trainSpeeds.getOrDefault(train, trainSpeed));

            trainSpeeds.put(train, trainSpeed);
            for (Map.Entry<Integer, Vec3> ce : positions.entrySet()) {
                Integer carriageID = ce.getKey();
                Vec3 pos = ce.getValue();

                CatnipServices.NETWORK.sendToClientsAround(event.level, pos,
                        100, new UpdateElectricTrainSoundPacket(train.id, carriageID, pos, (float) trainSpeed, acceleration, active));
            }
            if (active)
                if (train.fuelTicks <= 1) {
                    train.fuelTicks = 10;
                }

        }

        // When the train is disconnected from power, the code above isn't executed, this makes sure the train stops its sounds.

        for (Train train : trainSpeeds.keySet().stream().toList()) {
            if (visitedTrains.contains(train))
                continue;

            Map<Integer, Vec3> positions = new HashMap<>();
            for (Carriage carriage : train.carriages) {
                if (((IPantographList)carriage).hasElectricMotor()) {
                    Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(event.level);

                    if (carriage.isOnTwoBogeys()) {
                        positions.put(carriage.id, dce.trailingAnchor());
                        positions.put(carriage.id, dce.leadingAnchor());
                    } else
                        positions.put(carriage.id, dce.trailingAnchor());
                    break;
                }
            }

            for (Map.Entry<Integer, Vec3> ce : positions.entrySet()) {
                Integer carriageID = ce.getKey();
                Vec3 pos = ce.getValue();

                CatnipServices.NETWORK.sendToClientsAround(event.level, pos,
                        100, new UpdateElectricTrainSoundPacket(train.id, carriageID, pos, 0, 0, false));
            }

            trainSpeeds.remove(train);
        }
    }
}
