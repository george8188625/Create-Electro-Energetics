package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.util.*;
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
import java.util.concurrent.Future;

public class SimulationTicker {


    public static SimulatorProfiler profiler = new SimulatorProfiler();
    public static Map<Level, SimulationStats> allStats = new Object2ObjectArrayMap<>();

    public final ServerLevel level;
    public final InfrastructureSavedData sd;

    Object2DoubleMap<InWorldNode> VOLTAGES = new Object2DoubleOpenHashMap<>();
    public SimulationResults lastResults;
    private CircuitBuilder circuitBuilder;
    private SimulationStats stats;
    public List<SimulatorProfiler.ResultEntry> lastProfilerResults;
    public SimulationStats lastStats;

    public int microTickBits = 0;
    public int microTicks = 1 << microTickBits;

    public Future<SimulationResults> future = null;

    public SimulationTicker(ServerLevel level, InfrastructureSavedData sd) {
        this.level = level;
        this.sd = sd;
    }

    static WorkerThread electricalWorkerThread;

    public static void runServer() {
        electricalWorkerThread = new WorkerThread("CEE-Electrical-Simulator");
    }

    public static void stopServer() {
        electricalWorkerThread.shutdown();
    }

    public void tick() {
        microTickBits = CEEConfigs.server().simulationConfig.microTickBits.get();
        microTicks = 1 << microTickBits;

        profiler.push(level.dimension().location().toString());
        // Setup
        profiler.push("setupNodes");

        Set<InWorldNode> inWorldNodes = sd.getNodes();

        if (inWorldNodes.isEmpty()) {
            profiler.pop();
            profiler.pop();
            return;
        }
        stats = new SimulationStats();

        circuitBuilder = new CircuitBuilder(inWorldNodes);

        profiler.popPush("preTick");

        Collection<SimulatedDeviceInstance<?>> devices = sd.getDevices();
        // PreTick
        BridgeCollector bridgeCollector = new BridgeCollector(circuitBuilder, sd);
        for (SimulatedDeviceInstance deviceInstance : devices) {
            SimulatedDevice device = deviceInstance.simulatedDevice();
            device.preTick(deviceInstance.pos(), level, bridgeCollector, deviceInstance.extraData());
        }

        profiler.popPush("addToGraphEvent");

        NeoForge.EVENT_BUS.post(new AddToElectricGraphEvent(circuitBuilder, level, sd));

        VOLTAGES.clear();

        profiler.pop();
        profiler.pop();

        long thrStart = System.nanoTime();

        future = electricalWorkerThread.submit(() -> {
            List<Set<WrappedIndexedNode>> networks = circuitBuilder.dfs();
            stats.totalNodes = circuitBuilder.allNodes().size();
            stats.totalSeparatedNodes = new int[networks.size()];
            stats.totalOptimizedNodes = new int[networks.size()];
            stats.totalDevices = devices.size();
            long solveNanos = 0;
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

                if (CEEConfigs.server().simulationConfig.optimizeGraph.get())
                    network.optimize();
                stats.totalOptimizedNodes[k] = network.allNodes.size();
                k++;
                network.mapToSimNodes();
                allNetworks.add(network);
                stats.totalMicroTickers += network.microTicked.size();
            }
            long solveStart = System.nanoTime();

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
                    double[] mnaResults;
                    if (network.voltageSourcesInMatrix)
                        mnaResults = LUSolver.solve(network.conductanceMatrix, network.rhsVector);
                    else
                        mnaResults = CholeskySolver.solve(network.conductanceMatrix, network.rhsVector);
//                double[] mnaResults = CGSolver.solve(network.conductanceMatrix, network.rhsVector, new double[network.rhsVector.length], 1e-10d, 300);
//                double[] mnaResults = CGSolver.solve(network.conductanceMatrix, network.rhsVector,
//                        (network.lastMNAResult == null || network.lastMNAResult.length != network.rhsVector.length) ? new double[network.rhsVector.length] : network.lastMNAResult, 1e-10d, 300);
                    solveNanos += (System.nanoTime() - solveNanos);
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
                    VOLTAGES.put(iwn, allVoltages[i]);
                    continue;
                }
                double rms = 0;
                for (int j = 0; j < microTicks; j++)
                    rms += allVoltages[i|j] * allVoltages[i|j];
                rms /= microTicks;
                rms = Math.sqrt(rms);
                VOLTAGES.put(iwn, rms);
            }

            Object2DoubleMap<DirectionalNodeConnection> allSourceAmps = new Object2DoubleOpenHashMap<>();
            for (Object2DoubleMap<DirectionalNodeConnection> v : sourceAmps.values())
                allSourceAmps.putAll(v);
            profiler.addThreadedNanos(System.nanoTime() - thrStart);
            profiler.addSolverNanos(System.nanoTime() - solveStart);
            return new SimulationResults(allVoltages, microTicks, microTickBits, allSourceAmps, circuitBuilder, sd);
        });
    }

    public void endTick() {
        SimulationResults simulationResults = null;

        if (future != null) {
            try {
                simulationResults = future.get();
            } catch (InterruptedException | ExecutionException e) {
                InfrastructureSavedData.LOGGER.warn("Error while waiting for the electrical simulation to finish!", e);
                return;
            }
        }

        if (simulationResults == null) {
            return;
        }

        profiler.push(level.dimension().location().toString());

        profiler.push("postTick");

        for (SimulatedDeviceInstance device : sd.getDevices())
            device.simulatedDevice().postTick(device.pos(), level, simulationResults, device.extraData());

        profiler.popPush("finish");

//        sd.getWireConnectionManager().finish(allSimulationResults);
        sd.wireLifetimeModule.finishSimulation(simulationResults);

        NeoForge.EVENT_BUS.post(new FinishElectricSimulationEvent(simulationResults, level, sd));

        if (!circuitBuilder.allNodes().isEmpty())
            sd.setDirty();

        profiler.push("syncVoltages");
        VoltageSync.finishSimulation(sd, level, simulationResults);
        lastResults = simulationResults;
        lastStats = stats;
        lastProfilerResults = profiler.getResults();

        profiler.pop();
        profiler.pop();
        profiler.pop();

        SimulationTicker.allStats.put(level, stats);
    }

    public double getVoltageAt(InWorldNode node) {
        return VOLTAGES.getOrDefault(node, 0d);
    }

    public static double getWireResistance(InWorldNode node1, InWorldNode node2, double resistance) {
        double res = Math.sqrt(node1.sourcePos().distSqr(node2.sourcePos())) * resistance;
        return res == 0 ? resistance : res;
    }
}
