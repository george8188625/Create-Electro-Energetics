package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class DirectionalInWorldNodeConnection extends NodeConnection {
    public static StreamCodec<ByteBuf, DirectionalInWorldNodeConnection> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, DirectionalInWorldNodeConnection::node1,
            InWorldNode.STREAM_CODEC, DirectionalInWorldNodeConnection::node2,
            DirectionalInWorldNodeConnection::new
    );

    public DirectionalInWorldNodeConnection(InWorldNode node1, InWorldNode node2) {
        super(node1, node2);
    }

    public DirectionalInWorldNodeConnection(NodeConnection con) {
        this(con.node1(), con.node2());
    }

    public DirectionalInWorldNodeConnection invert() {
        return new DirectionalInWorldNodeConnection(node2(), node1());
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1(), node2());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeConnection that = (NodeConnection) o;
        return Objects.equals(node1(), that.node1()) && Objects.equals(node2(), that.node2());
    }
}
