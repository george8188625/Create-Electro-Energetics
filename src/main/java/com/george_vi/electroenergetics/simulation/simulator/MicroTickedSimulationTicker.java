package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import com.george_vi.electroenergetics.simulation.util.LUSolver;
import com.george_vi.electroenergetics.simulation.util.WorkerThread;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class MicroTickedSimulationTicker {

    public static SimulatorProfiler profiler = new SimulatorProfiler();
    public static Map<Level, SimulationStats> allStats = new Object2ObjectArrayMap<>();

    static WorkerThread electricalWorkerThread;

    public static void runServer() {
        electricalWorkerThread = new WorkerThread("CEE-Electrical-Simulator");
    }

    public static void stopServer() {
        electricalWorkerThread.shutdown();
    }

    public static void tick(ServerLevel level) {
        profiler.push(level.dimension().location().toString());
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        // Setup
        profiler.push("setupNodes");

        Set<InWorldNode> inWorldNodes = sd.getNodes();

        if (inWorldNodes.isEmpty()) {
            profiler.pop();
            profiler.pop();
            return;
        }
        SimulationStats stats = new SimulationStats();
        sd.stats = stats;

        final int microTickBits = 0;
        final int microTicks = 1 << microTickBits;

        CircuitBuilder circuitBuilder = new CircuitBuilder(inWorldNodes);
        sd.circuitBuilder = circuitBuilder;

        profiler.popPush("preTick");

        Collection<SimulatedDeviceInstance<?>> devices = sd.getDevices();
        // PreTick
        for (SimulatedDeviceInstance deviceInstance : devices) {
            SimulatedDevice device = deviceInstance.simulatedDevice();
            device.preTick(deviceInstance.pos(), level, new BridgeCollector(circuitBuilder, sd), deviceInstance.extraData());
        }

        profiler.popPush("connectWires");

        sd.getWireConnectionManager().buildCircuit(circuitBuilder);

        profiler.popPush("addToGraphEvent");

        NeoForge.EVENT_BUS.post(new AddToElectricGraphEvent(circuitBuilder, level, sd));

        sd.clearVoltages();

        profiler.pop();
        profiler.pop();

        long thrStart = System.nanoTime();

        sd.future = electricalWorkerThread.submit(() -> {
            List<Set<WrappedIndexedNode>> networks = circuitBuilder.dfs();
            stats.totalNodes = circuitBuilder.allNodes().size();
            stats.totalSeparatedNodes = new int[networks.size()];
            stats.totalOptimizedNodes = new int[networks.size()];
            stats.totalDevices = devices.size();

            // Solve
            double[] allVoltages = new double[circuitBuilder.allNodes().size() * microTicks];
            Map<BlockPos, Object2DoubleMap<DirectionalNodeConnection>> sourceAmps = new HashMap<>();

            List<Network> allNetworks = new ArrayList<>(networks.size());
            int l = 0;
            int k = 0;
            for (Set<WrappedIndexedNode> networkNodes : networks) {
                stats.totalSeparatedNodes[l] = networkNodes.size();
                l++;
                if (networkNodes.size() == 1)
                    continue;
                if (networkNodes.size() == 2) {
                    Iterator<WrappedIndexedNode> iterator = networkNodes.iterator();
                    WrappedIndexedNode node1 = iterator.next();
                    WrappedIndexedNode node2 = iterator.next();
                    ElectricalProperties properties = node1.adjacency.get(node2.ordinal);
                    if (properties.isSimpleResistor())
                        continue;
                }

                boolean foundSource = false;
                NodeLoop:
                for (WrappedIndexedNode node : networkNodes) {
                    for (ElectricalProperties properties : node.adjacency.values()) {
                        if (!properties.isSimpleResistor()) {
                            foundSource = true;
                            break NodeLoop;
                        }
                    }
                }

                if (!foundSource)
                    continue;

                Network network = new Network(networkNodes, circuitBuilder, sd);

                if (CEEConfigs.server().optimizeGraph.get())
                    network.optimize();
                stats.totalOptimizedNodes[k] = network.allNodes.size();
                k++;
                network.mapToSimNodes();
                allNetworks.add(network);
                stats.totalMicroTickers += network.microTicked.size();
            }
            for (int i = 0; i < microTicks; i++) {
                for (Long2ObjectMap.Entry<MicroTickingElectricalProperties> entry : circuitBuilder.microTickers.long2ObjectEntrySet()) {
                    int first = DataPacker.unpackFirstI(entry.getLongKey());
                    int second = DataPacker.unpackSecondI(entry.getLongKey());
                    entry.getValue().tick(allVoltages, i, microTickBits, microTicks, first, second);
                }
                for (Network network : allNetworks) {
                    network.formMatrix();
//                double[] mnaResults = BiCGStabSolver.solve(network.conductanceMatrix, network.rhsVector,
//                        (network.lastMNAResult == null || network.lastMNAResult.length != network.rhsVector.length) ? new double[network.rhsVector.length] : network.lastMNAResult, 1e-12d, 300);
//                double[] mnaResults = BiCGStabSolver.solve(network.conductanceMatrix, network.rhsVector,
//                        new double[network.rhsVector.length], 1e-12d, 300);
                    double[] mnaResults = LUSolver.solve(network.conductanceMatrix, network.rhsVector);
//                double[] mnaResults = CGSolver.solve(network.conductanceMatrix, network.rhsVector, new double[network.rhsVector.length], 1e-10d, 300);
//                double[] mnaResults = CGSolver.solve(network.conductanceMatrix, network.rhsVector,
//                        (network.lastMNAResult == null || network.lastMNAResult.length != network.rhsVector.length) ? new double[network.rhsVector.length] : network.lastMNAResult, 1e-10d, 300);
                    network.getResults(mnaResults, allVoltages, microTickBits, i);

                    int j = 0;
                    for (long packedConnection : network.voltageSources.keySet()) {
                        double amps = mnaResults[network.simulationNodes.length + j];
                        Node first = network.simulationNodes[DataPacker.unpackFirstI(packedConnection)].correspondingNode.node;
                        Node second = network.simulationNodes[DataPacker.unpackSecondI(packedConnection)].correspondingNode.node;
                        if (first instanceof InWorldNode iwn1 && second instanceof InWorldNode iwn2)
                            sourceAmps.computeIfAbsent(iwn1.sourcePos(), p -> new Object2DoubleOpenHashMap<>())
                                    .put(new DirectionalNodeConnection(iwn1, iwn2), amps);
                        j++;
                    }
                }
                for (Long2ObjectMap.Entry<MicroTickingElectricalProperties> entry : circuitBuilder.microTickers.long2ObjectEntrySet()) {
                    int first = DataPacker.unpackFirstI(entry.getLongKey());
                    int second = DataPacker.unpackSecondI(entry.getLongKey());
                    entry.getValue().afterTick(allVoltages, first, second, i, microTickBits, microTicks);
                }

            }

            for (int i = 0; i < allVoltages.length; i += microTicks) {
                WrappedIndexedNode node = circuitBuilder.getNode(i >> microTickBits);
                if (!(node.node instanceof InWorldNode iwn))
                    continue;
                if (microTickBits == 0) {
                    sd.setVoltage(iwn, allVoltages[i]);
                    continue;
                }
                double rms = 0;
                for (int j = 0; j < microTicks; j++)
                    rms += allVoltages[i|j] * allVoltages[i|j];
                rms /= microTicks;
                rms = Math.sqrt(rms);
                sd.setVoltage(iwn, rms);
            }

            Object2DoubleMap<DirectionalNodeConnection> allSourceAmps = new Object2DoubleOpenHashMap<>();
            for (Object2DoubleMap<DirectionalNodeConnection> v : sourceAmps.values())
                allSourceAmps.putAll(v);
            profiler.addThreadedNanos(System.nanoTime() - thrStart);
            return new SimulationResults(allVoltages, microTicks, microTickBits, allSourceAmps, circuitBuilder, sd);
        });
    }

    public static void endTick(ServerLevel level) {
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        SimulationResults allSimulationResults = null;

        if (sd.future != null) {
            try {
                allSimulationResults = sd.future.get();
            } catch (InterruptedException | ExecutionException e) {
                InfrastructureSavedData.LOGGER.warn("Error while waiting for the electrical simulation to finish!", e);
                return;
            }
        }

        if (allSimulationResults == null) {
            return;
        }

        profiler.push(level.dimension().location().toString());

        profiler.push("postTick");

        for (SimulatedDeviceInstance device : sd.getDevices())
            device.simulatedDevice().postTick(device.pos(), level, allSimulationResults, device.extraData());

        profiler.popPush("finish");

        sd.getWireConnectionManager().finish(allSimulationResults);

        NeoForge.EVENT_BUS.post(new FinishElectricSimulationEvent(allSimulationResults, level, sd));

        if (!sd.circuitBuilder.allNodes().isEmpty())
            sd.setDirty();

        profiler.push("syncVoltages");
        VoltageSync.finishSimulation(sd, level, allSimulationResults);
        sd.lastResults = allSimulationResults;
        sd.lastStats = sd.stats;
        sd.lastProfilerResults = profiler.getResults();

        profiler.pop();
        profiler.pop();
        profiler.pop();

        MicroTickedSimulationTicker.allStats.put(level, sd.stats);
    }

    public static double getWireResistance(InWorldNode node1, InWorldNode node2, WireType wireType) {
        double res = Math.sqrt(node1.sourcePos().distSqr(node2.sourcePos())) * wireType.getResistance();
        return res == 0 ? wireType.getResistance() : res;
    }
}
