package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public class DirectionalNodeConnection {
    private final Node node1;
    private final Node node2;

    public DirectionalNodeConnection(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public DirectionalNodeConnection(BlockPos pos, int id1, int id2) {
        this(new InWorldNode(id1, pos), new InWorldNode(id2, pos));
    }

    public boolean isAny(Node node) {
        return node1.equals(node) || node2.equals(node);
    }

    public DirectionalNodeConnection invert() {
        return new DirectionalNodeConnection(node2, node1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1, node2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectionalNodeConnection that = (DirectionalNodeConnection) o;
        return Objects.equals(node1, that.node1()) && Objects.equals(node2, that.node2());
    }

    public Node node1() {
        return node1;
    }

    public Node node2() {
        return node2;
    }

    @Override
    public String toString() {
        return node1 + " - " + node2;
    }
}
