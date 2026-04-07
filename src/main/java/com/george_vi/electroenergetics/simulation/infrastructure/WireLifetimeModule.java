package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendPositionedWireParticlesPacket;
import com.george_vi.electroenergetics.content.wire.SendWireParticlesPacket;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class WireLifetimeModule {

    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireSimulationState wireSimulationState;

    public WireLifetimeModule(InfrastructureSavedData sd, ServerLevel level, WireSimulationState wireSimulationState) {
        this.sd = sd;
        this.level = level;
        this.wireSimulationState = wireSimulationState;
    }

    public void finishSimulation(SimulationResults results) {
        if (!CEEConfigs.server().wiresBreak.get())
            return;

        InWorldNodeConnection longestWireToBreak = null;
        WireType longestWireTypeToBreak = null;
        boolean isCatenary = false;
        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireSimulationState.getAllConnections()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionEntry connectionData = e.getValue();
            WireType wireType = connectionData.wireData.wireType();
            List<WireSimulationState.CutWireEntry> cuts = connectionData.cuts;

            double current = 0;
            double wholeWireResistance = SimulationTicker.getWireResistance(connection.node1(), connection.node2(), connectionData.resistance.getAsDouble());
            if (cuts == null || cuts.isEmpty()) {
                double vd = Math.abs(results.getVoltageAt(connection.node1(), connection.node2()));
                current = vd / wholeWireResistance;
            } else {
                float prevPoint = 0;
                Node prevNode = connection.node1();
                for (WireSimulationState.CutWireEntry cut : cuts) {
                    float point = cut.point();
                    if (point - prevPoint < 0.01)
                        continue;
                    AttachedNode node = cut.node();
                    float dist = point - prevPoint;
                    prevPoint = point;
                    double vd = Math.abs(results.getVoltageAt(prevNode, node));
                    prevNode = node;
                    current = Math.max(current, vd / (wholeWireResistance * dist));
                }
            }

            float temp = connectionData.wireData.temperature;
            float newTemp = (float) (Math.min(current, 1000));
            newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
            newTemp = Math.max(temp - 33.3f + newTemp, 0);
            connectionData.wireData.temperature = newTemp;
            boolean increase = newTemp > temp;

            if (newTemp > wireType.getMaxTemperature() * 0.6 && level.isLoaded(connection.node1().sourcePos())) {
                // Smoke particles
                Vec3 wireCenter = VecHelper.lerp(0.5f, connection.node1().sourcePos().getCenter(), connection.node2().sourcePos().getCenter());
                if (connectionData.isCatenary) {
                    Vec3 pos1 = connection.node1().sourcePos().getBottomCenter();
                    Vec3 pos2 = connection.node2().sourcePos().getBottomCenter();
                    CatnipServices.NETWORK.sendToClientsAround(level, wireCenter,
                            connection.node1().sourcePos().getCenter().distanceTo(connection.node2().sourcePos().getCenter()) + 20, new SendPositionedWireParticlesPacket(pos1, pos2, ParticleTypes.SMOKE, 0f, 0.2f));
                    Vec3 topPos1 = pos1.add(0, 1.5, 0);
                    Vec3 topPos2 = pos2.add(0, 1.5, 0);
                    float distance = (float) topPos1.distanceTo(topPos2);
                    CatnipServices.NETWORK.sendToClientsAround(level, wireCenter,
                            connection.node1().sourcePos().getCenter().distanceTo(connection.node2().sourcePos().getCenter()) + 20, new SendPositionedWireParticlesPacket(topPos1, topPos2, ParticleTypes.SMOKE, 350f * (0.05f / distance), 0.2f));
                } else
                    CatnipServices.NETWORK.sendToClientsAround(level, wireCenter,
                            connection.node1().sourcePos().getCenter().distanceTo(connection.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(connection.node1(), connection.node2(), ParticleTypes.SMOKE, wireType.getSag(), 0.2f));
            }

            if (newTemp > wireType.getMaxTemperature() && increase) {
                if (longestWireToBreak == null) {
                    longestWireToBreak = connection;
                    longestWireTypeToBreak = wireType;
                    isCatenary = connectionData.isCatenary;

                }
                else if (SimulationTicker.getWireResistance(longestWireToBreak.node1(), longestWireToBreak.node2(), wireType.getResistance()) < SimulationTicker.getWireResistance(connection.node1(), connection.node2(), wireType.getResistance())) {
                    longestWireToBreak = connection;
                    longestWireTypeToBreak = wireType;
                    isCatenary = connectionData.isCatenary;
                }
            }

        }
        if (longestWireToBreak != null && sd.level.random.nextFloat() > 0.96f) {
            if (isCatenary) {
                sd.removeCatenary(longestWireToBreak.node1().sourcePos(), longestWireToBreak.node2().sourcePos());
                return;
            }
            WireType replaceWith = longestWireTypeToBreak.overheatedReplacement();
            if (replaceWith == null) {
                sd.removeConnection(longestWireToBreak);
                CatnipServices.NETWORK.sendToClientsAround(level, VecHelper.lerp(0.5f, longestWireToBreak.node1().sourcePos().getCenter(), longestWireToBreak.node2().sourcePos().getCenter()),
                        longestWireToBreak.node1().sourcePos().getCenter().distanceTo(longestWireToBreak.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(longestWireToBreak.node1(), longestWireToBreak.node2(), ParticleTypes.BUBBLE_POP, longestWireTypeToBreak.getSag(), 4));
            } else {
                WireData wireConnectionData = sd.getConnectionData(longestWireToBreak);
                sd.setConnectionData(longestWireToBreak, new WireData(replaceWith, wireConnectionData.temperature(), wireConnectionData.attachments()));
                CatnipServices.NETWORK.sendToClientsAround(level, VecHelper.lerp(0.5f, longestWireToBreak.node1().sourcePos().getCenter(), longestWireToBreak.node2().sourcePos().getCenter()),
                        longestWireToBreak.node1().sourcePos().getCenter().distanceTo(longestWireToBreak.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(longestWireToBreak.node1(), longestWireToBreak.node2(), ParticleTypes.LARGE_SMOKE, longestWireTypeToBreak.getSag(), 4));
            }
        }
    }
}
