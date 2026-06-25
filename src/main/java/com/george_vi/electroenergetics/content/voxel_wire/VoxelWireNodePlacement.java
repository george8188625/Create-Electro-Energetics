package com.george_vi.electroenergetics.content.voxel_wire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record VoxelWireNodePlacement(BlockPos position, Direction face, VoxelWireNodeTile nodeTile, int zOffset) {
    public static final Codec<VoxelWireNodePlacement> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.fieldOf("position").forGetter(VoxelWireNodePlacement::position))
            .and(Direction.CODEC.fieldOf("face").forGetter(VoxelWireNodePlacement::face))
            .and(VoxelWireNodeTile.CODEC.fieldOf("slot").forGetter(VoxelWireNodePlacement::nodeTile))
            .and(Codec.intRange(0, 15).fieldOf("zOffset").forGetter(VoxelWireNodePlacement::zOffset))
            .apply(instance, VoxelWireNodePlacement::new));

    public static final StreamCodec<ByteBuf, VoxelWireNodePlacement> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, VoxelWireNodePlacement::position,
            Direction.STREAM_CODEC, VoxelWireNodePlacement::face,
            VoxelWireNodeTile.STREAM_CODEC, VoxelWireNodePlacement::nodeTile,
            ByteBufCodecs.VAR_INT, VoxelWireNodePlacement::zOffset,
            VoxelWireNodePlacement::new
    );

    public static VoxelWireNodePlacement get(BlockPos pos, Direction clickedFace, Vec3 clickedPos) {
        pos = pos.relative(clickedFace);

        Vec3 localClickedPos = clickedPos.subtract(pos.getX(), pos.getY(), pos.getZ());
        localClickedPos = localClickedPos.subtract(0.5, 0.5, 0.5);

        if (clickedFace.getAxis().isHorizontal()) {
            localClickedPos = VecHelper.rotate(localClickedPos, clickedFace.toYRot(), Direction.Axis.Y);
        } else {
            localClickedPos = VecHelper.rotate(localClickedPos, clickedFace == Direction.UP ? 90 : 270, Direction.Axis.X);
        }

        localClickedPos = localClickedPos.add(0.5, 0.5, 0.5);
        int zOffset = (int) Math.round((1 + localClickedPos.z) * 16) % 16;
        VoxelWireNodeTile nodeTile = VoxelWireNodeTile.getByClickedPos((float) localClickedPos.x, (float) localClickedPos.y);

        return new VoxelWireNodePlacement(pos, clickedFace, nodeTile, zOffset);
    }

    public AABB getShape() {
        Vec3 center = getCenter();

        return AABB.ofSize(center, 3.9/16f, 3.9/16f, 3.9/16f);
    }

    public Vec3 getCenter() {
        Vec3 center = new Vec3(nodeTile.getX() / 4f + 2/16f, nodeTile.getY() / 4f + 2/16f, (2 + zOffset)/16f);

        if (face.getAxis().isHorizontal()) {
            center = VecHelper.rotateCentered(center, -face.toYRot(), Direction.Axis.Y);
        } else {
            center = VecHelper.rotateCentered(center, face == Direction.UP ? -90 : -270, Direction.Axis.X);
        }

        BlockPos pos = position;
        if (zOffset != 0)
            pos = pos.relative(face.getOpposite());
        center = center.add(pos.getX(), pos.getY(), pos.getZ());
        return center;
    }
}
