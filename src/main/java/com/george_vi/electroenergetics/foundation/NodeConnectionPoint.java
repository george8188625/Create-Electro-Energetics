package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.content.wire_spool.InteractWirePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NodeConnectionPoint(Node node1, Node node2, float point) {
    public static final StreamCodec<ByteBuf, NodeConnectionPoint> STREAM_CODEC = StreamCodec.composite(
            Node.STREAM_CODEC, NodeConnectionPoint::node1,
            Node.STREAM_CODEC, NodeConnectionPoint::node2,
            ByteBufCodecs.FLOAT, NodeConnectionPoint::point,
            NodeConnectionPoint::new
    );
}
