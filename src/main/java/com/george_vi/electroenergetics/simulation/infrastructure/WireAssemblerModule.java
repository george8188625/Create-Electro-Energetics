package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import it.unimi.dsi.fastutil.objects.ObjectDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Map;

public class WireAssemblerModule {

    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireSimulationState wireSimulationState;

    public WireAssemblerModule(InfrastructureSavedData sd, ServerLevel level, WireSimulationState wireSimulationState) {
        this.sd = sd;
        this.level = level;
        this.wireSimulationState = wireSimulationState;
    }

    /**
     * This is done this way to connect wires off thread for performance reasons.
     * It just writes what to connect here (main thread) and connects it off thread.
     * This is done to not tank TPS on large worlds
     */
    public void loadLazyConnections(List<ObjectDoublePair<DirectionalNodeConnection>> out) {

        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireSimulationState.getAllConnections()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionEntry connectionData = e.getValue();
            double resistance = SimulationTicker.getWireResistance(connection.node1(), connection.node2(),
                    connectionData.resistance.getAsDouble());
            List<WireSimulationState.CutWireEntry> cuts = connectionData.cuts;

            if (cuts.isEmpty())
                out.add(new ObjectDoubleImmutablePair<>(new DirectionalNodeConnection(connection.node1(), connection.node2()), resistance));
            else {
                float totalProgress = 0;
                Node lastNode = connection.node1();
                for (WireSimulationState.CutWireEntry cut : cuts) {
                    Node node = cut.node();
                    if (node.equals(lastNode))
                        continue;
                    float progress = cut.point() - totalProgress;
                    out.add(new ObjectDoubleImmutablePair<>(new DirectionalNodeConnection(lastNode, node), Math.max(0.001, resistance * progress)));
                    lastNode = node;
                }
                out.add(new ObjectDoubleImmutablePair<>(new DirectionalNodeConnection(lastNode, connection.node2()), Math.max(0.001, resistance * (1.01f - totalProgress))));
            }
        }
    }
}
