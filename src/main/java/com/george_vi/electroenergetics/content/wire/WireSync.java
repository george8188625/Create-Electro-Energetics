package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.ClearCatenaryPacket;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.SendCatenaryPacket;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.SendNodeDataPacket;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import it.unimi.dsi.fastutil.longs.*;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WireSync {
    private static final Map<UUID, ChunkCuboidEntry> loadedChunks = new HashMap<>();

    private final InfrastructureSavedData sd;
    private final ServerLevel level;

    public WireSync(InfrastructureSavedData sd, ServerLevel level) {
        this.sd = sd;
        this.level = level;
    }

    public static int getViewDistance() {
//        return 1;
        return CEEConfigs.server().wireViewDistance.get();
    }

    public void handlePlayerEnterNewSection(ServerPlayer player, long newPos) {
        int viewDistance = getViewDistance();
        int originX = ChunkPos.getX(newPos);
        int originZ = ChunkPos.getZ(newPos);
        ChunkCuboidEntry chunks = loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO);

        ChunkCuboidEntry chunksToLoad = new ChunkCuboidEntry(originX, originZ, viewDistance);

        if (chunks.equals(chunksToLoad))
            return;

        loadedChunks.put(player.getUUID(), chunksToLoad);

        List<InWorldNode> newNodes = new ArrayList<>(sd.getDynamicNodes().stream()
                .map(n -> n.node)
                .filter(n -> {
                    Vec3 nodePos = sd.getNodePositionOrCenter(n);
                    // If is in chunksToLoad and not in chunks, then it has not yet been loaded, but will be now.
                    return chunksToLoad.includes(Mth.floor(nodePos.x) >> 4, Mth.floor(nodePos.z) >> 4) &&
                            !chunks.includes(Mth.floor(nodePos.x) >> 4, Mth.floor(nodePos.z) >> 4);
                }).toList());

        LongList chunksToRemove = new LongArrayList(viewDistance);
        chunks.supplyDiff(chunksToLoad, chunksToRemove::add, chunk -> sd.getNodesInChunk(chunk, newNodes::add));

        for (InWorldNode node : newNodes) {
            InWorldNodeData nodeData = sd.getNodeDataOrThrow(node);
            if (nodeData.label != null || nodeData.detachedNodeType != null) {
                CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.update(nodeData));
            }

            for (InWorldNodeConnection connection : sd.getConnections(node)) {
                Vec3 pos = sd.getNodePositionOrCenter(connection.node2());
                if (chunks.includes(Mth.floor(pos.x) >> 4, Mth.floor(pos.z) >> 4))
                    continue;
                CatnipServices.NETWORK.sendToClient(player,
                        SendWireConnectionsPacket.connectWire(connection, sd.getConnectionData(connection)));
            }
        }

        // If is in chunksToLoad and not in chunks, then it has not yet been loaded, but will be now.
        List<CatenaryConnection> newCatenary = sd.getAllCatenaryConnections().stream().filter(
                c ->
                        (chunksToLoad.includes(ChunkPos.asLong(c.pos1())) &&
                        !chunks.includes(ChunkPos.asLong(c.pos2()))) ||
                        (chunksToLoad.includes(ChunkPos.asLong(c.pos2())) &&
                        !chunks.includes(ChunkPos.asLong(c.pos1())))
        ).toList();

        CatnipServices.NETWORK.sendToClient(player, new SendCatenaryPacket(newCatenary));

        List<InWorldNode> nodesToRemove = new ArrayList<>(sd.getNodes().stream().filter(n ->
                chunksToRemove.contains(ChunkPos.asLong(n.sableSourcePos(level)))).toList());
        List<InWorldNodeConnection> connectionsToRemove = new ArrayList<>();
        for (InWorldNode node : nodesToRemove) {

            InWorldNodeData nodeData = sd.getNodeDataOrThrow(node);
            if (nodeData.label != null || nodeData.detachedNodeType != null) {
                CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.remove(node));
            }

            for (InWorldNodeConnection connection : sd.getConnections(node)) {
                Vec3 pos = sd.getNodePositionOrCenter(connection.node2());
                // Don't remove if the other node is still loaded by the player.
                if (chunksToLoad.includes(Mth.floor(pos.x) >> 4, Mth.floor(pos.z) >> 4))
                    continue;
                connectionsToRemove.add(connection);
            }
        }

        CatnipServices.NETWORK.sendToClient(player, new ClearWireConnectionsPacket(connectionsToRemove.stream().map(c -> Pair.of(c.node1(), c.node2())).toList(), false));

        List<CatenaryConnection> catenaryToRemove = sd.getAllCatenaryConnections().stream().filter(
                c ->
                        (chunks.includes(ChunkPos.asLong(c.pos1())) &&
                        !chunksToLoad.includes(ChunkPos.asLong(c.pos1())) &&
                        !chunksToLoad.includes(ChunkPos.asLong(c.pos2()))) ||
                        (chunks.includes(ChunkPos.asLong(c.pos2())) &&
                        !chunksToLoad.includes(ChunkPos.asLong(c.pos1())) &&
                        !chunksToLoad.includes(ChunkPos.asLong(c.pos2())))
        ).toList();

        CatnipServices.NETWORK.sendToClient(player, new ClearCatenaryPacket(catenaryToRemove, false));
    }

    public void handleWireRemoved(InWorldNodeConnection connection) {
        Vec3 pos1 = sd.getNodePositionOrCenter(connection.node1());
        long chunk1 = ChunkPos.asLong(Mth.floor(pos1.x) >> 4, Mth.floor(pos1.z) >> 4);
        Vec3 pos2 = sd.getNodePositionOrCenter(connection.node2());
        long chunk2 = ChunkPos.asLong(Mth.floor(pos2.x) >> 4, Mth.floor(pos2.z) >> 4);
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (long loadedChunk : loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO))
                if (loadedChunk == chunk1 || loadedChunk == chunk2) {
                    CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearWire(connection));
                    break;
                }
        }
    }

    public void handleWireAdded(InWorldNodeConnection connection, WireData data) {
        Vec3 pos1 = sd.getNodePositionOrCenter(connection.node1());
        long chunk1 = ChunkPos.asLong(Mth.floor(pos1.x) >> 4, Mth.floor(pos1.z) >> 4);
        Vec3 pos2 = sd.getNodePositionOrCenter(connection.node2());
        long chunk2 = ChunkPos.asLong(Mth.floor(pos2.x) >> 4, Mth.floor(pos2.z) >> 4);
        data.lastChunk1 = chunk1;
        data.lastChunk2 = chunk2;
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (long loadedChunk : loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO))
                if (loadedChunk == chunk1 || loadedChunk == chunk2) {
                    CatnipServices.NETWORK.sendToClient(player, SendWireConnectionsPacket.connectWire(connection, data));
                    break;
                }
        }
    }

    public void handleCatenaryRemoved(BlockPos pos1, BlockPos pos2) {
        long chunk1 = ChunkPos.asLong(pos1);
        long chunk2 = ChunkPos.asLong(pos2);
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (long loadedChunk : loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO))
                if (loadedChunk == chunk1 || loadedChunk == chunk2) {
                    CatnipServices.NETWORK.sendToClient(player, ClearCatenaryPacket.clearWire(new CatenaryConnection(pos1, pos2)));
                    break;
                }
        }
    }

    public void handleCatenaryAdded(BlockPos pos1, BlockPos pos2) {
        long chunk1 = ChunkPos.asLong(pos1);
        long chunk2 = ChunkPos.asLong(pos2);
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (long loadedChunk : loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO))
                if (loadedChunk == chunk1 || loadedChunk == chunk2) {
                    CatnipServices.NETWORK.sendToClient(player, SendCatenaryPacket.connectWire(new CatenaryConnection(pos1, pos2)));
                    break;
                }
        }
    }

    public void unloadForPlayer(ServerPlayer player) {
        loadedChunks.remove(player.getUUID());
        CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearAll());
        CatnipServices.NETWORK.sendToClient(player, ClearCatenaryPacket.clearAll());
    }

    public void handleWireRepositioned(InWorldNodeConnection connection, WireData data) {
        long chunk1 = ChunkPos.asLong(connection.node1().sableSourcePos(level));
        long chunk2 = ChunkPos.asLong(connection.node2().sableSourcePos(level));
        long lastChunk1 = data.lastChunk1;
        long lastChunk2 = data.lastChunk2;

        if (chunk1 == lastChunk1 && chunk2 == lastChunk2)
            return;

        for (ServerPlayer player : level.getPlayers(p -> true)) {
            ChunkCuboidEntry playerChunks = loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO);
            if (playerChunks.includes(chunk1) || playerChunks.includes(chunk2)) {
                if (!playerChunks.includes(lastChunk1) && !playerChunks.includes(lastChunk2))
                    CatnipServices.NETWORK.sendToClient(player, SendWireConnectionsPacket.connectWire(connection, data));
            } else {
                if (playerChunks.includes(lastChunk1) || playerChunks.includes(lastChunk2))
                    CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearWire(connection));
            }
        }

        data.lastChunk1 = chunk1;
        data.lastChunk2 = chunk2;
    }

    public void handleNodeLabelRename(InWorldNodeData nodeData) {
        Vec3 globalPos = nodeData.getGlobalPos();
        long chunk = ChunkPos.asLong(Mth.floor(globalPos.x) >> 4, Mth.floor(globalPos.z) >> 4);

        for (ServerPlayer player : level.getPlayers(p -> true)) {
            ChunkCuboidEntry playerChunks = loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO);
            if (playerChunks.includes(chunk)) {
                CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.update(nodeData));
            }
        }
    }

    public void handleNodeCreate(InWorldNodeData nodeData) {
        Vec3 globalPos = nodeData.getGlobalPos();
        long chunk = ChunkPos.asLong(Mth.floor(globalPos.x) >> 4, Mth.floor(globalPos.z) >> 4);
        nodeData.lastChunk = chunk;

        if (nodeData.label != null || nodeData.detachedNodeType != null)
            for (ServerPlayer player : level.getPlayers(p -> true)) {
                ChunkCuboidEntry playerChunks = loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO);
                if (playerChunks.includes(chunk)) {
                    CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.update(nodeData));
                }
            }
    }

    public void handleNodeRemove(InWorldNodeData nodeData) {
        if (nodeData.label == null && nodeData.detachedNodeType == null)
            return;
        Vec3 globalPos = nodeData.getGlobalPos();
        long chunk = ChunkPos.asLong(Mth.floor(globalPos.x) >> 4, Mth.floor(globalPos.z) >> 4);

        for (ServerPlayer player : level.getPlayers(p -> true)) {
            ChunkCuboidEntry playerChunks = loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO);
            if (playerChunks.includes(chunk)) {
                CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.remove(nodeData.node));
            }
        }
    }

    public void handleNodeMoved(InWorldNodeData nodeData) {
        long chunk = ChunkPos.asLong(nodeData.node.sableSourcePos(level));
        long lastChunk = nodeData.lastChunk;

        if (nodeData.label != null || nodeData.detachedNodeType != null)
            for (ServerPlayer player : level.getPlayers(p -> true)) {
                ChunkCuboidEntry playerChunks = loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO);
                if ((nodeData.detachedNodeType != null && playerChunks.includes(chunk)) ||
                        (playerChunks.includes(chunk) && !playerChunks.includes(lastChunk))) {
                    CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.update(nodeData));
                } else if (!playerChunks.includes(chunk) && playerChunks.includes(lastChunk)) {
                    CatnipServices.NETWORK.sendToClient(player, SendNodeDataPacket.remove(nodeData.node));
                }
            }

        nodeData.lastChunk = chunk;
    }

    /**
     * Instead of storing a bunch of chunk positions, it stores a range (rectangle) of chunk.
     */
    private static class ChunkCuboidEntry implements LongIterable {
        public int minX;
        public int maxX;
        public int minZ;
        public int maxZ;

        public static final ChunkCuboidEntry ZERO = new ChunkCuboidEntry(0, 0, 0, 0);

        public ChunkCuboidEntry(int minX, int maxX, int minZ, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        /**
         * Creates a range originating in the specified chunk position, with the distance to edge of the specified range
         */
        public ChunkCuboidEntry(int xOrigin, int zOrigin, int range) {
            minX = xOrigin - range;
            maxX = xOrigin + range + 1;

            minZ = zOrigin - range;
            maxZ = zOrigin + range + 1;
        }

        /**
         * Supplies chunks that exist in the original range, but not in {@code other} to {@code removalConsumer}.
         * Supplies chunks that do not exist in the original range, but do in {@code other} to {@code additionConsumer}
         */
        public void supplyDiff(ChunkCuboidEntry other, LongConsumer removalConsumer, LongConsumer additionConsumer) {
            forEach(l -> {
                if (!other.includes(l))
                    removalConsumer.accept(l);
            });

            other.forEach(l -> {
                if (!includes(l))
                    additionConsumer.accept(l);
            });
        }

        public boolean includes(long chunk) {
            return includes(ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
        }

        private boolean includes(int x, int z) {
            return x >= minX && x < maxX && z >= minZ && z < maxZ;
        }

        public boolean isEmpty() {
            return maxX - minX == 0 || maxZ - minZ == 0;
        }

        /**
         * @return Iterator that iterates over all the chunks.
         */
        @Override
        public @NotNull LongIterator iterator() {
            return new LongIterator() {
                int x = minX;
                int z = minZ;

                @Override
                public long nextLong() {
                    long chunk = ChunkPos.asLong(x, z);
                    x++;
                    if (x >= maxX) {
                        x = minX;
                        z++;
                    }
                    return chunk;
                }

                @Override
                public boolean hasNext() {
                    if (minX == maxX || minZ == maxZ)
                        return false;
                    return z < maxZ;
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCuboidEntry that = (ChunkCuboidEntry) o;
            return minX == that.minX && maxX == that.maxX && minZ == that.minZ && maxZ == that.maxZ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(minX, maxX, minZ, maxZ);
        }
    }

}
