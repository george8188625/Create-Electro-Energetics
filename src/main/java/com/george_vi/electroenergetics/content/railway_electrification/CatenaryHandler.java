package com.george_vi.electroenergetics.content.railway_electrification;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.UpdateElectricTrainSoundPacket;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CatenaryHandler {


    public static void addToGraph(AddToElectricGraphEvent event) {

        Map<Train, List<Pair<Pair<Float, TrainPantographEntry>, CatenaryConnection>>> catenaryConnections = new HashMap<>();
        Object2IntMap<CatenaryConnection> cutConnections = new Object2IntOpenHashMap<>();
        List<CatenaryConnection> allCatenaryConnections = event.sd.getAllCatenaryConnections();
        int i = -1;
        for (Train train : Create.RAILWAYS.trains.values()) {
            ICEETrainExtension trainExtension = (ICEETrainExtension)train;
            ElectricTrainData trainData = trainExtension.getElectricTrainData();

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

                float yaw = (float) (Mth.atan2(diffZ, diffX) * Mth.RAD_TO_DEG) + 180;
                float pitch = (float) (Mth.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * Mth.RAD_TO_DEG) * -1;

                Vec3 pivotPosition = carriage.isOnTwoBogeys() ? positionVec : VecHelper.lerp(0.5f, positionVec, coupledVec);

                PantographLoop:
                for (TrainPantographEntry pantograph : pantographs) {
                    if (!pantograph.active)
                        continue;

                    // Here, all active pantographs are checked if they touch a catenary wire.

                    Vec3 pantographPos = Vec3.atLowerCornerOf(pantograph.rotatedPos).add(pantograph.facingForward ? 0.5 : -0.5, pantograph.type.reach / 2 + 0.5f, 0);

                    pantographPos = VecHelper.rotate(pantographPos, -pitch, Direction.Axis.Z);
                    pantographPos = VecHelper.rotate(pantographPos, -yaw + 180, Direction.Axis.Y);

                    pantographPos = pivotPosition.add(pantographPos);
//                    event.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pantographPos.x, pantographPos.y, pantographPos.z, 3, 0, 0, 0, 0);

                    for (CatenaryConnection connection : allCatenaryConnections) {
                        Vec3 start = connection.getStartPos();
                        Vec3 end = connection.getEndingPos();

                        double t = 0;

                        Vec3 ab = end.subtract(start);
                        Vec3 ap = pantographPos.subtract(start);
                        double denom = ab.lengthSqr();
                        if (denom != 0) {
                            t = ap.dot(ab) / denom;
                            t = Mth.clamp(t, 0, 1);
                        }

                        Vec3 closest = VecHelper.lerp((float) t, start, end);

                        Vec3 distance = pantographPos.subtract(closest);
                        distance = VecHelper.rotate(distance, yaw + 180, Direction.Axis.Y);
                        distance = VecHelper.rotate(distance, -pitch, Direction.Axis.X);

                        if (Math.abs(distance.z()) < 2 && Math.abs(distance.x()) < 0.5 && Math.abs(distance.y()) < pantograph.type.reach / 2) {
                            pantograph.prevPos = pantograph.pos;
                            pantograph.pos = closest;
                            catenaryConnections.computeIfAbsent(train, k -> new ArrayList<>())
                                    .add(Pair.of(Pair.of((float) t, pantograph), connection));
                            cutConnections.compute(connection, (k, v) -> v == null ? 1 : v + 1);
                            continue PantographLoop;
                        }
                    }
                    pantograph.prevPos = pantograph.pos;
                    pantograph.pos = null;
                }
            }


        }

        i = 0;
        for (CatenaryConnection connection : allCatenaryConnections) {
            if (cutConnections.containsKey(connection)) {

                // Catenary wires are cut and a node is inserted, if a pantograph is in the middle, and shipped off to the simulator.

                List<Pair<Pair<Float, TrainPantographEntry>, Train>> connectedTrains = new ArrayList<>();
                for (Map.Entry<Train, List<Pair<Pair<Float, TrainPantographEntry>, CatenaryConnection>>> e : catenaryConnections.entrySet()) {
                    Train train = e.getKey();
                    for (Pair<Pair<Float, TrainPantographEntry>, CatenaryConnection> e1 : e.getValue()) {
                        Pair<Float, TrainPantographEntry> progress = e1.getFirst();
                        CatenaryConnection con = e1.getSecond();
                        if (connection.equals(con))
                            connectedTrains.add(Pair.of(progress, train));
                    }
                }
                InWorldNode startNode = new InWorldNode(0, connection.pos1);
                InWorldNode endNode = new InWorldNode(0, connection.pos2);
                float totalProgress = 0.01f;
                double totalResistance = SimulationTicker.getWireResistance(startNode, endNode, CEEWireTypes.COPPER.get());
                Node lastNode = startNode;


                for (Pair<Pair<Float, TrainPantographEntry>, Train> e : connectedTrains.stream().sorted(Comparator.comparingDouble(p -> p.getFirst().getFirst())).toList()) {
                    float progress = e.getFirst().getFirst();
                    Train train = e.getSecond();
                    ICEETrainExtension trainExtension = (ICEETrainExtension)train;
                    ElectricTrainData trainData = trainExtension.getElectricTrainData();

                    double resistance = totalResistance * (progress - totalProgress);
                    AttachedNode pantographNode = e.getFirst().getSecond().node;
                    event.builder.addNode(pantographNode);
                    i++;
                    event.builder.connect(lastNode, pantographNode, ElectricalProperties.resistor(resistance));
                    lastNode = pantographNode;
                }
                event.builder.connect(lastNode, endNode, ElectricalProperties.resistor(totalResistance * (1.01 - totalProgress)));

            } else {
                InWorldNode node1 = new InWorldNode(0, connection.pos1);
                InWorldNode node2 = new InWorldNode(0, connection.pos2);
                event.builder.connect(node1, node2, ElectricalProperties.resistor(SimulationTicker.getWireResistance(node1, node2, CEEWireTypes.COPPER.get())));
            }
        }

        // Create train circuit, all pantographs merge into one node, that node is connected to ground through a resistance
        i = 0;
        for (Train train : Create.RAILWAYS.trains.values()) {
            ICEETrainExtension trainExtension = (ICEETrainExtension)train;
            ElectricTrainData trainData = trainExtension.getElectricTrainData();
            if (trainData.pantographs.isEmpty() && trainData.accumulatorCharge <= 0.001)
                continue;
            double trainSpeed = Math.abs(train.speed);
            float acceleration = (float) (trainSpeed - trainData.lastSpeed);

            AttachedNode groundNode = trainData.groundNode = new AttachedNode(i, "CEEPantographGroundNode");
            AttachedNode trainNode = trainData.trainNode = new AttachedNode(i, "CEETrainNode");
            event.builder.addNode(groundNode);
            event.builder.addNode(trainNode);
            event.builder.ground(groundNode, 10);
            double trainResistance = Math.abs(train.speed) > 0.01 ?
                    acceleration > 0.001 ? CEEConfigs.server().resistanceValues.electricTrainAccelerationResistance.get() : CEEConfigs.server().resistanceValues.electricTrainCruiseResistance.get() : 9999;
            if (trainData.accumulatorCharge < trainData.accumulators)
                trainResistance = 1 / (1 / CEEConfigs.server().resistanceValues.electricTrainAccelerationResistance.get() + 1 / trainResistance);
            event.builder.connect(groundNode, trainNode, ElectricalProperties.resistor(trainResistance));

            for (TrainPantographEntry pe : trainExtension.getElectricTrainData().pantographs)
                event.builder.connect(pe.node, trainNode, ElectricalProperties.resistor(CEEConfigs.server().resistanceValues.wireResistance.get()));
        }

    }

    public static void finishSimulation(FinishElectricSimulationEvent event) {
        for (Train train : Create.RAILWAYS.trains.values()) {

            ICEETrainExtension trainExtension = (ICEETrainExtension)train;
            ElectricTrainData trainData = trainExtension.getElectricTrainData();

            AttachedNode groundNode = trainData.groundNode;
            AttachedNode trainNode = trainData.trainNode;
            double voltage;
            if (groundNode != null && trainNode != null)
                voltage = Math.abs(event.results.getVoltageAt(groundNode, trainNode));
            else
                voltage = 0;
            boolean active = trainData.hasCreativeSource || voltage > CEEConfigs.server().voltageValues.trainMinVoltage.get();
            double trainSpeed = Math.abs(train.speed);


            // Sparks
            for (TrainPantographEntry pantograph : trainData.pantographs) {
                double current = Math.abs(event.results.getCurrentThrough(trainNode, pantograph.node));

                if (pantograph.pos == null && pantograph.prevPos != null && pantograph.lastCurrent > 1e-2d) {
                    CatnipServices.NETWORK.sendToClientsAround(event.level, pantograph.prevPos,
                            40, new SendSparkPacket(pantograph.prevPos, SendSparkPacket.SparkSize.MEDIUM));
                } else if (pantograph.pos != null && pantograph.prevPos == null && current > 1e-2d) {
                    CatnipServices.NETWORK.sendToClientsAround(event.level, pantograph.pos,
                            40, new SendSparkPacket(pantograph.pos, SendSparkPacket.SparkSize.MEDIUM));
                } else if (pantograph.pos != null && current > 7 && CEEConfigs.server().trainValues.highSpeedPantographSparks.get() != 0 &&
                    Math.abs(train.speed) > CEEConfigs.server().trainValues.highSpeedPantographSparks.get() && event.level.random.nextFloat() > 0.95) {
                    CatnipServices.NETWORK.sendToClientsAround(event.level, pantograph.pos,
                            40, new SendSparkPacket(pantograph.pos, SendSparkPacket.SparkSize.MEDIUM));
                } else if (pantograph.pos != null && Math.abs(train.speed) > 0.1 && CEEConfigs.server().trainValues.winterPantographSparks.get() &&
                        event.level.random.nextFloat() > 0.98) {
                    BlockPos pos = BlockPos.containing(pantograph.pos);
                    if (event.level.isLoaded(pos) && event.level.getBiome(pos).value().getBaseTemperature() < 0.15f)
                        CatnipServices.NETWORK.sendToClientsAround(event.level, pantograph.pos,
                                40, new SendSparkPacket(pantograph.pos, SendSparkPacket.SparkSize.MEDIUM));
                }
                pantograph.lastCurrent = current;
            }

            if (!active) {
                if (trainData.accumulatorCharge > 0) {
                    if (trainSpeed > 0.001)
                        trainData.accumulatorCharge = Math.max(0d, trainData.accumulatorCharge - 1d / CEEConfigs.server().trainValues.ticksPerAccumulatorOnTrain.get());
                    active = true;
                }
            } else
                if (trainData.accumulatorCharge < trainData.accumulators)
                    trainData.accumulatorCharge = Math.min(trainData.accumulators, trainData.accumulatorCharge + 1d / CEEConfigs.server().trainValues.ticksPerAccumulatorChargeOnTrain.get());

            Map<Integer, Vec3> positions = new HashMap<>();
            for (Carriage carriage : train.carriages) {
                if (((IPantographList)carriage).hasElectricMotor()) {
                    Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(event.level);

                    int carriageIndex = train.carriages.indexOf(carriage);
                    if (carriage.isOnTwoBogeys()) {
                        positions.put(carriageIndex + 1, dce.trailingAnchor());
                        positions.put(-carriageIndex - 1, dce.leadingAnchor());
                    } else
                        positions.put(carriageIndex + 1, dce.trailingAnchor());
                }
            }
            float acceleration = (float) (trainSpeed - trainData.lastSpeed);

            trainData.lastSpeed = trainSpeed;
            for (Map.Entry<Integer, Vec3> ce : positions.entrySet()) {
                Integer carriageID = ce.getKey();
                Vec3 pos = ce.getValue();

                if (pos == null)
                    continue;

                CatnipServices.NETWORK.sendToClientsAround(event.level, pos,
                        100, new UpdateElectricTrainSoundPacket(train.id, carriageID, (float) trainSpeed, acceleration, active, CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getId(trainExtension.getSoundType())));
            }
            if (active)
                if (train.fuelTicks <= 1) {
                    train.fuelTicks = 10;
                }

        }
    }
}
