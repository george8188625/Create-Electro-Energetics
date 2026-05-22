package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEDamageTypes;
import com.george_vi.electroenergetics.CEEMobEffects;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendQuadraticParticlesPacket;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNodeGenerator;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class WireElectrocutionModule {

    private final AttachedNodeGenerator electrocutionNodeGenerator = new AttachedNodeGenerator("Electrocution");
    private final AttachedNodeGenerator electrocutionGroundNodeGenerator = new AttachedNodeGenerator("ElectrocutionGround");
    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireSimulationState wireSimulationState;
    final Map<Entity, ElectrocutionEntry> electrocutions = new HashMap<>();

    DamageSource damageSource;
    DamageSource hvDamageSource;

    public WireElectrocutionModule(InfrastructureSavedData sd, ServerLevel level, WireSimulationState wireSimulationState) {
        this.sd = sd;
        this.level = level;
        this.wireSimulationState = wireSimulationState;
    }

    public void buildCircuit(CircuitBuilder builder) {
        if (!CEEConfigs.server().enableElectrocution.get())
            return;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Mob || entity instanceof ServerPlayer))
                continue;
            if (entity instanceof ServerPlayer p && (p.gameMode.getGameModeForPlayer() == GameType.CREATIVE || p.gameMode.getGameModeForPlayer() == GameType.SPECTATOR))
                continue;
            if (((LivingEntity) entity).hasEffect(CEEMobEffects.DIELECTRIC))
                continue;
            computeElectrocutionFor(entity, builder);
        }
    }
    private void computeElectrocutionFor(Entity entity, CircuitBuilder builder) {
        Map<ConnectionEntry, ElectrocutionPart> electrocutionParts = new HashMap<>();
        SectionPos.aroundAndAtBlockPos(entity.blockPosition(), s -> computeForWires(wireSimulationState.getConnectionsInSection(s), entity, electrocutionParts::put));

        ElectrocutionEntry prevEntry = electrocutions.get(entity);

        if (electrocutionParts.isEmpty()) {
            if (prevEntry != null)
                wireSimulationState.invalidateHandle(prevEntry.handle);
            electrocutions.remove(entity);
            return;
        }

        WireSimulationState.WireCutHandle handle = prevEntry != null ? prevEntry.handle : wireSimulationState.createHandle("Electrocuted " + entity.getName());

        AttachedNode centralNode = electrocutionNodeGenerator.newNode();
        AttachedNode groundNode = null;
        if (entity.onGround()) {
            groundNode = prevEntry == null || prevEntry.groundNode == null ? electrocutionGroundNodeGenerator.newNode() : prevEntry.groundNode;
            builder.connect(centralNode, groundNode, ElectricalProperties.resistor(10));
            builder.ground(groundNode, 1 / 2333d);
        }
        Map<ConnectionEntry, Pair<WireElectrocutionModule.ElectrocutionPart, AttachedNode>> nodes = new HashMap<>();
        electrocutionParts.forEach(((connectionEntry, e) -> {
            AttachedNode node;
            if (prevEntry != null) {
                var prevPair = prevEntry.nodes.get(connectionEntry);
                if (prevPair != null)
                    node = prevPair.getSecond();
                else
                    node = wireSimulationState.createCut(handle, connectionEntry, e.point);
            } else
                node = wireSimulationState.createCut(handle, connectionEntry, e.point);
            double resistance = connectionEntry.wireData.wireType().insulationResistance() + 1444;
            builder.connect(centralNode, node, ElectricalProperties.resistor(resistance));
            nodes.put(connectionEntry, Pair.of(e, node));
        }));

        ElectrocutionEntry electrocutionEntry = new ElectrocutionEntry(nodes, centralNode, groundNode, handle);
        electrocutions.put(entity, electrocutionEntry);
    }

    private void computeForWires(Map<InWorldNodeConnection, ConnectionEntry> wiresToCheck, Entity entity, BiConsumer<ConnectionEntry, ElectrocutionPart> out) {
        if (wiresToCheck == null)
            return;

        AABB bb1 = entity.getBoundingBox();

        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wiresToCheck.entrySet()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionEntry connectionEntry = e.getValue();

            AABB bb2 = connectionEntry.dangerousBB;
            double dangerousDistance = connectionEntry.dangerousDistance;

            if (!(bb1.minX <= bb2.maxX) || !(bb1.maxX >= bb2.minX) ||
                    !(bb1.minY <= bb2.maxY) || !(bb1.maxY >= bb2.minY) ||
                    !(bb1.minZ <= bb2.maxZ) || !(bb1.maxZ >= bb2.minZ))
                continue;

            WireData wireData = connectionEntry.wireData;
            if (!connectionEntry.isOvervolted)
                continue;

            List<Vec3> points = connectionEntry.points;
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
                out.accept(connectionEntry, new ElectrocutionPart(bestProgress, bestPoint, bestResistance));
            }
        }
    }



    public void finishSimulation(SimulationResults results) {
        if (!CEEConfigs.server().enableElectrocution.get())
            return;

        Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        if (damageSource == null)
            damageSource = new DamageSource(registry.getHolderOrThrow(CEEDamageTypes.ELECTROCUTION));
        if (hvDamageSource == null)
            hvDamageSource = new DamageSource(registry.getHolderOrThrow(CEEDamageTypes.HV_ELECTROCUTION));
        for (Iterator<Map.Entry<Entity, ElectrocutionEntry>> iterator = electrocutions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Entity, ElectrocutionEntry> e = iterator.next();
            Entity entity = e.getKey();
            ElectrocutionEntry electrocutionEntry = e.getValue();
            if (entity.isRemoved()) {
                wireSimulationState.invalidateHandle(electrocutionEntry.handle);
                iterator.remove();
            }
            AttachedNode centralNode = electrocutionEntry.centralNode;
            double highestCurrent = 0d;
            double highestVoltage = results.getVoltageAt(centralNode);
            for (Map.Entry<ConnectionEntry, Pair<ElectrocutionPart, AttachedNode>> e1 : electrocutionEntry.nodes.entrySet()) {
                ConnectionEntry connectionEntry = e1.getKey();
                AttachedNode node = e1.getValue().getSecond();
                Vec3 pos1 = e1.getValue().getFirst().pos;
                double resistance = e1.getValue().getFirst().resistance;
                double current = Math.abs(results.getVoltageAt(centralNode, node)) / resistance;
                double voltage = Math.abs(results.getVoltageAt(node));
                if (current > 0.03) {
                    Vec3 pos2 = entity.position();
                    if (!(entity instanceof Player p && p.isCreative()))
                        CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos1, 40, new SendQuadraticParticlesPacket(pos1, pos2, ParticleTypes.BUBBLE_POP, -2f, 0.7f));
                }
                highestCurrent = Math.max(highestCurrent, current);
                highestVoltage = Math.max(highestVoltage, voltage);
            }
            if (electrocutionEntry.groundNode != null) {
                double resistance = 2333;
                double current = Math.abs(results.getVoltageAt(centralNode, electrocutionEntry.groundNode)) / resistance;
                highestCurrent = Math.max(highestCurrent, current);
            }

            if (highestCurrent > 0.06) {
                float damage = highestVoltage < 14_000 ? (float) (Math.log(highestCurrent * 10.5d) * 1.9d + 3.6d + 4.84d * highestCurrent) :
                        (float) (highestVoltage / 3_000);

                entity.hurt(highestVoltage > 9_900 ? hvDamageSource : damageSource, damage);
            }
        }

        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireSimulationState.getAllConnections()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionEntry connectionEntry = e.getValue();
            WireType wireType = connectionEntry.wireData.wireType();
            double lastWireVoltage = Math.max(Math.abs(sd.ticker.getVoltageAt(connection.node1())), Math.abs(sd.ticker.getVoltageAt(connection.node2())));
            double unsafeDistance = Mth.clamp(Math.log(0.006d * lastWireVoltage + 1) / 8.1d + 0.000011d * lastWireVoltage -0.6d, 0.1, 3);
            connectionEntry.dangerousDistance = unsafeDistance * unsafeDistance;

            connectionEntry.dangerousBB = connectionEntry.bb.inflate(connectionEntry.dangerousDistance);
            connectionEntry.isOvervolted = lastWireVoltage > wireType.maxInsulationVoltage();

        }
    }

    private record ElectrocutionEntry(Map<ConnectionEntry, Pair<ElectrocutionPart, AttachedNode>> nodes, AttachedNode centralNode,
                                      AttachedNode groundNode, WireSimulationState.WireCutHandle handle) {

    }

    private record ElectrocutionPart(float point, Vec3 pos, double resistance) {

    }
}
