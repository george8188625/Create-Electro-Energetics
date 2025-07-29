package com.george_vi.electroenergetics.foundation;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public class NodeConnection {
    private final Node node1;
    private final Node node2;

    public NodeConnection(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public NodeConnection(BlockPos pos, int id1, int id2) {
        this(new Node(id1, pos), new Node(id2, pos));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NodeConnection) obj;
        return (Objects.equals(this.node1, that.node1) &&
                Objects.equals(this.node2, that.node2)) ||
                (Objects.equals(this.node2, that.node1) &&
                        Objects.equals(this.node1, that.node2));
    }

    @Override
    public int hashCode() {
        return node1.hashCode() ^ node2.hashCode();
    }

    public Node node1() {
        return node1;
    }

    public Node node2() {
        return node2;
    }

    @Override
    public String toString() {
        return "NodeConnection[" +
                "node1=" + node1 + ", " +
                "node2=" + node2 + ']';
    }

}
