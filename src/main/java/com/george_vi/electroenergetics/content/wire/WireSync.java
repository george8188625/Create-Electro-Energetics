package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.ClearCatenaryPacket;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.SendCatenaryPacket;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
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

    public final InfrastructureSavedData sd;
    public final ServerLevel level;

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

        loadedChunks.put(player.getUUID(), chunksToLoad);

        List<InWorldNode> newNodes = new ArrayList<>(sd.getDynamicNodes().stream().filter(n -> {
            Vec3 nodePos = sd.getNodePositionOrCenter(n);
            // If is in chunksToLoad and not in chunks, then it has not yet been loaded, but will be now.
            return chunksToLoad.includes(Mth.floor(nodePos.x) >> 4, Mth.floor(nodePos.z) >> 4) &&
                    !chunks.includes(Mth.floor(nodePos.x) >> 4, Mth.floor(nodePos.z) >> 4);
        }).toList());

        LongList chunksToRemove = new LongArrayList(viewDistance);
        chunks.supplyDiff(chunksToLoad, chunksToRemove::add, chunk -> sd.getNodesInChunk(chunk, newNodes::add));

        for (InWorldNode node : newNodes) {
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

        for (ServerPlayer player : level.getPlayers(p -> true)) {
            boolean unloaded = true;
            for (long loadedChunk : loadedChunks.getOrDefault(player.getUUID(), ChunkCuboidEntry.ZERO)) {
                if (loadedChunk == chunk1 || loadedChunk == chunk2) {
                    CatnipServices.NETWORK.sendToClient(player, SendWireConnectionsPacket.connectWire(connection, data));
                    unloaded = false;
                    break;
                }
            }

            if (unloaded) {
                CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearWire(connection));
            }
        }
    }

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

        public ChunkCuboidEntry(int xOrigin, int zOrigin, int range) {
            minX = xOrigin - range;
            maxX = xOrigin + range + 1;

            minZ = zOrigin - range;
            maxZ = zOrigin + range + 1;
        }

        public void supplyDiff(ChunkCuboidEntry other, LongConsumer removalConsumer, LongConsumer additionConsumer) {
            int totalMinX = Math.min(minX, other.minX);
            int totalMinZ = Math.min(minZ, other.minZ);
            int totalMaxX = Math.max(maxX, other.maxX);
            int totalMaxZ = Math.max(maxZ, other.maxZ);
            for (int x = totalMinX; x <= totalMaxX; x++) {
                for (int z = totalMinZ; z <= totalMaxZ; z++) {
                    if (includes(x, z) && !other.includes(x, z))
                        removalConsumer.accept(ChunkPos.asLong(x, z));
                    else if (!includes(x, z) && other.includes(x, z))
                        additionConsumer.accept(ChunkPos.asLong(x, z));
                }
            }
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

        @Override
        public @NotNull LongIterator iterator() {
            return new LongIterator() {
                int x = minX - 1;
                int z = minZ;

                @Override
                public long nextLong() {
                    x++;
                    if (x >= maxX) {
                        x = minX;
                        z++;
                    }
                    return ChunkPos.asLong(x, z);
                }

                @Override
                public boolean hasNext() {
                    return z < maxZ;
                }
            };
        }
    }

}
