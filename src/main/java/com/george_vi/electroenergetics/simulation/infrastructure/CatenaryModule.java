package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.SyncTrainGaugeDataPacket;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.UpdateElectricTrainSoundPacket;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CatenaryModule {
    private static final double GAUGE_SYNC_THRESHOLD = 0.05; // 5% change threshold for network sync

    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireSimulationState levelWireSimulationState;
    private final Set<Train> allTrains = new HashSet<>();

    public CatenaryModule(InfrastructureSavedData sd, ServerLevel level, WireSimulationState wireSimulationState) {
        this.sd = sd;
        this.level = level;
        this.levelWireSimulationState = wireSimulationState;
    }

    public void buildCircuit(CircuitBuilder builder) {
        int id = 0;
        for (Train train : Create.RAILWAYS.trains.values()) {
            allTrains.add(train);
            ICEETrainExtension trainExtension = (ICEETrainExtension)train;
            ElectricTrainData trainData = trainExtension.getElectricTrainData();
            boolean connected = false;
            for (Carriage carriage : train.carriages) {
                List<TrainPantographEntry> pantographs = ((IPantographList)carriage).electroEnergetics$getPantographList();
                if (carriage.presentInMultipleDimensions() ||
                        !carriage.getPresentDimensions().getFirst().equals(level.dimension()) ||
                        !((IPantographList)carriage).electroEnergetics$hasElectricMotor())
                    continue;

                Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(carriage.getPresentDimensions().getFirst());
                Vec3 positionVec = dce.rotationAnchors.getFirst();
                Vec3 coupledVec = dce.rotationAnchors.getSecond();
                //noinspection ConstantValue
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

                    Vec3 pantographPos = Vec3.atLowerCornerOf(pantograph.rotatedPos).add((pantograph.facingForward ? 1 : -1) * pantograph.type.backOffset, pantograph.type.reach / 2 + pantograph.type.topOffset, 0);

                    pantographPos = VecHelper.rotate(pantographPos, -pitch, Direction.Axis.Z);
                    pantographPos = VecHelper.rotate(pantographPos, -yaw + 180, Direction.Axis.Y);

                    pantographPos = pivotPosition.add(pantographPos);
//                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pantographPos.x, pantographPos.y, pantographPos.z, 3, 0, 0, 0, 0);

                    long section = SectionPos.asLong(Mth.floor(pantographPos.x) >> 4, Mth.floor(pantographPos.y) >> 4, Mth.floor(pantographPos.z) >> 4);
                    for (ConnectionEntry connectionEntry : levelWireSimulationState.getConnectionsInSection(section).values()) {
                        if (connectionEntry.wireData.wireType().getSag() != 0 && !(connectionEntry.wireData instanceof CatenaryConnectionData))
                            continue;
                        Vec3 start = connectionEntry.pos1;
                        Vec3 end = connectionEntry.pos2;

                        float t = 0;

                        Vec3 ab = end.subtract(start);
                        Vec3 ap = pantographPos.subtract(start);
                        double denom = ab.lengthSqr();
                        if (denom != 0) {
                            t = (float) (ap.dot(ab) / denom);
                            t = Mth.clamp(t, 0, 1);
                        }

                        Vec3 closest = VecHelper.lerp(t, start, end);

                        Vec3 distance = pantographPos.subtract(closest);
                        distance = VecHelper.rotate(distance, yaw + 180, Direction.Axis.Y);
                        distance = VecHelper.rotate(distance, -pitch, Direction.Axis.X);
                        float xTol = (float) ((distance.y + pantograph.type.reach / 2) * 0.2f + 0.125f);
                        if (Math.abs(distance.z()) < pantograph.type.sidewaysReach && Math.abs(distance.x()) < xTol && Math.abs(distance.y()) < pantograph.type.reach / 2) {

                            // If we have a handle, but it's from a different dimension, then invalidate it
                            // (we'll create a new one)
                            if (trainData.wireCutHandle != null && trainData.connectedWireState != levelWireSimulationState) {
                                trainData.connectedWireState.invalidateHandle(trainData.wireCutHandle);
                                trainData.wireCutHandle = null;
                            }

                            if (trainData.wireCutHandle == null) {
                                trainData.connectedWireState = levelWireSimulationState;
                                trainData.wireCutHandle = trainData.connectedWireState.createHandle("ElectricTrain");
                            }

                            pantograph.prevPos = pantograph.pos;
                            pantograph.pos = closest;

                            // If not connected, connect
                            if (pantograph.node == null) {
                                pantograph.node = trainData.connectedWireState.createCut(trainData.wireCutHandle, connectionEntry, t);
                                pantograph.onConnection = connectionEntry;
                            }
                            // If connected to another within this dimension
                            else if (trainData.connectedWireState == levelWireSimulationState)
                            {
                                if (pantograph.onConnection != connectionEntry ||
                                        !trainData.connectedWireState.cutExists(trainData.wireCutHandle, pantograph.node)) {
                                    trainData.connectedWireState.removeCut(trainData.wireCutHandle, pantograph.node);
                                    pantograph.node = levelWireSimulationState.createCut(trainData.wireCutHandle, connectionEntry, t);

                                    pantograph.onConnection = connectionEntry;
                                    trainData.connectedWireState = levelWireSimulationState;
                                }
                                // Continue being connected
                                else {
                                    trainData.connectedWireState.relocateCut(trainData.wireCutHandle, pantograph.node, t);
                                }
                            }
                            connected = true;
//                            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, closest.x, closest.y, closest.z, 3, 0, 0, 0, 0);
                            continue PantographLoop;
                        }
                    }
                    pantograph.prevPos = pantograph.pos;
                    pantograph.pos = null;
                    if (trainData.wireCutHandle != null && pantograph.node != null)
                        trainData.connectedWireState.removeCut(trainData.wireCutHandle, pantograph.node);

                }
            }

            // Only modify properties if the train is being powered by a source in this dimension, otherwise we end up
            // resetting the train properties even if it's being powered in a different dimension.
            if(trainData.connectedWireState != levelWireSimulationState)
                continue;

            if (!connected || (trainData.pantographs.isEmpty() && trainData.accumulatorCharge <= 0.001)) {
                trainData.groundNode = null;
                trainData.trainNode = null;

                continue;
            }

            double trainSpeed = Math.abs(train.speed);
            float acceleration = (float) (trainSpeed - trainData.lastSpeed);

            // This needs to happen in this method as the train speed is updated after it.
            trainData.lastSpeed = trainSpeed;

            AttachedNode groundNode = trainData.groundNode = new AttachedNode(id, "CEEPantographGroundNode");
            AttachedNode trainNode = trainData.trainNode = new AttachedNode(id, "CEETrainNode");
            builder.addNode(groundNode);
            builder.addNode(trainNode);
            builder.ground(groundNode, 10);

            double power = 0.1;
            if (Math.abs(train.speed) > 0.01)
                power = acceleration > 0.001
                        ? CEEConfigs.server().resistanceValues.electricTrainAccelerationPowerConsumption.get()
                        : CEEConfigs.server().resistanceValues.electricTrainCruisePowerConsumption.get();

            if (trainData.accumulatorCharge < trainData.accumulators)
                power += CEEConfigs.server().resistanceValues.electricTrainAccelerationPowerConsumption.get();

            // P = V*V/R
            // P*R = V*V
            // R = V*V/P

            double lastVoltage = Math.abs(trainData.lastVoltage) < 1 ? 3000 : trainData.lastVoltage;
            double trainResistance = lastVoltage * lastVoltage / power;

            builder.connect(groundNode, trainNode, ElectricalProperties.resistor(trainResistance));

            for (TrainPantographEntry pe : trainExtension.getElectricTrainData().pantographs)
                if (pe.active && pe.node != null)
                    builder.connect(pe.node, trainNode, ElectricalProperties.resistor(CEEConfigs.server().resistanceValues.wireResistance.get()));
            id++;
        }

        Iterator<Train> trainIterator = allTrains.iterator();
        while (trainIterator.hasNext()) {
            Train train = trainIterator.next();
            if (!Create.RAILWAYS.trains.containsKey(train.id) && ((ICEETrainExtension)train).getElectricTrainData().wireCutHandle != null) {
                var trainData = ((ICEETrainExtension) train).getElectricTrainData();

                trainData.connectedWireState.invalidateHandle(trainData.wireCutHandle);
                trainData.wireCutHandle = null;
                trainIterator.remove();
            }
        }
    }

    public void finishSimulation(SimulationResults results) {
        for (Train train : Create.RAILWAYS.trains.values()) {

            ICEETrainExtension trainExtension = (ICEETrainExtension)train;
            ElectricTrainData trainData = trainExtension.getElectricTrainData();

            AttachedNode groundNode = trainData.groundNode;
            AttachedNode trainNode = trainData.trainNode;
            double voltage;
            if (groundNode != null && trainNode != null)
                voltage = Math.abs(results.getVoltageAt(groundNode, trainNode));
            else
                voltage = 0;

            // Store voltage for gauge displays on train contraptions
            trainData.lastVoltage = voltage;

            boolean active = trainData.hasCreativeSource || voltage > CEEConfigs.server().voltageValues.trainMinVoltage.get();
            double trainSpeed = train.derailed ? 0 : Math.abs(train.speed);

            // Calculate total current draw for ammeter displays
            double totalCurrent = 0;

            // Sparks
            for (TrainPantographEntry pantograph : trainData.pantographs) {
                if (pantograph.node == null || trainNode == null)
                    continue;
                double current = Math.abs(results.getCurrentThrough(trainNode, pantograph.node));
                totalCurrent += current;

                Vec3 sparkPos = pantographSparkPosition(level, train, pantograph, current);
                if (sparkPos != null)
                    CatnipServices.NETWORK.sendToClientsAround(level, sparkPos,
                            40, new SendSparkPacket(sparkPos, SendSparkPacket.SparkSize.MEDIUM));
                pantograph.lastCurrent = current;
            }

            // Store total current for ammeter displays on train contraptions
            trainData.displayCurrent = totalCurrent;

            // Sync voltage and current to clients for gauge displays (with throttling to reduce network traffic)
            // Only send when values change significantly or every 5 ticks minimum
            trainData.ticksSinceGaugeSync++;
            boolean significantVoltageChange = Math.abs(voltage - trainData.lastSyncedVoltage) > Math.max(voltage, trainData.lastSyncedVoltage) * GAUGE_SYNC_THRESHOLD;
            boolean significantCurrentChange = Math.abs(totalCurrent - trainData.lastSyncedCurrent) > Math.max(totalCurrent, trainData.lastSyncedCurrent) * GAUGE_SYNC_THRESHOLD;
            boolean shouldSync = trainData.ticksSinceGaugeSync >= 5 || significantVoltageChange || significantCurrentChange;

            if (shouldSync && !train.carriages.isEmpty()) {
                Carriage.DimensionalCarriageEntity firstCarriage = train.carriages.getFirst().getDimensionalIfPresent(level.dimension());
                if (firstCarriage != null && firstCarriage.entity != null) {
                    CarriageContraptionEntity entity = firstCarriage.entity.get();
                    if (entity != null) {
                        Vec3 trainPos = entity.position();
                        CatnipServices.NETWORK.sendToClientsAround(
                            level,
                            trainPos,
                            100,
                            new SyncTrainGaugeDataPacket(train.id, voltage, totalCurrent)
                        );
                        trainData.lastSyncedVoltage = voltage;
                        trainData.lastSyncedCurrent = totalCurrent;
                        trainData.ticksSinceGaugeSync = 0;
                    }
                }
            }

            if (!active) {
                if (trainData.accumulatorCharge > 0) {
                    if (trainSpeed > 0.001)
                        trainData.accumulatorCharge = Math.max(0d, trainData.accumulatorCharge - 1d / CEEConfigs.server().trainValues.ticksPerAccumulatorOnTrain.get());
                    active = true;
                }
            } else if (trainData.accumulatorCharge < trainData.accumulators)
                trainData.accumulatorCharge = Math.min(trainData.accumulators, trainData.accumulatorCharge + 1d / CEEConfigs.server().trainValues.ticksPerAccumulatorChargeOnTrain.get());

            Map<Integer, Vec3> positions = new HashMap<>();
            for (Carriage carriage : train.carriages) {
                if (((IPantographList)carriage).electroEnergetics$hasElectricMotor()) {
                    Carriage.DimensionalCarriageEntity dce = carriage.getDimensionalIfPresent(level.dimension());
                    if (dce == null)
                        continue;

                    int carriageIndex = train.carriages.indexOf(carriage);
                    if (carriage.isOnTwoBogeys()) {
                        positions.put(carriageIndex + 1, dce.trailingAnchor());
                        positions.put(-carriageIndex - 1, dce.leadingAnchor());
                    } else
                        positions.put(carriageIndex + 1, dce.trailingAnchor());
                }
            }
            float acceleration = (float) (trainSpeed - trainData.lastSpeed);

            for (Map.Entry<Integer, Vec3> ce : positions.entrySet()) {
                Integer carriageID = ce.getKey();
                Vec3 pos = ce.getValue();

                if (pos == null)
                    continue;

                CatnipServices.NETWORK.sendToClientsAround(level, pos,
                        100, new UpdateElectricTrainSoundPacket(train.id, carriageID, (float) trainSpeed, acceleration, active, CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getId(trainExtension.getSoundType())));
            }
            if (active)
                if (train.fuelTicks <= 1) {
                    train.fuelTicks = 10;
                }

        }
    }

    private static Vec3 pantographSparkPosition(ServerLevel level, Train train, TrainPantographEntry pantograph, double current) {
        if (pantograph.pos == null && pantograph.prevPos != null && pantograph.lastCurrent > 1e-2d) {
            return pantograph.prevPos;
        } else if (pantograph.pos != null && pantograph.prevPos == null && current > 1e-2d) {
            return pantograph.pos;
        } else if (pantograph.pos != null && current > 7) {
            if (CEEConfigs.server().trainValues.highSpeedPantographSparks.get() != 0 &&
                    Math.abs(train.speed) > CEEConfigs.server().trainValues.highSpeedPantographSparks.get() &&
                    level.random.nextFloat() < CEEConfigs.server().trainValues.highSpeedPantographSparkChance.get()) {
                return pantograph.pos;
            } else if (Math.abs(train.speed) > 0.1) {
                BlockPos pos = BlockPos.containing(pantograph.pos);
                if (CEEConfigs.server().trainValues.winterPantographSparks.get() &&
                        level.random.nextFloat() < CEEConfigs.server().trainValues.winterPantographSparkChance.get() &&
                        level.isLoaded(pos) && level.getBiome(pos).value().getBaseTemperature() < 0.15f) {
                    return pantograph.pos;
                } else if (CEEConfigs.server().trainValues.rainPantographSparks.get() &&
                        level.random.nextFloat() < CEEConfigs.server().trainValues.rainPantographSparkChance.get() &&
                        level.isLoaded(pos) && level.isRainingAt(pos)) {
                    return pantograph.pos;
                }
            }
        }
        return null;
    }
}
