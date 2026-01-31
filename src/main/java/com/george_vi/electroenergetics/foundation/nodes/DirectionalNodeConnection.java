package com.george_vi.electroenergetics.foundation.nodes;

import net.minecraft.core.BlockPos;

/**
 * This class holds a connection between two {@link Node}.
 * These connections are directional.
 * @see DirectionalInWorldNodeConnection
 */
public class DirectionalNodeConnection {
    private final Node node1;
    private final Node node2;
    private DirectionalNodeConnection inverted;

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
        if (inverted == null) {
            inverted = new DirectionalNodeConnection(node2, node1);
            inverted.inverted = this;
        }
        return inverted;
    }

    @Override
    public int hashCode() {
        int result = 31 + node1.hashCode();
        result = 31 * result + node2.hashCode();
        result ^= (result >>> 16);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectionalNodeConnection that = (DirectionalNodeConnection) o;
        return node1.equals(that.node1) && node2.equals(that.node2);
    }

    public Node node1() {
        return node1;
    }

    public Node node2() {
        return node2;
    }

    @Override
    public String toString() {
        return node1 + " --- " + node2;
    }
}
