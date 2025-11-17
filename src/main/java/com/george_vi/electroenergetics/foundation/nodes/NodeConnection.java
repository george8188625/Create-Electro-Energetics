package com.george_vi.electroenergetics.foundation.nodes;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.stream.IntStream;

public class NodeConnection {
    public static final Codec<NodeConnection> CODEC = Codec.INT_STREAM.comapFlatMap(
            s -> Util.fixedSize(s, 8).map(a -> new NodeConnection(new InWorldNode(a[0], new BlockPos(a[1], a[2], a[3])), new InWorldNode(a[4], new BlockPos(a[5], a[6], a[7])))),
            n -> IntStream.of(n.node1.id(), n.node1.sourcePos().getX(), n.node1.sourcePos().getY(), n.node1.sourcePos().getZ(), n.node2.id(), n.node2.sourcePos().getX(), n.node2.sourcePos().getY(), n.node2.sourcePos().getZ())
    );

    public static final StreamCodec<ByteBuf, NodeConnection> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, NodeConnection::node1,
            InWorldNode.STREAM_CODEC, NodeConnection::node2,
            NodeConnection::new
    );

    private final InWorldNode node1;
    private final InWorldNode node2;

    public NodeConnection(InWorldNode node1, InWorldNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public NodeConnection(BlockPos pos, int id1, int id2) {
        this(new InWorldNode(id1, pos), new InWorldNode(id2, pos));
    }

    public boolean isAny(InWorldNode node) {
        return node1.equals(node) || node2.equals(node);
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

    public InWorldNode node1() {
        return node1;
    }

    public InWorldNode node2() {
        return node2;
    }

    @Override
    public String toString() {
        return "NodeConnection[" +
                "node1=" + node1 + ", " +
                "node2=" + node2 + ']';
    }
}
