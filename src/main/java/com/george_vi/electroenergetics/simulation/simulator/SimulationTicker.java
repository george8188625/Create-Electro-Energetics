package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.SendWireParticlesPacket;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.AttachedNode;
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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;

public class SimulationTicker {

    public static List<SimulationPerformance> performances = new ArrayList<>();
    public static int totalTime = 0;
    public static SimulatorProfiler profiler = new SimulatorProfiler();

    public static void tick(ServerLevel level) {
        profiler.push(level.dimension().location().toString());
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        // SETUP
        profiler.push("setupNodes");

        Set<Pair<WireType, NodeConnection>> connections = new HashSet<>();
        Map<Node, List<Node>> adjacency = new HashMap<>();
        Map<DirectionSensitiveNodeConnection, ElectricalProperties> connectionProperties = new HashMap<>();
        List<Node> allNodes = new ArrayList<>();
        allNodes.addAll(sd.getNodes());

        if (allNodes.isEmpty())
            return;

        profiler.popPush("setupConnections");

        for (Node node : allNodes) {
            adjacency.put(node, new ArrayList<>());
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
            List<Node> internalNodes = new ArrayList<>();

            device.preTick(deviceInstance.pos(), level, new BridgeCollector(bridges, internalNodes), deviceInstance.extraData());


            for (Node internalNode : internalNodes) {
                adjacency.put(internalNode, new ArrayList<>());
                allNodes.add(internalNode);
            }
            for (ElectricalNodeConnection bridge : bridges) {
                try {
                    adjacency.get(bridge.node1()).add(bridge.node2());
                } catch (NullPointerException e) { adjacency.put(bridge.node1(), new ArrayList<>()); adjacency.get(bridge.node1()).add(bridge.node2()); allNodes.add(bridge.node1()); }
                try {
                    adjacency.get(bridge.node2()).add(bridge.node1());
                } catch (NullPointerException e) { adjacency.put(bridge.node2(), new ArrayList<>()); adjacency.get(bridge.node2()).add(bridge.node1()); allNodes.add(bridge.node2()); }

                connectionProperties.put(new DirectionSensitiveNodeConnection(bridge.node1(), bridge.node2()), bridge.electricalProperties);
                connectionProperties.put(new DirectionSensitiveNodeConnection(bridge.node2(), bridge.node1()), bridge.electricalProperties.invert());
            }
            profiler.pop();
        }

        profiler.popPush("connectWires");

        for (Pair<WireType, NodeConnection> connection : connections) {
            Node node1 = connection.getSecond().node1();
            Node node2 = connection.getSecond().node2();

            if (adjacency.containsKey(node1) && adjacency.containsKey(node2)) {
                adjacency.get(node1).add(node2);
                adjacency.get(node2).add(node1);
            }

            connectionProperties.put(new DirectionSensitiveNodeConnection(connection.getSecond()), new ElectricalProperties(getWireResistance(node1, node2, connection.getFirst()), 0, 0));
            connectionProperties.put(new DirectionSensitiveNodeConnection(connection.getSecond()).invert(), new ElectricalProperties(getWireResistance(node1, node2, connection.getFirst()), 0, 0));
        }

        NeoForge.EVENT_BUS.post(new AddToElectricGraphEvent(adjacency, connectionProperties, allNodes, level, sd));

        // RESET ALL VOLTAGES

        for (Node node : allNodes) {
            sd.setVoltage(node, 0d);
        }

        profiler.popPush("dfs");

        // DFS
        Set<Node> visited = new HashSet<>();
        List<Set<Node>> networks = new ArrayList<>();

        for (Node node : allNodes) {
            if (adjacency.get(node).isEmpty())
                continue;
            if (!visited.contains(node)) {
                Set<Node> component = new HashSet<>();
                dfs(node, adjacency, visited, component);
                networks.add(component);
            }
        }

        profiler.popPush("solveNetworks");

        Map<BlockPos, Map<Node, Double>> resultsPerBlock = new HashMap<>();
        Map<BlockPos, Map<DirectionSensitiveNodeConnection, Double>> sourceAmps = new HashMap<>();

        // SOLVE NETWORKS

        for (Set<Node> networkNodes : networks) {

            profiler.push("lookForSource");
            boolean foundSource = false;
            Map<Node, Map<Node, ElectricalProperties>> networkConnections = new HashMap<>();
            for (Node node : networkNodes) {
                Map<Node, ElectricalProperties> nodeConnections = new HashMap<>();
                for (Node connectedNode : adjacency.get(node)) {
                    ElectricalProperties properties = connectionProperties.get(new DirectionSensitiveNodeConnection(node, connectedNode));
                    nodeConnections.put(connectedNode, properties);
                    if (properties.currentSource() != 0 || properties.voltageSource() != 0)
                        foundSource = true;
                }
                networkConnections.put(node, nodeConnections);
            }
            profiler.pop();
            double[] mnaResults;

            Map<Node, Double> results;

            if (foundSource) {
                Network network = new Network(networkNodes.stream().toList(), networkConnections, sd);

                profiler.push("optimize");
                if (CEEConfigs.server().optimizeGraph.get())
                    network.optimize();
                profiler.popPush("solve");

                Map<Node, SimulationNode> simulationNodes = network.mapToSimNodes();
                Pair<Map<Couple<SimulationNode>, Double>, Pair<SparseMatrix<Double>, double[]>> matrices = network.formMatrix(simulationNodes);
                Map<Couple<SimulationNode>, Double> voltageSources = matrices.getFirst();

                mnaResults = LUSolver.solve(matrices.getSecond().getFirst(), matrices.getSecond().getSecond());

                profiler.popPush("interpretResults");

                int i = 0;
                for (Couple<SimulationNode> con : voltageSources.keySet()) {
                    double amps = mnaResults[simulationNodes.size() + i];
                    if (!sourceAmps.containsKey(con.getFirst().correspondingNode.sourcePos()))
                        sourceAmps.put(con.getFirst().correspondingNode.sourcePos(), new HashMap<>());
                    sourceAmps.get(con.getFirst().correspondingNode.sourcePos()).put(new DirectionSensitiveNodeConnection(con.getFirst().correspondingNode, con.getSecond().correspondingNode), amps);
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

            int minVoltage = 0;
            int maxVoltage = 0;
            for (Map.Entry<Node, Double> e : results.entrySet()) {
                double voltage = e.getValue();

                minVoltage = (int) Math.min(voltage, minVoltage);
                maxVoltage = (int) Math.max(voltage, maxVoltage);

                sd.setVoltage(e.getKey(), voltage);
                if (e.getKey() instanceof AttachedNode)
                    continue;

                if (!resultsPerBlock.containsKey(e.getKey().sourcePos()))
                    resultsPerBlock.put(e.getKey().sourcePos(), new HashMap<>());
                resultsPerBlock.get(e.getKey().sourcePos()).put(e.getKey(), voltage);
            }

            profiler.pop();
        }

        profiler.pop();

        for (Node node : allNodes) {
            if (node instanceof AttachedNode)
                continue;
            Map<Node, Double> e = resultsPerBlock.get(node.sourcePos());
            if (e == null) {
                resultsPerBlock.put(node.sourcePos(), new HashMap<>());
                e = new HashMap<>();
            }
            if (!e.containsKey(node))
                resultsPerBlock.get(node.sourcePos()).put(node, 0d);

        }

        profiler.push("postTick");
        resultsPerBlock.forEach((pos, voltages) -> {
            InfrastructureSavedData.SimulatedDeviceInstance device = sd.getDevice(pos);
            if (device == null)
                return;
            profiler.push(device.simulatedDevice().getID().toString());
            SimulationResults results = new SimulationResults(voltages, sourceAmps.getOrDefault(pos, Collections.emptyMap()), adjacency, connectionProperties, sd);
            device.simulatedDevice().postTick(pos, level, results, device.extraData());
            profiler.pop();
        });
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
                            connection.node1().sourcePos().getCenter().distanceTo(connection.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(connection.node1(), connection.node2(), ParticleTypes.SMOKE, wireType.getSag()));
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
                sd.removeConnection(longestWireToBreak);
                CatnipServices.NETWORK.sendToClientsAround(level, VecHelper.lerp(0.5f, longestWireToBreak.node1().sourcePos().getCenter(), longestWireToBreak.node2().sourcePos().getCenter()),
                        longestWireToBreak.node1().sourcePos().getCenter().distanceTo(longestWireToBreak.node2().sourcePos().getCenter()) + 20, new SendWireParticlesPacket(longestWireToBreak.node1(), longestWireToBreak.node2(), ParticleTypes.BUBBLE_POP, longestWireTypeToBreak.getSag()));
            }
        }
        profiler.popPush("finish");

        if (!allNodes.isEmpty())
            sd.setDirty();

        Map<DirectionSensitiveNodeConnection, Double> allSourceAmps = new HashMap<>();
        for (Map<DirectionSensitiveNodeConnection, Double> v : sourceAmps.values())
            allSourceAmps.putAll(v);

        NeoForge.EVENT_BUS.post(new FinishElectricSimulationEvent(new SimulationResults(Collections.emptyMap(), allSourceAmps, adjacency, connectionProperties, sd), level, sd));
        
        VoltageSync.finishSimulation(sd, level);

        profiler.pop();
        profiler.pop();
    }



    public static double getWireResistance(Node node1, Node node2, WireType wireType) {
        double res = Math.sqrt(node1.sourcePos().distSqr(node2.sourcePos())) * wireType.getResistance();
        return res == 0 ? wireType.getResistance() : res;
    }

    static void dfs(Node current, Map<Node, List<Node>> adjacency, Set<Node> visited, Set<Node> component) {
        visited.add(current);
        component.add(current);
        for (Node neighbor : adjacency.get(current)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacency, visited, component);
            }
        }
    }

    public record SimulationPerformance(int nodes, int optimizedNodes, int totalTime, int optimizationTime, int solutionTime, int minVoltage, int maxVoltage) { }
}
