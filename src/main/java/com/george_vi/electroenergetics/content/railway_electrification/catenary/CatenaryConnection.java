package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class CatenaryConnection {
    public static final StreamCodec<ByteBuf, CatenaryConnection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, c -> c.pos1,
            BlockPos.STREAM_CODEC, c -> c.pos2,
            CatenaryConnection::new
    );
    public final BlockPos pos1;
    public final BlockPos pos2;

    public CatenaryConnection(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public Vec3 getStartPos() {
        return pos1.getBottomCenter();
    }

    public Vec3 getEndingPos() {
        return pos2.getBottomCenter();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CatenaryConnection) obj;
        return pos1.equals(that.pos1) && pos2.equals(that.pos2);
    }

    @Override
    public int hashCode() {
        int result = 31 + ((pos1.getY() + pos1.getZ() * 31) * 31 + pos1.getX());
        result = 31 * result + (pos2.getY() + pos2.getZ() * 31) * 31 + pos2.getX();
        result ^= (result >>> 16);
        return result;
    }

    @Override
    public String toString() {
        return pos1.toShortString() + " =|=|= " + pos2.toShortString();
    }

    public CatenaryConnection swap() {
        return new CatenaryConnection(pos2, pos1);
    }
}
