package com.george_vi.electroenergetics.foundation.nodes;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

/**
 * This class holds a connection between two {@link InWorldNode}.
 * These connections are directional.
 * @see InWorldNodeConnection
 * @see DirectionalNodeConnection
 */
public class DirectionalInWorldNodeConnection extends DirectionalNodeConnection {
    private final InWorldNode node1;
    private final InWorldNode node2;

    public static StreamCodec<ByteBuf, DirectionalInWorldNodeConnection> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, DirectionalInWorldNodeConnection::node1,
            InWorldNode.STREAM_CODEC, DirectionalInWorldNodeConnection::node2,
            DirectionalInWorldNodeConnection::new
    );

    public DirectionalInWorldNodeConnection(InWorldNode node1, InWorldNode node2) {
        super(node1, node2);
        this.node1 = node1;
        this.node2 = node2;
    }

    /**
     * Returns an inverted copy of this connection.
     */
    @Override
    public DirectionalInWorldNodeConnection invert() {
        return new DirectionalInWorldNodeConnection(node2(), node1());
    }

    /**
     * Returns true if the specified node is a part of this connection.
     */
    public boolean isAny(InWorldNode node) {
        return node1.equals(node) || node2.equals(node);
    }

    @Override
    public InWorldNode node1() {
        return node1;
    }

    @Override
    public InWorldNode node2() {
        return node2;
    }

    @Override
    public String toString() {
        return node1 + " ->- " + node2;
    }

    @Override
    public int hashCode() {
        return node1.hashCode() * 31 + node2.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectionalInWorldNodeConnection that = (DirectionalInWorldNodeConnection) o;
        return Objects.equals(node1(), that.node1()) && Objects.equals(node2(), that.node2());
    }
}
