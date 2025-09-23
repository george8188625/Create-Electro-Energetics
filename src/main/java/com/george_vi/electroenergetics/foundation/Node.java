package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class Node implements Comparable<Node> {
    public static final Codec<Node> CODEC = Codec.INT_STREAM.comapFlatMap(
            s -> Util.fixedSize(s, 4).map(a -> new Node(a[0], new BlockPos(a[1], a[2], a[3]))),
            n -> IntStream.of(n.id(), n.sourcePos().getX(), n.sourcePos().getY(), n.sourcePos().getZ())
    );

    public static final StreamCodec<ByteBuf, Node> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Node::id,
            BlockPos.STREAM_CODEC, Node::sourcePos,
            Node::new
    );

    public static final Node ZERO = new Node(0, BlockPos.ZERO);
    private final int id;
    private final BlockPos sourcePos;

    public Node(int id, BlockPos sourcePos) {
        this.id = id;
        this.sourcePos = sourcePos;
    }

    public static Node closestNode(Level level, Vec3 clickedPos, float threshold) {

        List<Pair<Vec3, Node>> nodes = new ArrayList<>();

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
                    nodes.add(Pair.of(e.getValue(), new Node(e.getKey(), pos)));
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
            return Vec3.ZERO;
        return db.getNodePosition(level, sourcePos, state, id);
    }

    public Vec3 toGlobalPos(Vec3 pos) {
        return pos.add(sourcePos().getX(), sourcePos().getY(), sourcePos().getZ());
    }

    @Override
    public int compareTo(@NotNull Node other) {
        if (id() == other.id())
            return sourcePos().compareTo(other.sourcePos());
        return id - other.id();
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", sourcePos=" + sourcePos +
                '}';
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
        var that = (Node) obj;
        return this.id == that.id &&
                Objects.equals(this.sourcePos, that.sourcePos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourcePos);
    }

}
