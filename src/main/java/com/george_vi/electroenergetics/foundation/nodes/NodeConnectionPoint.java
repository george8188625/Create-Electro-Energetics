package com.george_vi.electroenergetics.foundation.nodes;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record NodeConnectionPoint(InWorldNode node1, InWorldNode node2, float point) {
    public static final StreamCodec<ByteBuf, NodeConnectionPoint> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, NodeConnectionPoint::node1,
            InWorldNode.STREAM_CODEC, NodeConnectionPoint::node2,
            ByteBufCodecs.FLOAT, NodeConnectionPoint::point,
            NodeConnectionPoint::new
    );

    public Vec3 posAt(Vec3 pos1, Vec3 pos2) {
        return posAt(pos1, pos2, 1);
    }

    public Vec3 posAt(Vec3 pos1, Vec3 pos2, float sag) {
        return QuadraticWireHelper.posAt(pos1, pos2, point, sag);
    }

    public NodeConnectionPoint reverse() {
        return new NodeConnectionPoint(node2, node1, 1.0f - point);
    }

    public InWorldNodeConnection connection () {
        return new InWorldNodeConnection(node1, node2);
    }
}
