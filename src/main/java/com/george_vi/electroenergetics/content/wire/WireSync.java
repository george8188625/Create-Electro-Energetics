package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.ClearCatenaryPacket;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.SendCatenaryPacket;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class WireSync {
    public static Map<UUID, List<ChunkPos>> loadedChunks = new HashMap<>();
    static final int renderDistance = 20;

    public static void handlePlayerEnterNewSection(ServerPlayer player, ChunkPos newPos) {
        List<ChunkPos> chunks = loadedChunks.computeIfAbsent(player.getUUID(), uuid -> new ArrayList<>());

        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) player.level());

        List<ChunkPos> newChunks = new ArrayList<>();
        List<ChunkPos> chunksToLoad = new ArrayList<>();
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                ChunkPos chunk = new ChunkPos(x + newPos.x, z + newPos.z);
                chunksToLoad.add(chunk);
                if (!chunks.contains(chunk))
                    newChunks.add(chunk);
            }
        }

        List<InWorldNode> newNodes = new ArrayList<>(sd.getNodes().stream().filter(n -> newChunks.contains(new ChunkPos(n.sourcePos()))).toList());

        for (InWorldNode node : newNodes) {
            for (InWorldNodeConnection connection : sd.getConnections(node)) {
                if (chunks.contains(new ChunkPos(connection.node2().sourcePos())))
                    continue;
                CatnipServices.NETWORK.sendToClient(player, SendWireConnectionsPacket.connectWire(connection, sd.getConnectionData(connection)));
            }
        }

        List<CatenaryConnection> newCatenary = sd.getAllCatenaryConnections().stream().filter(c -> (newChunks.contains(new ChunkPos(c.pos1())) && !chunks.contains(new ChunkPos(c.pos2()))) ||
                (newChunks.contains(new ChunkPos(c.pos2())) && !chunks.contains(new ChunkPos(c.pos1())))).toList();

        CatnipServices.NETWORK.sendToClient(player, new SendCatenaryPacket(newCatenary));

        chunks.addAll(newChunks);
        List<ChunkPos> chunksToRemove = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            ChunkPos chunk = chunks.get(i);
            if (!chunksToLoad.contains(chunk))
                chunksToRemove.add(chunk);
        }

        for (ChunkPos chunkPos : chunksToRemove)
            chunks.remove(chunkPos);

        List<InWorldNode> nodesToRemove = new ArrayList<>(sd.getNodes().stream().filter(n -> chunksToRemove.contains(new ChunkPos(n.sourcePos()))).toList());
        List<InWorldNodeConnection> connectionsToRemove = new ArrayList<>();
        for (InWorldNode node : nodesToRemove) {
            for (InWorldNodeConnection connection : sd.getConnections(node)) {
                if (chunks.contains(new ChunkPos(connection.node2().sourcePos())))
                    continue;
                connectionsToRemove.add(connection);
            }
        }

        CatnipServices.NETWORK.sendToClient(player, new ClearWireConnectionsPacket(connectionsToRemove.stream().map(c -> Pair.of(c.node1(), c.node2())).toList(), false));

        List<CatenaryConnection> catenaryToRemove = sd.getAllCatenaryConnections().stream().filter(c -> (chunksToRemove.contains(new ChunkPos(c.pos1())) && !chunks.contains(new ChunkPos(c.pos2()))) ||
                (chunksToRemove.contains(new ChunkPos(c.pos2())) && !chunks.contains(new ChunkPos(c.pos1())))).toList();

        CatnipServices.NETWORK.sendToClient(player, new ClearCatenaryPacket(catenaryToRemove, false));
    }

    public static void handleWireRemoved(InWorldNodeConnection connection, ServerLevel level) {
        ChunkPos chunk1 = new ChunkPos(connection.node1().sourcePos());
        ChunkPos chunk2 = new ChunkPos(connection.node2().sourcePos());
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (ChunkPos loadedChunk : loadedChunks.getOrDefault(player.getUUID(), Collections.emptyList()))
                if (loadedChunk.equals(chunk1) || loadedChunk.equals(chunk2)) {
                    CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearWire(connection));
                    break;
                }
        }
    }

    public static void handleWireAdded(InWorldNodeConnection connection, WireData data, ServerLevel level) {
        ChunkPos chunk1 = new ChunkPos(connection.node1().sourcePos());
        ChunkPos chunk2 = new ChunkPos(connection.node2().sourcePos());
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (ChunkPos loadedChunk : loadedChunks.getOrDefault(player.getUUID(), Collections.emptyList()))
                if (loadedChunk.equals(chunk1) || loadedChunk.equals(chunk2)) {
                    CatnipServices.NETWORK.sendToClient(player, SendWireConnectionsPacket.connectWire(connection, data));
                    break;
                }
        }
    }

    public static void handleCatenaryRemoved(BlockPos pos1, BlockPos pos2, ServerLevel level) {
        ChunkPos chunk1 = new ChunkPos(pos1);
        ChunkPos chunk2 = new ChunkPos(pos2);
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (ChunkPos loadedChunk : loadedChunks.getOrDefault(player.getUUID(), Collections.emptyList()))
                if (loadedChunk.equals(chunk1) || loadedChunk.equals(chunk2)) {
                    CatnipServices.NETWORK.sendToClient(player, ClearCatenaryPacket.clearWire(new CatenaryConnection(pos1, pos2)));
                    break;
                }
        }
    }

    public static void handleCatenaryAdded(BlockPos pos1, BlockPos pos2, ServerLevel level) {
        ChunkPos chunk1 = new ChunkPos(pos1);
        ChunkPos chunk2 = new ChunkPos(pos2);
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            for (ChunkPos loadedChunk : loadedChunks.getOrDefault(player.getUUID(), Collections.emptyList()))
                if (loadedChunk.equals(chunk1) || loadedChunk.equals(chunk2)) {
                    CatnipServices.NETWORK.sendToClient(player, SendCatenaryPacket.connectWire(new CatenaryConnection(pos1, pos2)));
                    break;
                }
        }
    }

    public static void unloadForPlayer(ServerPlayer player) {
        loadedChunks.remove(player.getUUID());
        CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearAll());
        CatnipServices.NETWORK.sendToClient(player, ClearCatenaryPacket.clearAll());
    }
}
