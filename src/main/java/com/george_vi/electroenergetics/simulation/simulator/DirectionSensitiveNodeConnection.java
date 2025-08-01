package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class DirectionSensitiveNodeConnection extends NodeConnection {
    public static StreamCodec<ByteBuf, DirectionSensitiveNodeConnection> STREAM_CODEC = StreamCodec.composite(
            Node.STREAM_CODEC, DirectionSensitiveNodeConnection::node1,
            Node.STREAM_CODEC, DirectionSensitiveNodeConnection::node2,
            DirectionSensitiveNodeConnection::new
    );

    public DirectionSensitiveNodeConnection(Node node1, Node node2) {
        super(node1, node2);
    }

    public DirectionSensitiveNodeConnection(NodeConnection con) {
        this(con.node1(), con.node2());
    }

    public DirectionSensitiveNodeConnection invert() {
        return new DirectionSensitiveNodeConnection(node2(), node1());
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
