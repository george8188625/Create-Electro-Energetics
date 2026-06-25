package com.george_vi.electroenergetics.content.voxel_wire;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.lang.Lang;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

public enum VoxelWireNodeTile implements StringRepresentable {
    N0_0,
    N0_1,
    N0_2,
    N0_3,
    N1_0,
    N1_1,
    N1_2,
    N1_3,
    N2_0,
    N2_1,
    N2_2,
    N2_3,
    N3_0,
    N3_1,
    N3_2,
    N3_3,
    ;

    @SuppressWarnings("deprecation")
    public static final StringRepresentable.EnumCodec<VoxelWireNodeTile> CODEC = StringRepresentable.fromEnum(VoxelWireNodeTile::values);
    public static final StreamCodec<ByteBuf, VoxelWireNodeTile> STREAM_CODEC = ByteBufCodecs.idMapper(i -> VoxelWireNodeTile.values()[i], VoxelWireNodeTile::ordinal);

    public static VoxelWireNodeTile getByClickedPos(float x, float y) {
        return get(Mth.clamp(Mth.floor(x * 4f), 0, 4), Mth.clamp(Mth.floor(y * 4f), 0, 4));
    }

    public static VoxelWireNodeTile get(int x, int y) {
        if (x < 0 || x > 3 || y < 0 || y > 3)
            throw new IllegalArgumentException("Illegal position: " + x + ", " + y);
        return values()[x * 4 + y];
    }

    public int getX() {
        return ordinal() >> 2;
    }

    public int getY() {
        return ordinal() & 0b11;
    }

    @Override
    public String getSerializedName() {
        return Lang.asId(name());
    }
}
