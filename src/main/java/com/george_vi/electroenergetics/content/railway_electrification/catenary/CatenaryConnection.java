package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * This class holds a catenary connection between two {@link BlockPos}.
 * These connections are bidirectional.
 * That means, for a connection between a and b, where a and b are two {@link BlockPos}:
 * CatenaryConnection(a, b) is equal to CatenaryConnection(b, a)
 */
public record CatenaryConnection(BlockPos pos1, BlockPos pos2) {
    public static final StreamCodec<ByteBuf, CatenaryConnection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, c -> c.pos1,
            BlockPos.STREAM_CODEC, c -> c.pos2,
            CatenaryConnection::new
    );

    /**
     * Returns true if the specified pos is a part of this connection.
     */
    public boolean isAny(BlockPos pos) {
        return pos1.equals(pos) || pos2.equals(pos);
    }

    public Vec3 getStartPos() {
        return pos1.getBottomCenter();
    }

    public Vec3 getEndingPos() {
        return pos2.getBottomCenter();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        var that = (CatenaryConnection) o;
        return (Objects.equals(this.pos1, that.pos1) &&
                Objects.equals(this.pos2, that.pos2)) ||
                (Objects.equals(this.pos2, that.pos1) &&
                        Objects.equals(this.pos1, that.pos2));
    }

    @Override
    public int hashCode() {
        return pos1.hashCode() ^ pos2.hashCode();
    }

    @Override
    public String toString() {
        return pos1.toShortString() + " =|=|= " + pos2.toShortString();
    }

    public CatenaryConnection swap() {
        return new CatenaryConnection(pos2, pos1);
    }
}
