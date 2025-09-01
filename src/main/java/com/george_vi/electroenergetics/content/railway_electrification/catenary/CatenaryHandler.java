package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.UpdateElectricTrainSoundPacket;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CatenaryHandler {

    static List<Pair<Train, Couple<Node>>> trains = new ArrayList<>();
    static Map<Train, Double> trainSpeeds = new HashMap<>();

    public static void addToGraph(AddToElectricGraphEvent event) {
        trains.clear();

        Map<Train, List<Pair<Float, Couple<BlockPos>>>> catenaryConnections = new HashMap<>();
        Map<Couple<BlockPos>, Integer> cutConnections = new HashMap<>();
        List<Couple<BlockPos>> allCatenaryConnections = event.sd.getAllCatenaryConnections();

        for (Train train : Create.RAILWAYS.trains.values()) {
            for (Carriage carriage : train.carriages) {
                List<Pair<BlockPos, Boolean>> pantographs = ((IPantographList)carriage).getPantographList();
                if (carriage.presentInMultipleDimensions() ||
                    !carriage.getPresentDimensions().getFirst().equals(event.level.dimension()))
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

                for (Pair<BlockPos, Boolean> pantograph : pantographs) {


                    Vec3 pantographPos = Vec3.atLowerCornerOf(pantograph.getFirst()).add(pantograph.getSecond() ? 0.5 : -0.5, 1.5, 0);

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

                        if (!(Math.abs(distance.z()) > 2) && !(Math.abs(distance.x()) > 0.5) && !(Math.abs(distance.y()) > 1)) {
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
        for (Couple<BlockPos> connection : allCatenaryConnections) {
            if (cutConnections.containsKey(connection)) {
                List<Pair<Float, Train>> connectedTrains = new ArrayList<>();
                for (Map.Entry<Train, List<Pair<Float, Couple<BlockPos>>>> e : catenaryConnections.entrySet()) {
                    Train train = e.getKey();
                    for (Pair<Float, Couple<BlockPos>> e1 : e.getValue()) {
                        float progress = e1.getFirst();
                        Couple<BlockPos> con = e1.getSecond();
                        if (connection.equals(con)) {
                            connectedTrains.add(Pair.of(progress, train));
                        }
                    }
                }
                Node startNode = new Node(0, connection.getFirst());
                Node endNode = new Node(0, connection.getSecond());
                float totalProgress = 0.01f;
                double totalResistance = SimulationTicker.getWireResistance(startNode, endNode, CEEWireTypes.STANDARD.get());
                Node lastNode = startNode;


                for (Pair<Float, Train> e : connectedTrains.stream().sorted(Comparator.comparingDouble(Pair::getFirst)).toList()) {
                    float progress = e.getFirst();
                    Train train = e.getSecond();
                    double resistance = totalResistance * (progress - totalProgress);
                    Node trainNode = event.addNode("CeePantographNode", i, BlockPos.ZERO);
                    Node groundNode = event.addGroundedNode("CeePantographGroundNode", i, BlockPos.ZERO);
                    i++;

                    double trainSpeed = Math.abs(train.speed);
                    float acceleration = (float) (trainSpeed - trainSpeeds.getOrDefault(train, trainSpeed));

                    event.connect(groundNode, trainNode, ElectricalProperties.resistor(Math.abs(train.speed) > 0.01 ?
                            acceleration > 0.001 ? CEEConfigs.server().resistanceValues.electricTrainAccelerationResistance.get() : CEEConfigs.server().resistanceValues.electricTrainCruiseResistance.get() : 9999));
                    event.connect(lastNode, trainNode, ElectricalProperties.resistor(resistance));
                    trains.add(Pair.of(train, Couple.create(trainNode, groundNode)));
                    lastNode = trainNode;
                }

                event.connect(lastNode, endNode, ElectricalProperties.resistor(totalResistance * (1.01 - totalProgress)));

            } else {
                Node node1 = new Node(0, connection.getFirst());
                Node node2 = new Node(0, connection.getSecond());
                event.connect(node1, node2, ElectricalProperties.resistor(SimulationTicker.getWireResistance(node1, node2, CEEWireTypes.STANDARD.get())));
            }
        }

    }

    public static void finishSimulation(FinishElectricSimulationEvent event) {
        List<Train> visitedTrains = new ArrayList<>();
        for (Pair<Train, Couple<Node>> e : trains) {
            Train train = e.getFirst();
            visitedTrains.add(train);

            Node node1 = e.getSecond().getFirst();
            Node node2 = e.getSecond().getSecond();

            double voltage = Math.abs(event.results.getVoltageAt(node1) - event.results.getVoltageAt(node2));
            boolean active = voltage > 1990;

            double trainSpeed = Math.abs(train.speed);

            Carriage carriage = train.currentlyBackwards ? train.carriages.getLast() : train.carriages.getFirst();
            Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(event.level);
            Vec3 pos = train.currentlyBackwards ? dce.leadingAnchor() : dce.trailingAnchor();
            float acceleration = (float) (trainSpeed - trainSpeeds.getOrDefault(train, trainSpeed));

            trainSpeeds.put(train, trainSpeed);

            CatnipServices.NETWORK.sendToClientsAround(event.level, pos,
                    100, new UpdateElectricTrainSoundPacket(train.id, pos, (float) trainSpeed, acceleration, active));
            if (active)
                if (train.fuelTicks == 0) {
                    train.fuelTicks = 20;
                }

        }

        // When the train is disconnected from power, the code above isn't executed, this makes sure the train stops its sounds.

        for (Train train : trainSpeeds.keySet().stream().toList()) {
            if (visitedTrains.contains(train))
                continue;
            Carriage carriage = train.currentlyBackwards ? train.carriages.getLast() : train.carriages.getFirst();
            Vec3 pos = carriage.getDimensional(event.level).positionAnchor;

            CatnipServices.NETWORK.sendToClientsAround(event.level, pos,
                    100, new UpdateElectricTrainSoundPacket(train.id, pos, 0, 0, false));

            trainSpeeds.remove(train);
        }
    }
}
