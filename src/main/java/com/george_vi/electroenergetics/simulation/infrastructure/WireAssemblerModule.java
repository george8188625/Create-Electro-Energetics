package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.createmod.catnip.data.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public class WireAssemblerModule {

    final InfrastructureSavedData sd;
    final ServerLevel level;
    final WireInfrastructure wireInfrastructure;

    public WireAssemblerModule(InfrastructureSavedData sd, ServerLevel level, WireInfrastructure wireInfrastructure) {
        this.sd = sd;
        this.level = level;
        this.wireInfrastructure = wireInfrastructure;
    }

    public void buildCircuit(CircuitBuilder builder) {
        for (Map.Entry<InWorldNodeConnection, ConnectionEntry> e : wireInfrastructure.connections.entrySet()) {
            InWorldNodeConnection connection = e.getKey();
            ConnectionEntry connectionData = e.getValue();
            double resistance = connectionData.resistance.getAsDouble();
            List<Pair<Float, AttachedNode>> cuts = connectionData.cuts;

            if (cuts.isEmpty())
                builder.connect(connection.node1(), connection.node2(), ElectricalProperties.resistor(resistance));
            else {
                float totalProgress = 0;
                Node lastNode = connection.node1();
                for (Pair<Float, AttachedNode> cut : cuts) {
                    Node node = cut.getSecond();
                    if (node.equals(lastNode))
                        continue;
                    float progress = cut.getFirst() - totalProgress;
                    builder.connect(lastNode, node, ElectricalProperties.resistor(Math.max(0.001, resistance * progress)));
                    lastNode = node;
                }
                builder.connect(lastNode, connection.node2(), ElectricalProperties.resistor(Math.max(0.001, resistance * (1.01f - totalProgress))));
            }
        }
    }
}
