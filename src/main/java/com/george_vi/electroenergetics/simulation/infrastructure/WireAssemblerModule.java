package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import net.createmod.catnip.data.Pair;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class WireAssemblerModule {

    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireSimulationState wireSimulationState;

    public WireAssemblerModule(InfrastructureSavedData sd, ServerLevel level, WireSimulationState wireSimulationState) {
        this.sd = sd;
        this.level = level;
        this.wireSimulationState = wireSimulationState;
    }

    public void buildCircuit(CircuitBuilder builder) {
        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireSimulationState.getAllConnections()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionEntry connectionData = e.getValue();
            double resistance = SimulationTicker.getWireResistance(connection.node1(), connection.node2(), connectionData.resistance.getAsDouble());
            List<WireSimulationState.CutWireEntry> cuts = connectionData.cuts;

            if (cuts.isEmpty())
                builder.connect(connection.node1(), connection.node2(), ElectricalProperties.resistor(resistance));
            else {
                float totalProgress = 0;
                Node lastNode = connection.node1();
                for (WireSimulationState.CutWireEntry cut : cuts) {
                    Node node = cut.node();
                    if (node.equals(lastNode))
                        continue;
                    float progress = cut.point() - totalProgress;
                    builder.connect(lastNode, node, ElectricalProperties.resistor(Math.max(0.001, resistance * progress)));
                    lastNode = node;
                }
                builder.connect(lastNode, connection.node2(), ElectricalProperties.resistor(Math.max(0.001, resistance * (1.01f - totalProgress))));
            }
        }
    }
}
