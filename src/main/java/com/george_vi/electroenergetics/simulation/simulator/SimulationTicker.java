package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import com.george_vi.electroenergetics.simulation.util.LUSolver;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class SimulationTicker {

    public static SimulatorProfiler profiler = new SimulatorProfiler();

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

        CircuitBuilder circuitBuilder = new CircuitBuilder(inWorldNodes);

        profiler.popPush("preTick");

        // PreTick
        for (SimulatedDeviceInstance deviceInstance : sd.getDevices()) {
            SimulatedDevice device = deviceInstance.simulatedDevice();
            device.preTick(deviceInstance.pos(), level, new BridgeCollector(circuitBuilder, sd), deviceInstance.extraData());
        }

        profiler.popPush("connectWires");

        sd.getWireConnectionManager().buildCircuit(circuitBuilder);

        profiler.popPush("addToGraphEvent");

        NeoForge.EVENT_BUS.post(new AddToElectricGraphEvent(circuitBuilder, level, sd));

        sd.clearVoltages();

        profiler.popPush("dfs");

        List<Set<WrappedIndexedNode>> networks = circuitBuilder.dfs();

        profiler.popPush("solveNetworks");

        // Solve
        double[] allVoltages = new double[circuitBuilder.allNodes().size()];
        Map<BlockPos, Object2DoubleMap<DirectionalNodeConnection>> sourceAmps = new HashMap<>();
//        Object2DoubleMap<Node> allVoltages = new Object2DoubleOpenHashMap<>(circuitBuilder.getAllNodes().size(), 0.999f);

        for (Set<WrappedIndexedNode> networkNodes : networks) {
            if (networkNodes.size() == 1)
                continue;
//            profiler.push("divideConnections");

            boolean foundSource = false;
            NodeLoop:
            for (WrappedIndexedNode node : networkNodes) {
                for (ElectricalProperties properties : node.adjacency.values()) {
                    if (properties.isCurrentSource() || properties.isVoltageSource()) {
                        foundSource = true;
                        break NodeLoop;
                    }
                }
            }

//            profiler.pop();

            if (!foundSource)
                continue;

            Network network = new Network(networkNodes, circuitBuilder, sd);

//            profiler.push("optimize");
            if (CEEConfigs.server().optimizeGraph.get() && networkNodes.size() > 40)
                network.optimize();
//            profiler.popPush("matrixify");

            network.mapToSimNodes();
            network.formMatrix();

//            profiler.popPush("solve");

            double[] mnaResults = LUSolver.solve(network.conductanceMatrix, network.rhsVector);

//            profiler.popPush("interpretResults");

            int i = 0;
            for (long packedConnection : network.voltageSources.keySet()) {
                double amps = mnaResults[network.simulationNodes.length + i];
                Node first = network.simulationNodes[DataPacker.unpackFirstI(packedConnection)].correspondingNode.node;
                Node second = network.simulationNodes[DataPacker.unpackSecondI(packedConnection)].correspondingNode.node;
                if (first instanceof InWorldNode iwn1 && second instanceof InWorldNode iwn2)
                    sourceAmps.computeIfAbsent(iwn1.sourcePos(), p -> new Object2DoubleOpenHashMap<>())
                            .put(new DirectionalNodeConnection(iwn1, iwn2), amps);
                i++;
            }

            Object2DoubleMap<WrappedIndexedNode> results = network.getResults(mnaResults);

//            profiler.popPush("interpretResults");

            results.forEach((n, v) -> {
                if (n.node instanceof InWorldNode iwn)
                    sd.setVoltage(iwn, v);
                allVoltages[n.ordinal] = v;
            });


//            profiler.pop();
        }

        profiler.pop();

        profiler.push("postTick");
        for (SimulatedDeviceInstance device : sd.getDevices()) {
            SimulationResults results = new SimulationResults(allVoltages, sourceAmps.getOrDefault(device.pos(), Object2DoubleMaps.emptyMap()), circuitBuilder, sd);
            device.simulatedDevice().postTick(device.pos(), level, results, device.extraData());
        }
        profiler.popPush("finish");


        Object2DoubleMap<DirectionalNodeConnection> allSourceAmps = new Object2DoubleOpenHashMap<>();
        for (Object2DoubleMap<DirectionalNodeConnection> v : sourceAmps.values())
            allSourceAmps.putAll(v);
        SimulationResults allSimulationResults = new SimulationResults(allVoltages, allSourceAmps, circuitBuilder, sd);
        sd.getWireConnectionManager().finish(allSimulationResults);

        NeoForge.EVENT_BUS.post(new FinishElectricSimulationEvent(allSimulationResults, level, sd));

        if (!circuitBuilder.allNodes().isEmpty())
            sd.setDirty();

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
