package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.foundation.AttachedNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.simulator.DirectionSensitiveNodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddToElectricGraphEvent extends Event {
    final Map<Node, List<Node>> adjacency;
    final Map<DirectionSensitiveNodeConnection, ElectricalProperties> connectionProperties;
    final List<Node> allNodes;
    public final ServerLevel level;
    public final InfrastructureSavedData sd;

    public AddToElectricGraphEvent(Map<Node, List<Node>> adjacency, Map<DirectionSensitiveNodeConnection, ElectricalProperties> connectionProperties, List<Node> allNodes, ServerLevel level, InfrastructureSavedData sd) {
        this.adjacency = adjacency;
        this.connectionProperties = connectionProperties;
        this.allNodes = allNodes;
        this.level = level;
        this.sd = sd;
    }

    public AttachedNode addNode(String ownerID, int id, BlockPos pos) {
        AttachedNode node = new AttachedNode(id, pos, ownerID, false);
        allNodes.add(node);
        return node;
    }

    public AttachedNode addGroundedNode(String ownerID, int id, BlockPos pos) {
        AttachedNode node = new AttachedNode(id, pos, ownerID, true);
        allNodes.add(node);
        return node;
    }

    public void connect(Node node1, Node node2, ElectricalProperties properties) {
        if (!allNodes.contains(node1))
            throw new IllegalArgumentException("Node: " + node1.toString() + " doesn't exist!");
        if (!allNodes.contains(node2))
            throw new IllegalArgumentException("Node: " + node2.toString() + " doesn't exist!");

        adjacency.computeIfAbsent(node1, n -> new ArrayList<>()).add(node2);
        adjacency.computeIfAbsent(node2, n -> new ArrayList<>()).add(node1);
        connectionProperties.put(new DirectionSensitiveNodeConnection(node1, node2), properties);
        connectionProperties.put(new DirectionSensitiveNodeConnection(node2, node1), properties.invert());
    }
}
