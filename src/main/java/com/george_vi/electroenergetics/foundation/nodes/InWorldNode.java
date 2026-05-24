package com.george_vi.electroenergetics.foundation.nodes;

import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Describes a node that is attached to a block.
 * Each {@link InWorldNode} is identified by an ID and a block position. IDs are local to the block position.
 * @see AttachedNode
 */
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

    public InWorldNode(int id, int x, int y, int z) {
        this(id, new BlockPos(x, y, z));
    }

    public static InWorldNode closestNode(Level level, Vec3 clickedPos, float threshold) {

        List<Pair<Vec3, InWorldNode>> nodes = new ArrayList<>();

        List<BlockPos> offsets = getNodeSearchBlockPositions(clickedPos);

        for (BlockPos offset : offsets) {
            BlockPos pos = BlockPos.containing(clickedPos).offset(offset);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ElectricalDeviceBlock<?> db)
                for (Map.Entry<Integer, Vec3> e : db.getNodePositions(level, pos, state).entrySet()) {
                    int id = e.getKey();
                    Vec3 nodePos = e.getValue();
                    if (db.isNodeAccessible(level, pos, state, id))
                        nodes.add(Pair.of(nodePos, new InWorldNode(id, pos)));
                }
        }

        return nodes.stream()
                .filter(e -> e.getSecond().toGlobalPos(e.getFirst(), level).distanceTo(clickedPos) <= threshold)
                .min(Comparator.comparingDouble(e -> e.getSecond().toGlobalPos(e.getFirst(), level).distanceTo(clickedPos)))
                .map(Pair::getSecond)
                .orElse(null);
    }

    public static InWorldNode closestNode(Level level, BlockPos pos, BlockState state, float threshold, Vec3 clickedPos) {

        List<Pair<Vec3, InWorldNode>> nodes = new ArrayList<>();

        if (state.getBlock() instanceof ElectricalDeviceBlock<?> db)
            for (Map.Entry<Integer, Vec3> e : db.getNodePositions(level, pos, state).entrySet()) {
                int id = e.getKey();
                Vec3 nodePos = e.getValue();
                if (db.isNodeAccessible(level, pos, state, id))
                    nodes.add(Pair.of(nodePos, new InWorldNode(id, pos)));
            }


        return nodes.stream()
                .filter(e -> e.getSecond().toGlobalPos(e.getFirst(), level).distanceTo(SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position)clickedPos)) <= threshold)
                .min(Comparator.comparingDouble(e -> e.getSecond().toGlobalPos(e.getFirst(), level).distanceTo(SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position)clickedPos))))
                .map(Pair::getSecond)
                .orElse(null);
    }

    private static @NotNull List<BlockPos> getNodeSearchBlockPositions(Vec3 clickedPos) {
        List<BlockPos> offsets = new ArrayList<>();
        Vec3 relativePos = new Vec3(clickedPos.x() % 1, clickedPos.y() % 1, clickedPos.z() % 1);
        int xDirection = relativePos.x < 0.5 ? -1 : 1;
        int yDirection = relativePos.y < 0.5 ? -1 : 1;
        int zDirection = relativePos.z < 0.5 ? -1 : 1;

        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                for (int z = 0; z < 2; z++)
                    offsets.add(new BlockPos(x * xDirection, y * yDirection, z * zDirection));
        return offsets;
    }

    /**
     * Reads the node from a compoundTag, converts legacy format to new.
     */
    public static InWorldNode readFromTag(CompoundTag connectionTag) {
        int[] nodes = connectionTag.getIntArray("Node");
        if (nodes.length != 4)
            return new InWorldNode(connectionTag.getInt("ID"),
                    NBTHelper.readBlockPos(connectionTag, "Pos"));
        return new InWorldNode(nodes[0], nodes[1], nodes[2], nodes[3]);
    }

    public Vec3 getPosition(Level level) {
        BlockState state = level.getBlockState(sourcePos);
        if (!(state.getBlock() instanceof ElectricalDeviceBlock<?> db))
            return null;
        Vec3 localPos = db.getNodePosition(level, sourcePos, state, id);
        if (localPos == null)
            return null;
        return toGlobalPos(localPos, level);
    }

    public Vec3 getPosition(Level level, float partialTicks) {
        BlockState state = level.getBlockState(sourcePos);
        if (!(state.getBlock() instanceof ElectricalDeviceBlock<?> db))
            return null;
        Vec3 localPos = db.getNodePosition(level, sourcePos, state, id);
        if (localPos == null)
            return null;
        return toGlobalPos(localPos, level, partialTicks);
    }

    public Vec3 getLocalPosition(Level level) {
        BlockState state = level.getBlockState(sourcePos);
        if (!(state.getBlock() instanceof ElectricalDeviceBlock<?> db))
            return null;
        Vec3 localPos = db.getNodePosition(level, sourcePos, state, id);
        if (localPos == null)
            return null;
        return localPos;
    }

    public Vec3 toGlobalPos(Vec3 pos, Level level) {
        Position globalPos = pos.add(sourcePos().getX(), sourcePos().getY(), sourcePos().getZ());
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, globalPos);
    }

    public Vec3 toGlobalPosNoSable(Vec3 pos, Level level) {
        return pos.add(sourcePos().getX(), sourcePos().getY(), sourcePos().getZ());
    }

    public Vec3 toGlobalPos(Vec3 pos, Level level, float partialTicks) {
        Vec3 globalPos = pos.add(sourcePos().getX(), sourcePos().getY(), sourcePos().getZ());
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, globalPos);
        if (subLevelAccess == null)
            return globalPos;

        Pose3dc logicalPose = subLevelAccess.logicalPose();
        Pose3dc lastPose = subLevelAccess.lastPose();

        Vec3 lastPos = lastPose.transformPosition(globalPos);
        Vec3 logicalPos = logicalPose.transformPosition(globalPos);

        return VecHelper.lerp(partialTicks, lastPos, logicalPos);
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
                this.sourcePos.equals(that.sourcePos);
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

    public BlockPos sableSourcePos(Level level) {
        return BlockPos.containing(SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position)sourcePos.getCenter()));
    }

    /**
     * Sometimes nodes in Sable's sublevels may not be fully loaded.
     * In some cases this may result in extremely long wire connections.
     * @return true if non-sublevel or sublevel and loaded, false if sublevel and unloaded
     */
    public boolean isFullyLoadable(Level level) {
       return isPosFullyLoadable(level, sourcePos);
    }

    /**
     * Sometimes nodes in Sable's sublevels may not be fully loaded.
     * In some cases this may result in extremely long wire connections.
     * @return true if non-sublevel or sublevel and loaded, false if sublevel and unloaded
     */
    public static boolean isPosFullyLoadable(Level level, BlockPos pos) {
        if (isFromSubLevel(level, pos))
            return SableCompanion.INSTANCE.getContaining(level, pos) != null;
        return true;
    }

    public static boolean isFromSubLevel(Level level, BlockPos pos) {
        int cX = pos.getX() >> 4;
        int cZ = pos.getZ() >> 4;
        return cX >= 1280_000 && cZ >= 1280_000;
    }
}
