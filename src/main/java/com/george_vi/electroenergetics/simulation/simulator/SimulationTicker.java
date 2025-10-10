package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendWireParticlesPacket;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.util.LUSolver;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class SimulationTicker {

    public static int totalTime = 0;
    public static SimulatorProfiler profiler = new SimulatorProfiler();

    public static void tick(ServerLevel level) {
        profiler.push(level.dimension().location().toString());
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        // SETUP
        profiler.push("setupNodes");

        Set<Pair<WireType, NodeConnection>> connections = new HashSet<>();

        List<InWorldNode> inWorldNodes = sd.getNodes();
        CircuitBuilder circuitBuilder = new CircuitBuilder(inWorldNodes);

        if (circuitBuilder.getAllNodes().isEmpty()) {
            profiler.pop();
            profiler.pop();
            return;
        }

        profiler.popPush("setupConnections");

        for (InWorldNode node : inWorldNodes) {
            for (NodeConnection connection : sd.getConnections(node)) {
                WireData connectionData = sd.getConnectionData(connection);
                WireType wireType = connectionData == null ? CEEWireTypes.STANDARD.get() : connectionData.wireType();
                connections.add(Pair.of(wireType, connection));
            }
        }

        profiler.popPush("preTick");

        // PRE-TICK / COLLECT BRIDGES
        for (InfrastructureSavedData.SimulatedDeviceInstance deviceInstance : sd.getDevices()) {
            SimulatedDevice device = deviceInstance.simulatedDevice();
            profiler.push(device.getID().toString());

            List<ElectricalNodeConnection> bridges = new ArrayList<>();
            List<InWorldNode> internalNodes = new ArrayList<>();
            Map<Node, Double> groundConductance = new HashMap<>();
            Map<Node, Integer> defaultZeroPotentials = new HashMap<>();

            device.preTick(deviceInstance.pos(), level, new BridgeCollector(bridges, internalNodes, groundConductance, defaultZeroPotentials), deviceInstance.extraData());

            for (ElectricalNodeConnection bridge : bridges)
                circuitBuilder.connect(bridge.node1(), bridge.node2(), bridge.electricalProperties);
            for (Map.Entry<Node, Double> e : groundConductance.entrySet())
                circuitBuilder.ground(e.getKey(), e.getValue());

            for (Map.Entry<Node, Integer> e : defaultZeroPotentials.entrySet())
                circuitBuilder.defaultZeroPotential(e.getKey(), e.getValue());
            profiler.pop();
        }

        profiler.popPush("connectWires");

        for (Pair<WireType, NodeConnection> connection : connections) {
            InWorldNode node1 = connection.getSecond().node1();
            InWorldNode node2 = connection.getSecond().node2();
            circuitBuilder.connect(node1, node2, ElectricalProperties.resistor(getWireResistance(node1, node2, connection.getFirst())));
        }

        NeoForge.EVENT_BUS.post(new AddToElectricGraphEvent(circuitBuilder, level, sd));

        // RESET ALL VOLTAGES

        for (InWorldNode node : inWorldNodes)
            sd.setVoltage(node, 0d);

        profiler.popPush("dfs");

        // DFS
        List<Set<Node>> networks = circuitBuilder.dfs();

        profiler.popPush("solveNetworks");

        Map<BlockPos, Map<Node, Double>> resultsPerBlock = new HashMap<>();
        Map<BlockPos, Map<DirectionalNodeConnection, Double>> sourceAmps = new HashMap<>();
        Map<Node, Double> allVoltages = new HashMap<>(circuitBuilder.getAllNodes().size());
        // SOLVE NETWORKS

        for (Set<Node> networkNodes : networks) {

            profiler.push("divideConnections");
            boolean foundSource = false;
            Map<Node, Map<Node, ElectricalProperties>> networkConnections = new HashMap<>(networkNodes.size());
            for (Node node : networkNodes) {
                Map<Node, ElectricalProperties> nodeConnections = new HashMap<>(3);
                for (Map.Entry<Node, ElectricalProperties> e : circuitBuilder.getAdjacentNodes(node).entrySet()) {
                    Node connectedNode = e.getKey();
                    ElectricalProperties properties = e.getValue();

                    nodeConnections.put(connectedNode, properties);
                    if (properties.isCurrentSource() || properties.isVoltageSource())
                        foundSource = true;
                }
                networkConnections.put(node, nodeConnections);
            }
            profiler.pop();
            double[] mnaResults;

            Map<Node, Double> results;

            if (foundSource) {
                Network network = new Network(networkNodes, networkConnections, circuitBuilder, sd);

                profiler.push("optimize");
                if (CEEConfigs.server().optimizeGraph.get())
                    network.optimize();
                profiler.popPush("matrixify");

                Map<Node, SimulationNode> simulationNodes = network.mapToSimNodes();
                Pair<Map<Couple<SimulationNode>, Double>, Pair<double[][], double[]>> matrices = network.formMatrix(simulationNodes);
                profiler.popPush("solve");

                Map<Couple<SimulationNode>, Double> voltageSources = matrices.getFirst();

                mnaResults = LUSolver.solve(matrices.getSecond().getFirst(), matrices.getSecond().getSecond());

                profiler.popPush("interpretResults");

                int i = 0;
                for (Couple<SimulationNode> con : voltageSources.keySet()) {
                    double amps = mnaResults[simulationNodes.size() + i];
                    if (con.getFirst().correspondingNode instanceof InWorldNode iwn1 && con.getSecond().correspondingNode instanceof InWorldNode iwn2)
                        sourceAmps.computeIfAbsent(iwn1.sourcePos(), p -> new HashMap<>())
                                .put(new DirectionalNodeConnection(iwn1, iwn2), amps);
                    i++;
                }

                results = network.getResults(mnaResults, simulationNodes);
                profiler.pop();
            } else {
                results = new HashMap<>(networkNodes.size());
                for (Node node : networkNodes)
                    results.put(node, 0d);
            }

            profiler.push("interpretResults");

            for (Map.Entry<Node, Double> e : results.entrySet()) {
                double voltage = e.getValue();
                Node node = e.getKey();
                if (node instanceof InWorldNode iwn)
                    sd.setVoltage(iwn, voltage);
            }
            allVoltages.putAll(results);

            profiler.pop();
        }

        profiler.pop();

        for (InWorldNode node : inWorldNodes)
            allVoltages.putIfAbsent(node, 0d);

        profiler.push("postTick");
        for (InfrastructureSavedData.SimulatedDeviceInstance device : sd.getDevices()) {
            profiler.push(device.simulatedDevice().getID().toString());
            SimulationResults results = new SimulationResults(allVoltages, sourceAmps.getOrDefault(device.pos(), Collections.emptyMap()), circuitBuilder, sd);
            device.simulatedDevice().postTick(device.pos(), level, results, device.extraData());
            profiler.pop();
        }
        profiler.popPush("updateWireTemperature");

        // WIRE BREAKING
        if (CEEConfigs.server().wiresBreak.get()) {
            NodeConnection longestWireToBreak = null;
            WireType longestWireTypeToBreak = null;
            for (Pair<WireType, NodeConnection> e : connections) {
                WireType wireType = e.getFirst();
                NodeConnection connection = e.getSecond();

                double vd = Math.abs(resultsPerBlock.getOrDefault(connection.node1().sourcePos(), Collections.emptyMap()).getOrDefault(connection.node1(), 0d)
                        - resultsPerBlock.getOrDefault(connection.node2().sourcePos(), Collections.emptyMap()).getOrDefault(connection.node2(), 0d));
                double current = vd / getWireResistance(connection.node1(), connection.node2(), wireType);

                float temp = sd.getConnectionTemperature(connection);

                float newTemp = (float) (Math.min(current, 500));
                newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
                newTemp = Math.max(temp - 33.3f + newTemp, 0);
                sd.setConnectionTemperature(connection, newTemp);

                if (newTemp > wireType.getMaxTemperature() * 0.6 && level.isLoaded(connection.node1().sourcePos())) {
                    // smoke particles
                    CatnipServices.NETWORK.sendToClientsAround(level, VecHelper.lerp(0.5f, connection.node1().sourcePos().getCenter(), connection.node2().sourcePos().getCenter()),
                            connection.node1().sourcePos().getCenter().distanceTo(connection.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(connection.node1(), connection.node2(), ParticleTypes.SMOKE, wireType.getSag(), 0.2f));
                }

                if (newTemp > wireType.getMaxTemperature()) {
                    if (longestWireToBreak == null) {
                        longestWireToBreak = connection;
                        longestWireTypeToBreak = wireType;

                    }
                    else if (getWireResistance(longestWireToBreak.node1(), longestWireToBreak.node2(), wireType) < getWireResistance(connection.node1(), connection.node2(), wireType)) {
                        longestWireToBreak = connection;
                        longestWireTypeToBreak = wireType;
                    }
                }

            }
            if (longestWireToBreak != null) {
                WireType replaceWith = longestWireTypeToBreak.replaceOverheatedWith();
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
        profiler.popPush("finish");

        if (!circuitBuilder.getAllNodes().isEmpty())
            sd.setDirty();

        Map<DirectionalNodeConnection, Double> allSourceAmps = new HashMap<>();
        for (Map<DirectionalNodeConnection, Double> v : sourceAmps.values())
            allSourceAmps.putAll(v);

        NeoForge.EVENT_BUS.post(new FinishElectricSimulationEvent(new SimulationResults(allVoltages, allSourceAmps, circuitBuilder, sd), level, sd));
        profiler.push("syncVoltages");
        VoltageSync.finishSimulation(sd, level);

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }



    public static double getWireResistance(InWorldNode node1, InWorldNode node2, WireType wireType) {
        double res = Math.sqrt(node1.sourcePos().distSqr(node2.sourcePos())) * wireType.getResistance();
        return res == 0 ? wireType.getResistance() : res;
    }
}
