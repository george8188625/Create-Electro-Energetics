package com.george_vi.electroenergetics.foundation.nodes;

import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.data.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class InWorldNode extends Node implements Comparable<InWorldNode> {
    public static final Codec<InWorldNode> CODEC = Codec.INT_STREAM.comapFlatMap(
            s -> Util.fixedSize(s, 4).map(a -> new InWorldNode(a[0], new BlockPos(a[1], a[2], a[3]))),
            n -> IntStream.of(n.id(), n.sourcePos().getX(), n.sourcePos().getY(), n.sourcePos().getZ())
    );

    public static final StreamCodec<ByteBuf, InWorldNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, InWorldNode::id,
            BlockPos.STREAM_CODEC, InWorldNode::sourcePos,
            InWorldNode::new
    );

    public static final InWorldNode ZERO = new InWorldNode(0, BlockPos.ZERO);
    private final int id;
    private final BlockPos sourcePos;

    public InWorldNode(int id, BlockPos sourcePos) {
        this.id = id;
        this.sourcePos = sourcePos;
    }

    public static InWorldNode closestNode(Level level, Vec3 clickedPos, float threshold) {

        List<Pair<Vec3, InWorldNode>> nodes = new ArrayList<>();

        List<BlockPos> offsets = new ArrayList<>();
        Vec3 relativePos = new Vec3(clickedPos.x() % 1, clickedPos.y() % 1, clickedPos.z() % 1);
        int xDirection = relativePos.x < 0.5 ? -1 : 1;
        int yDirection = relativePos.y < 0.5 ? -1 : 1;
        int zDirection = relativePos.z < 0.5 ? -1 : 1;

        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                for (int z = 0; z < 2; z++)
                    offsets.add(new BlockPos(x * xDirection, y * yDirection, z * zDirection));

        for (BlockPos offset : offsets) {
            BlockPos pos = BlockPos.containing(clickedPos).offset(offset);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof DeviceBlock db)
                for (Map.Entry<Integer, Vec3> e : db.getNodePositions(level, pos, state).entrySet())
                    nodes.add(Pair.of(e.getValue(), new InWorldNode(e.getKey(), pos)));
        }

        return nodes.stream()
                .filter(e -> e.getSecond().toGlobalPos(e.getFirst()).distanceTo(clickedPos) <= threshold)
                .min(Comparator.comparingDouble(e -> e.getSecond().toGlobalPos(e.getFirst()).distanceTo(clickedPos)))
                .map(Pair::getSecond)
                .orElse(null);
    }

    public Vec3 getPosition(Level level) {
        BlockState state = level.getBlockState(sourcePos);
        if (!(state.getBlock() instanceof DeviceBlock db))
            return null;
        Vec3 localPos = db.getNodePosition(level, sourcePos, state, id);
        if (localPos == null)
            return null;
        return toGlobalPos(localPos);
    }

    public Vec3 toGlobalPos(Vec3 pos) {
        return pos.add(sourcePos().getX(), sourcePos().getY(), sourcePos().getZ());
    }

    @Override
    public String toString() {
        return "N" + id + "@" + sourcePos.toShortString();
    }

    public int id() {
        return id;
    }

    public BlockPos sourcePos() {
        return sourcePos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InWorldNode) obj;
        return this.id == that.id &&
                Objects.equals(this.sourcePos, that.sourcePos);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + id;
        hash = hash * 31 + sourcePos.getX();
        hash = hash * 31 + sourcePos.getY();
        hash = hash * 31 + sourcePos.getZ();
        return hash;
    }

    @Override
    public int compareTo(@NotNull InWorldNode n) {
        if (n.id == id)
            return sourcePos().compareTo(n.sourcePos());
        return id - n.id;
    }
}
