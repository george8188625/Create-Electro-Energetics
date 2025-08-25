package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.util.LUSolver;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public class SimulationTicker {

    public static List<SimulationPerformance> performances = new ArrayList<>();
    public static int totalTime = 0;
    public static void tick(ServerLevel level) {
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        long start = System.nanoTime();
        List<SimulationPerformance> performances = new ArrayList<>();

        // SETUP
        Set<Pair<WireType, NodeConnection>> connections = new HashSet<>();
        Map<Node, List<Node>> adjacency = new HashMap<>();
        Map<DirectionSensitiveNodeConnection, ElectricalProperties> connectionProperties = new HashMap<>();
        List<Node> allNodes = new ArrayList<>();
        allNodes.addAll(sd.getNodes());

        if (allNodes.isEmpty())
            return;

        for (Node node : allNodes) {
            adjacency.put(node, new ArrayList<>());
            for (NodeConnection connection : sd.getConnections(node)) {
                WireData connectionData = sd.getConnectionData(connection);
                WireType wireType = connectionData == null ? CEEWireTypes.STANDARD.get() : connectionData.wireType();
                connections.add(Pair.of(wireType, connection));
            }
        }

        // PRE-TICK / COLLECT BRIDGES
        for (InfrastructureSavedData.SimulatedDeviceInstance deviceInstance : sd.getDevices()) {
            SimulatedDevice device = deviceInstance.simulatedDevice();

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
        }

        for (Pair<WireType, NodeConnection> connection : connections) {
            Node node1 = connection.getSecond().node1();
            Node node2 = connection.getSecond().node2();

            if (adjacency.containsKey(node1) && adjacency.containsKey(node2)) {
                adjacency.get(node1).add(node2);
                adjacency.get(node2).add(node1);
            }

            connectionProperties.put(new DirectionSensitiveNodeConnection(connection.getSecond()), new ElectricalProperties(getWireResistance(node1, node2, connection.getFirst()), 0));
            connectionProperties.put(new DirectionSensitiveNodeConnection(connection.getSecond()).invert(), new ElectricalProperties(getWireResistance(node1, node2, connection.getFirst()), 0));
        }

        // RESET ALL VOLTAGES

        for (Node node : allNodes) {
            sd.setVoltage(node, 0d);
        }

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

        Map<BlockPos, Map<Node, Double>> resultsPerBlock = new HashMap<>();
        Map<BlockPos, Map<NodeConnection, Double>> sourceAmps = new HashMap<>();

        // SOLVE NETWORKS

        for (Set<Node> networkNodes : networks) {
            long networkStart = System.nanoTime();

            Map<Node, Map<Node, ElectricalProperties>> networkConnections = new HashMap<>();
            for (Node node : networkNodes) {
                Map<Node, ElectricalProperties> nodeConnections = new HashMap<>();
                for (Node connectedNode : adjacency.get(node))
                    nodeConnections.put(connectedNode, connectionProperties.get(new DirectionSensitiveNodeConnection(node, connectedNode)));
                networkConnections.put(node, nodeConnections);
            }

            Network network = new Network(networkNodes.stream().toList(), networkConnections, sd);

            long optimizationStart = System.nanoTime();
            int preOptimizationNodes = network.getAllNodes().size();
            if (CEEConfigs.server().optimizeGraph.get())
                network.optimize();
            long optimizationEnd = System.nanoTime();
            int postOptimizationNodes = network.getAllNodes().size();

            Map<Node, SimulationNode> simulationNodes = network.mapToSimNodes();
            Pair<Map<Couple<SimulationNode>, Double>, Pair<SparseMatrix<Double>, double[]>> matrices = network.formMatrix(simulationNodes);
            Map<Couple<SimulationNode>, Double> voltageSources = matrices.getFirst();

            long solveStart = System.nanoTime();
            double[] mnaResults;
            if (!voltageSources.isEmpty())
                mnaResults = LUSolver.solve(matrices.getSecond().getFirst(), matrices.getSecond().getSecond());
            else
                mnaResults = new double[matrices.getSecond().getSecond().length];

            long solveEnd = System.nanoTime();

            int i = 0;
            for (Couple<SimulationNode> con : voltageSources.keySet()) {
                double amps = mnaResults[simulationNodes.size() + i];
                if (!sourceAmps.containsKey(con.getFirst().correspondingNode.sourcePos()))
                    sourceAmps.put(con.getFirst().correspondingNode.sourcePos(), new HashMap<>());
                sourceAmps.get(con.getFirst().correspondingNode.sourcePos()).put(new NodeConnection(con.getFirst().correspondingNode, con.getSecond().correspondingNode), amps);
                i++;
            }

            Map<Node, Double> results = network.getResults(mnaResults, simulationNodes);

            int minVoltage = 0;
            int maxVoltage = 0;
            for (Map.Entry<Node, Double> e : results.entrySet()) {
                double voltage = e.getValue();

                if (!resultsPerBlock.containsKey(e.getKey().sourcePos()))
                    resultsPerBlock.put(e.getKey().sourcePos(), new HashMap<>());
                resultsPerBlock.get(e.getKey().sourcePos()).put(e.getKey(), voltage);

                minVoltage = (int) Math.min(voltage, minVoltage);
                maxVoltage = (int) Math.max(voltage, maxVoltage);

                sd.setVoltage(e.getKey(), voltage);
            }
            long networkEnd = System.nanoTime();

            performances.add(new SimulationPerformance(preOptimizationNodes, postOptimizationNodes, (int) (networkEnd - networkStart) / 1000, (int) (optimizationEnd - optimizationStart) / 1000, (int) (solveEnd - solveStart) / 1000, minVoltage, maxVoltage));
        }
        for (Node node : allNodes) {
            Map<Node, Double> e = resultsPerBlock.get(node.sourcePos());
            if (e == null) {
                resultsPerBlock.put(node.sourcePos(), new HashMap<>());
                e = new HashMap<>();
            }
            if (!e.containsKey(node))
                resultsPerBlock.get(node.sourcePos()).put(node, 0d);

        }

        resultsPerBlock.forEach((pos, voltages) -> {
            InfrastructureSavedData.SimulatedDeviceInstance device = sd.getDevice(pos);
            device.simulatedDevice().postTick(pos, level, voltages, sourceAmps.getOrDefault(pos, Collections.emptyMap()), device.extraData());
        });

        // WIRE BREAKING
        if (CEEConfigs.server().wiresBreak.get()) {
            NodeConnection longestWireToBreak = null;
            for (Pair<WireType, NodeConnection> connection : connections) {
                double vd = Math.abs(resultsPerBlock.getOrDefault(connection.getSecond().node1().sourcePos(), Collections.emptyMap()).getOrDefault(connection.getSecond().node1(), 0d)
                        - resultsPerBlock.getOrDefault(connection.getSecond().node2().sourcePos(), Collections.emptyMap()).getOrDefault(connection.getSecond().node2(), 0d));
                double current = vd / getWireResistance(connection.getSecond().node1(), connection.getSecond().node2(), connection.getFirst());

                float temp = sd.getConnectionTemperature(connection.getSecond());

                float newTemp = (float) (Math.min(current, 500));
                newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
                newTemp = Math.max(temp - 33.3f + newTemp, 0);
                sd.setConnectionTemperature(connection.getSecond(), newTemp);

                if (newTemp > 2000 && level.isLoaded(connection.getSecond().node1().sourcePos())) {
                    for (Vec3 point : QuadraticWireHelper.cablePoints(Vec3.atCenterOf(connection.getSecond().node1().sourcePos()), Vec3.atCenterOf(connection.getSecond().node2().sourcePos()), 1)) {
                        if (level.random.nextInt(100) != 0)
                            continue;
                        level.sendParticles(new DustParticleOptions(new Vector3f(0f, 0f, 0f), 1),
                                point.x(), point.y(), point.z(),
                                0, 0.5, 0.5, 0.5, 0);
                    }
                }

                if (newTemp > 5000) {
                    if (longestWireToBreak == null)
                        longestWireToBreak = connection.getSecond();
                    else if (getWireResistance(longestWireToBreak.node1(), longestWireToBreak.node2(), connection.getFirst()) < getWireResistance(connection.getSecond().node1(), connection.getSecond().node2(), connection.getFirst()))
                        longestWireToBreak = connection.getSecond();
                }

            }
            if (longestWireToBreak != null) {
                sd.removeConnection(longestWireToBreak);
                for (Vec3 point : QuadraticWireHelper.cablePoints(Vec3.atCenterOf(longestWireToBreak.node1().sourcePos()), Vec3.atCenterOf(longestWireToBreak.node2().sourcePos()), 1)) {
                    if (level.random.nextInt(3) != 0)
                        continue;
                    level.sendParticles(ParticleTypes.BUBBLE_POP,
                            point.x(), point.y(), point.z(),
                            10, 0.05, 0.05, 0.05, 0);
                }
            }
        }

        if (!allNodes.isEmpty())
            sd.setDirty();

        totalTime = (int) ((System.nanoTime() - start) / 1000);
        SimulationTicker.performances = performances;

        for (Node node : allNodes) {
            double voltage = sd.getVoltageAt(node);

            InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(node.sourcePos());
            if (deviceInstance != null)
                level.getPlayers(player -> Math.sqrt(player.blockPosition().distSqr(node.sourcePos())) < deviceInstance.simulatedDevice().sendVoltagesDistance()).forEach(
                        player -> CatnipServices.NETWORK.sendToClient(player, new SendVoltageDataPacket(node.sourcePos(), node.id(), (float) voltage)));
        }
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
