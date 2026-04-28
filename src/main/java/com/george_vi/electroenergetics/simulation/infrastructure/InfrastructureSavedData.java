package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.companion.SableCompanion;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class InfrastructureSavedData extends SavedData {
    public ArrayList<InWorldNodeConnection> sableToUpdate = new ArrayList<>();
    Map<InWorldNode, List<InWorldNode>> NODES = new HashMap<>();
    Map<InWorldNode, Vec3> NODE_POSITIONS = new HashMap<>();
    Map<InWorldNode, Vec3> LOCAL_NODE_POSITIONS = new HashMap<>();
    // These nodes are in sublevels and their positions are updated frequently
    public Set<InWorldNode> DYNAMIC_POSITION_NODES = new HashSet<>();
    Map<BlockPos, List<InWorldNode>> NODES_BY_POS = new HashMap<>();
    Map<InWorldNodeConnection, WireData> CONNECTION_DATA = new HashMap<>();
    /**
     * Only non-dynamic nodes!
     */
    Long2ObjectMap<Set<InWorldNode>> NODES_BY_CHUNK = new Long2ObjectOpenHashMap<>();

    // Trains
    Map<BlockPos, List<BlockPos>> CATENARY_ADJACENCY = new HashMap<>();
    Map<CatenaryConnection, CatenaryConnectionData> CATENARY_DATA = new HashMap<>();

    public ServerLevel level;
    public SimulationTicker ticker;
    public DevicesSavedData deviceSD;

    // Wire infrastructure
    public WireSimulationState wireSimulationState;
    public WireAssemblerModule wireAssemblerModule;
    public WireLifetimeModule wireLifetimeModule;
    public WireCrossContactModule wireCrossContactModule;
    public CatenaryModule catenaryModule;
    public WireElectrocutionModule wireElectrocutionModule;
    public WireSync wireSync;

    public static final Logger LOGGER = LogUtils.getLogger();

    private InfrastructureSavedData(ServerLevel level) {
        this.level = level;
        wireSimulationState = new WireSimulationState(this, level);
        wireAssemblerModule = new WireAssemblerModule(this, level, this.wireSimulationState);
        wireLifetimeModule = new WireLifetimeModule(this, level, this.wireSimulationState);
        wireCrossContactModule = new WireCrossContactModule(this, level, this.wireSimulationState);
        catenaryModule = new CatenaryModule(this, level, this.wireSimulationState);
        wireElectrocutionModule = new WireElectrocutionModule(this, level, this.wireSimulationState);
        ticker = new SimulationTicker(level, this);
        wireSync = new WireSync(this, level);
        deviceSD = DevicesSavedData.load(level);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {

        // Save Nodes / Node Connections

        ListTag nodeList = new ListTag();
        for (Map.Entry<InWorldNode, List<InWorldNode>> e : NODES.entrySet()) {
            CompoundTag nodeTag = new CompoundTag();

            nodeTag.put("Pos", NbtUtils.writeBlockPos(e.getKey().sourcePos()));
            nodeTag.putInt("ID", e.getKey().id());
            nodeTag.putBoolean("DynamicPos", DYNAMIC_POSITION_NODES.contains(e.getKey()));

            Vec3 lastKnownPos = getNodePosition(e.getKey());
            if (lastKnownPos != null) {
                CompoundTag lastKnownPosTag = new CompoundTag();
                lastKnownPosTag.putDouble("X", lastKnownPos.x);
                lastKnownPosTag.putDouble("Y", lastKnownPos.y);
                lastKnownPosTag.putDouble("Z", lastKnownPos.z);
                nodeTag.put("LastKnownPos", lastKnownPosTag);
            }
            ListTag connectedNodesList = new ListTag();
            for (InWorldNode node : e.getValue()) {
                CompoundTag subNodeTag = new CompoundTag();

                subNodeTag.put("Pos", NbtUtils.writeBlockPos(node.sourcePos()));
                subNodeTag.putInt("ID", node.id());
                WireData connectionData = CONNECTION_DATA.get(new InWorldNodeConnection(e.getKey(), node));

                if (connectionData == null)
                    continue;

                ListTag attachmentList = new ListTag();
                for (Pair<Float, WireAttachment> attachment : connectionData.attachments()) {
                    CompoundTag attachmentTag = attachment.getSecond().write();
                    attachmentTag.putFloat("Point", attachment.getFirst());
                    attachmentList.add(attachmentTag);
                }
                subNodeTag.put("Attachments", attachmentList);

                subNodeTag.putString("WireType", CEERegistries.WIRE_TYPE.getKey(connectionData.wireType()).toString());
                subNodeTag.putFloat("Temperature", connectionData.temperature());
                subNodeTag.putDouble("Length", connectionData.length);

                connectedNodesList.add(subNodeTag);
            }

            nodeTag.put("ConnectedNodes", connectedNodesList);
            nodeList.add(nodeTag);
        }
        compoundTag.put("Nodes", nodeList);

        // Save Railway Catenary

        ListTag catenaryList = new ListTag();
        for (Map.Entry<BlockPos, List<BlockPos>> e : CATENARY_ADJACENCY.entrySet()) {
            CompoundTag catenaryTag = new CompoundTag();
            ListTag connectionList = new ListTag();
            for (BlockPos otherPos : e.getValue()) {
                CompoundTag tg = new CompoundTag();
                tg.put("Pos", NbtUtils.writeBlockPos(otherPos));
                CatenaryConnectionData data = CATENARY_DATA.get(new CatenaryConnection(e.getKey(), otherPos));
                tg.putFloat("Temperature", data.temperature);
                tg.putBoolean("IsLow", data.isLow);
                tg.putDouble("Length", data.length);
                connectionList.add(tg);
            }
            catenaryTag.put("From", NbtUtils.writeBlockPos(e.getKey()));
            catenaryTag.put("Connections", connectionList);
            catenaryList.add(catenaryTag);
        }
        compoundTag.put("Catenary", catenaryList);

        return compoundTag;
    }

    static InfrastructureSavedData load(ServerLevel level, CompoundTag compoundTag, HolderLookup.Provider provider) {
        InfrastructureSavedData sd = new InfrastructureSavedData(level);
        sd.NODES = new HashMap<>();
        sd.NODES_BY_POS = new HashMap<>();
        sd.CONNECTION_DATA = new HashMap<>();
        sd.CATENARY_ADJACENCY = new HashMap<>();
        sd.CATENARY_DATA = new HashMap<>();
        // Read Nodes / Node Connections

        NBTHelper.iterateCompoundList(compoundTag.getList("Nodes", Tag.TAG_COMPOUND), tag -> {

            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            int id = tag.getInt("ID");

            CompoundTag lastKnownPosTag = tag.getCompound("LastKnownPos");
            CompoundTag lastKnownLocalPosTag = tag.getCompound("LastKnownLocalPos");
            Vec3 lastKnownPos = tag.contains("LastKnownPos") ? new Vec3(lastKnownPosTag.getDouble("X"), lastKnownPosTag.getDouble("Y"), lastKnownPosTag.getDouble("Z")) : null;
            Vec3 lastKnownLocalPos = tag.contains("LastKnownLocalPos") ? new Vec3(lastKnownLocalPosTag.getDouble("X"), lastKnownLocalPosTag.getDouble("Y"), lastKnownLocalPosTag.getDouble("Z")) : null;
            List<InWorldNode> connectedNodes = new ArrayList<>();
            InWorldNode node = new InWorldNode(id, pos);
            NBTHelper.iterateCompoundList(tag.getList("ConnectedNodes", Tag.TAG_COMPOUND), connectionTag -> {
                InWorldNode connectedNode = new InWorldNode(connectionTag.getInt("ID"), NBTHelper.readBlockPos(connectionTag, "Pos"));
                if (connectedNode.equals(node)) {
                    LOGGER.warn("Could not load a wire connection, as both ends connect to a single node: {}, skipping...", connectedNode);
                    return;
                }

                connectedNodes.add(connectedNode);
                WireType wireType = CEERegistries.WIRE_TYPE.get(ResourceLocation.tryParse(connectionTag.getString("WireType")));
                if (wireType == null) {
                    LOGGER.warn("Could not load wire type between nodes: {}, {} with id: {} in: {}, changing to standard...", node, connectedNode, connectionTag.getString("WireType"), level.dimension().location());
                    wireType = CEEWireTypes.STANDARD.get();
                }


                List<Pair<Float, WireAttachment>> attachments = new ArrayList<>();
                NBTHelper.iterateCompoundList(connectionTag.getList("Attachments", Tag.TAG_COMPOUND), attachmentTag -> {
                    float point = attachmentTag.getFloat("Point");
                    try {
                        WireAttachment attachment = WireAttachment.read(attachmentTag);
                        attachments.add(Pair.of(point, attachment));
                    } catch (Throwable ignored) {
                        LOGGER.warn("Could not load wire attachment: {}", attachmentTag.getString("ID"));
                    }
                });

                InWorldNodeConnection connection = new InWorldNodeConnection(new InWorldNode(connectionTag.getInt("ID"), NBTHelper.readBlockPos(connectionTag, "Pos")), new InWorldNode(id, pos));
                WireData wireData = new WireData(wireType, Float.isNaN(connectionTag.getFloat("Temperature")) ? 0f : connectionTag.getFloat("Temperature"), attachments, connectionTag.getDouble("Length"));

                sd.CONNECTION_DATA.computeIfAbsent(connection, c -> wireData);
            });
            sd.NODE_POSITIONS.put(node, lastKnownPos == null ? node.sourcePos().getCenter() : lastKnownPos);
            sd.LOCAL_NODE_POSITIONS.put(node, lastKnownLocalPos == null ? VecHelper.CENTER_OF_ORIGIN : lastKnownLocalPos);
            sd.NODES.put(node, connectedNodes);
            if (tag.getBoolean("DynamicPos"))
                sd.DYNAMIC_POSITION_NODES.add(node);
            sd.NODES_BY_POS.computeIfAbsent(pos, ((p) -> new ArrayList<>()));
            sd.NODES_BY_POS.computeIfPresent(pos, ((p, nodes) -> {
                nodes.add(node);
                return nodes;
            }));
        });

        // Migrate devices from old versions

        if (compoundTag.contains("Devices", Tag.TAG_LIST)) {
            DevicesSavedData deviceSD = DevicesSavedData.load(level);
            migrateFromLegacy(compoundTag.getList("Devices", Tag.TAG_COMPOUND), deviceSD);
        }

        // Read Railway Catenary

        NBTHelper.iterateCompoundList(compoundTag.getList("Catenary", Tag.TAG_COMPOUND), tag -> {
            BlockPos from = NBTHelper.readBlockPos(tag, "From");

            // Legacy
            tag.getList("Connections", Tag.TAG_INT_ARRAY).forEach(tg -> {
                int[] arr = ((IntArrayTag)tg).getAsIntArray();
                if (arr.length != 3) {
                    LOGGER.warn("Could not load catenary connection, invalid position, at pos: {} in: {}", from.toShortString(), level.dimension().location());
                    return;
                }

                BlockPos to = new BlockPos(arr[0], arr[1], arr[2]);

                sd.CATENARY_ADJACENCY.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                CatenaryConnectionData catenaryData = new CatenaryConnectionData(0, false, 0);
                sd.CATENARY_DATA.put(new CatenaryConnection(from, to), catenaryData);

            });

            NBTHelper.iterateCompoundList(tag.getList("Connections", Tag.TAG_COMPOUND), tg -> {
                int[] arr = tg.getIntArray("Pos");
                if (arr.length != 3) {
                    LOGGER.warn("Could not load catenary connection, invalid position, at pos: {} in: {}", from.toShortString(), level.dimension().location());
                    return;
                }

                BlockPos to = new BlockPos(arr[0], arr[1], arr[2]);

                sd.CATENARY_ADJACENCY.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                boolean isLow = tg.getBoolean("IsLow");
                CatenaryConnectionData catenaryData = new CatenaryConnectionData(Float.isNaN(tg.getFloat("Temperature")) ? 0f : tg.getFloat("Temperature"), isLow, tg.getDouble("Length"));
                sd.CATENARY_DATA.put(new CatenaryConnection(from, to), catenaryData);
            });
        });
        return sd;
    }

    public static boolean isDynamicPosition(ServerLevel level, InWorldNode node) {
        return SableCompanion.INSTANCE.getContaining(level, node.sourcePos()) != null;
    }

    boolean simulationInitialized = false;
    private void loadSimulationState() {
        if (simulationInitialized)
            return;
        if (NODES.isEmpty() && CONNECTION_DATA.isEmpty() && CATENARY_DATA.isEmpty())
            return;

        simulationInitialized = true;

        for (InWorldNode node : NODES.keySet()) {
            onNodeUpdateOrCreate(node);
        }

        for (Map.Entry<InWorldNodeConnection, WireData> e : CONNECTION_DATA.entrySet()) {
            wireSimulationState.addConnection(e.getKey(), e.getValue(), true);
        }

        for (Map.Entry<CatenaryConnection, CatenaryConnectionData> e : CATENARY_DATA.entrySet()) {
            InWorldNodeConnection connection = new InWorldNodeConnection(
                    new InWorldNode(0, e.getKey().pos1()), new InWorldNode(0, e.getKey().pos2()));
            wireSimulationState.addCatenaryConnection(connection, e.getValue(), true);
        }

        wireSimulationState.onNodeChange(NODES.keySet());
    }

    public static void migrateFromLegacy(ListTag legacyDevices, DevicesSavedData deviceSD) {
        if (legacyDevices.isEmpty())
            return;

        for (Tag rawTag : legacyDevices) {
            CompoundTag tag = (CompoundTag) rawTag;
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            ResourceLocation id = ResourceLocation.parse(tag.getString("ID"));
            CompoundTag extraData = tag.getCompound("ExtraData");

            SimulatedDeviceType<?> type = CEERegistries.SIMULATED_DEVICE_TYPE.get(id);
            if (type == null) {
                LOGGER.warn("Legacy migration: unknown device type {} at {}, skipping", id, pos.toShortString());
                continue;
            }

            if (deviceSD.getDevice(pos) != null) continue;

            deviceSD.addDevice(type, pos, extraData);
            LOGGER.info("Migrated legacy device {} at {}", id, pos.toShortString());
        }
    }

    public static InfrastructureSavedData load(ServerLevel level) {
        InfrastructureSavedData sd = level.getDataStorage()
                .computeIfAbsent(new Factory<>(() -> new InfrastructureSavedData(level),
                        (compoundTag, provider) ->
                                InfrastructureSavedData.load(level, compoundTag, provider)),
                        "electroenergetics_infrastructure");
        sd.loadSimulationState();
        return sd;
    }

    public void registerOrUpdateNodes(BlockPos pos, List<Integer> nodeIDs) {
        List<InWorldNode> nodes = new ArrayList<>(nodeIDs.size());
        List<InWorldNode> oldNodesList = NODES_BY_POS.put(pos, nodes);
        Set<InWorldNode> oldNodes = oldNodesList == null ? new HashSet<>() : new HashSet<>(oldNodesList);

        // Update node positions
        for (int id : nodeIDs) {
            InWorldNode node = new InWorldNode(id, pos);
            oldNodes.remove(node);
            nodes.add(node);
            NODES.putIfAbsent(node, new ArrayList<>());
            if (isDynamicPosition(level, node))
                DYNAMIC_POSITION_NODES.add(node);
            Vec3 newPos = node.getLocalPosition(level);
            if (newPos == null)
                newPos = VecHelper.CENTER_OF_ORIGIN;
            if (!Objects.equals(newPos, LOCAL_NODE_POSITIONS.put(node, newPos))) {
                Vec3 globalPos = node.toGlobalPos(newPos, level);
                NODE_POSITIONS.put(node, globalPos);
                onNodeUpdateOrCreate(node);
                onNodePosUpdate(node, globalPos);
                for (InWorldNodeConnection connection : getConnections(node)) {
                    wireSimulationState.removeConnection(connection);
                    wireSimulationState.addConnection(connection, getConnectionData(connection), false);
                }
            }
        }

        // All forgotten nodes are going to be removed
        for (InWorldNode node : oldNodes) {
            for (InWorldNodeConnection connection : getConnections(node))
                removeAndDropConnection(connection);
            removeNode(node);
        }

        // Update catenary isLow
        List<BlockPos> catenaryConnections = CATENARY_ADJACENCY.get(pos);
        if (catenaryConnections != null) {
            for (BlockPos neighbour : catenaryConnections) {
                BlockState startingState = level.getBlockState(pos);
                BlockState endingState = level.getBlockState(neighbour);
                boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) &&
                        startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
                boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) &&
                        endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
                boolean isLow = isStartingLow || isEndingLow;
                CatenaryConnectionData connectionData = CATENARY_DATA.get(new CatenaryConnection(pos, neighbour));
                if (connectionData != null && connectionData.isLow != isLow) {
                    connectionData.isLow = isLow;
                    InWorldNodeConnection nodeConnection = new InWorldNodeConnection(
                            new InWorldNode(0, pos), new InWorldNode(0, neighbour));
                    wireSimulationState.removeConnection(nodeConnection);
                    wireSimulationState.addCatenaryConnection(nodeConnection, connectionData, false);
                }
            }
        }
        wireSimulationState.onNodeChange(NODES.keySet());
    }

    public void tick() {
        updateAllNodePositions();

        for (InWorldNodeConnection connection : sableToUpdate) {
            WireData connectionData = getConnectionData(connection);
            if (connectionData != null)
                setConnectionData(connection, connectionData);
        }
    }

    private void updateAllNodePositions() {
        // for Sable
        List<InWorldNodeConnection> connectionsToUpdate = new ArrayList<>();
        for (InWorldNode node : DYNAMIC_POSITION_NODES) {
            Vec3 localPos = LOCAL_NODE_POSITIONS.computeIfAbsent(node, (n) -> VecHelper.CENTER_OF_ORIGIN);
            if (!node.isFullyLoadable(level))
                continue;
            Vec3 pos = node.toGlobalPos(localPos, level);
            if (pos == null)
                continue;
            NODE_POSITIONS.compute(node, (n, p) -> {
                if (p == null || p.distanceToSqr(pos) > 0.003) {
                    getConnections(node, connectionsToUpdate::add);
                    return pos;
                }
                return p;
            });

        }
        for (InWorldNodeConnection connection : connectionsToUpdate) {
            WireData connectionData = getConnectionData(connection);
            if (connectionData == null)
                continue;

            if (wireSimulationState.relocateConnection(connection, connectionData))
                removeAndDropConnection(connection);
            else
                wireSync.handleWireRepositioned(connection, connectionData);
        }
    }

    /**
     * Returns all connections of the specified node
     */
    public List<InWorldNodeConnection> getConnections(InWorldNode node) {
        List<InWorldNodeConnection> connections = new ArrayList<>();
        getConnections(node, connections::add);
        return connections;
    }

    /**
     * Supplies all connections of the specified node to the consumer
     */
    public void getConnections(InWorldNode node, Consumer<InWorldNodeConnection> consumer) {
        List<InWorldNode> connectedNodes = NODES.get(node);
        if (connectedNodes == null)
            return;

        for (InWorldNode connectedNode : connectedNodes)
            consumer.accept(new InWorldNodeConnection(node, connectedNode));
    }

    /**
     * Removes the specified connection.
     * @return The previous {@link WireData} if the connection existed. Otherwise, null.
     */
    public WireData removeConnection(InWorldNodeConnection connection) {

        NODES.get(connection.node1()).remove(connection.node2());
        NODES.get(connection.node2()).remove(connection.node1());
        WireData connectionData = CONNECTION_DATA.remove(connection);
        if (connectionData != null) {
            for (Pair<Float, WireAttachment> attachment : connectionData.attachments()) {
                Vec3 pos = QuadraticWireHelper.posAt(Vec3.atCenterOf(connection.node1().sableSourcePos(level)),
                        Vec3.atCenterOf(connection.node2().sableSourcePos(level)), attachment.getFirst(),
                        connectionData.wireType().getSag());

                for (ItemStack stack : attachment.getSecond().getDrops(level))
                    Containers.dropItemStack(level, pos.x(), pos.y(), pos.z(), stack);

            }
        }

        wireSync.handleWireRemoved(connection);
        wireSimulationState.removeConnection(connection);
//        wireConnectionManager.wireRemoved(connection);
        setDirty();
        return connectionData;
    }

    public Map<InWorldNodeConnection, WireData> getAllConnections() {
        return CONNECTION_DATA;
    }

    /**
     * Creates the node connection at the specified position.
     * @return The {@link InWorldNodeConnection} object describing the wire connection.
     */
    public InWorldNodeConnection connect(InWorldNode node1, InWorldNode node2, WireType wireType) {
        if (node1.equals(node2))
            throw new IllegalArgumentException("Tried to connect a wire between a single node: " + node1);

        List<InWorldNode> adjacency1 = NODES.get(node1);
        List<InWorldNode> adjacency2 = NODES.get(node2);
        if (adjacency1 == null)
            throw new IllegalArgumentException("Node: " + node1 + "doesn't exist.");
        else if (adjacency2 == null)
            throw new IllegalArgumentException("Node: " + node2 + "doesn't exist.");
        adjacency1.add(node2);
        adjacency2.add(node1);

        InWorldNodeConnection connection = new InWorldNodeConnection(node1, node2);
        WireData data = new WireData(wireType, 0f, Collections.emptyList(), 0);
        CONNECTION_DATA.put(connection, data);
        setDirty();

        wireSimulationState.addConnection(connection, data, false);
        wireSync.handleWireAdded(connection, data);
        return connection;
    }

    /**
     * Creates a connection, but doesn't update it. Should be updated afterward. Internal. Don't use.
     */
    public InWorldNodeConnection connectNoUpdate(InWorldNode node1, InWorldNode node2, WireData data) {
        if (node1.equals(node2))
            throw new IllegalArgumentException("Tried to connect a wire between a single node: " + node1);

        List<InWorldNode> adjacency1 = NODES.get(node1);
        List<InWorldNode> adjacency2 = NODES.get(node2);
        if (adjacency1 == null)
            throw new IllegalArgumentException("Node: " + node1 + "doesn't exist.");
        else if (adjacency2 == null)
            throw new IllegalArgumentException("Node: " + node2 + "doesn't exist.");
        adjacency1.add(node2);
        adjacency2.add(node1);
        InWorldNodeConnection connection = new InWorldNodeConnection(node1, node2);
        CONNECTION_DATA.put(connection, data);
        setDirty();
        return connection;
    }

    public boolean isConnected(InWorldNode node1, InWorldNode node2) {
        return NODES.getOrDefault(node1, Collections.emptyList()).contains(node2) ||
                (node1.id() == 0 && node2.id() == 0 && CATENARY_ADJACENCY
                        .getOrDefault(node1.sourcePos(), Collections.emptyList()).contains(node2.sourcePos()));
    }

    public WireData getConnectionData(InWorldNodeConnection connection) {
        return CONNECTION_DATA.get(connection);
    }

    public void setConnectionData(InWorldNodeConnection connection, WireData data) {
        wireSync.handleWireAdded(connection, data);
        wireSimulationState.removeConnection(connection);
        wireSimulationState.addConnection(connection, data, false);
        CONNECTION_DATA.put(connection, data);
    }

    /**
     * Returns all nodes at the specified position.
     */
    public List<InWorldNode> getNodesAt(BlockPos pos) {
        return NODES_BY_POS.getOrDefault(pos, Collections.emptyList());
    }

    /**
     * @return null if the node doesn't exist, otherwise returns the node.
     */
    public InWorldNode getNode(BlockPos pos, int id) {
        List<InWorldNode> nodes = NODES_BY_POS.get(pos);
        if (nodes != null)
            for (InWorldNode node : nodes)
                if (node.id() == id)
                    return node;
        return null;
    }

    /**
     * @return All nodes
     */
    public Set<InWorldNode> getNodes() {
        return NODES.keySet();
    }

    /**
     * @return All dynamically-positioned nodes
     */
    public Set<InWorldNode> getDynamicNodes() {
        return DYNAMIC_POSITION_NODES;
    }

    /**
     * Tries to update the global node position and returns it.
     * @return node position or center if it can't find the position.
     */
    public Vec3 getNodePositionOrCenter(InWorldNode node) {
        Vec3 pos = getNodePosition(node);
        return pos == null ? node.sourcePos().getCenter() : pos;
    }

    /**
     * Tries to update the global node position and returns it.
     * @return null if it can't find the position.
     */
    public Vec3 getNodePosition(InWorldNode node) {
        Vec3 pos = NODE_POSITIONS.get(node);
        if (pos == null || level.isLoaded(node.sourcePos())) {
            Vec3 newPos = node.getLocalPosition(level);

            if (!node.isFullyLoadable(level))
                return pos;

            if (newPos == null)
                newPos = VecHelper.CENTER_OF_ORIGIN;

            LOCAL_NODE_POSITIONS.put(node, newPos);
            pos = node.toGlobalPos(newPos, level);

            Vec3 finalPos = pos;
            NODE_POSITIONS.compute(node, (n, v) -> {
                if (v != null && v.distanceToSqr(finalPos) <= 0.003)
                    return v;
                onNodePosUpdate(node, finalPos);
                return finalPos;
            });
        }
        return pos;
    }

    /**
     * Tries to update the local node position and returns it.
     * @return null if it can't find the position.
     */
    public Vec3 getLocalNodePosition(InWorldNode node) {
        Vec3 pos = LOCAL_NODE_POSITIONS.get(node);
        if (pos == null || level.isLoaded(node.sourcePos())) {
            Vec3 newPos = node.getLocalPosition(level);

            if (newPos == null)
                newPos = VecHelper.CENTER_OF_ORIGIN;

            LOCAL_NODE_POSITIONS.put(node, pos = newPos);
            if (node.isFullyLoadable(level)) {
                Vec3 finalPos = node.toGlobalPos(newPos, level);
                NODE_POSITIONS.compute(node, (n, v) -> {
                    if (v != null && v.distanceToSqr(finalPos) <= 0.003)
                        return v;
                    onNodePosUpdate(node, finalPos);
                    return finalPos;
                });
            }
        }
        return pos;
    }

    /**
     * Creates the specified catenary connection.
     */
    public void connectCatenary(BlockPos pos1, BlockPos pos2) {
        CATENARY_ADJACENCY.computeIfAbsent(pos1, k -> new ArrayList<>()).add(pos2);
        CATENARY_ADJACENCY.computeIfAbsent(pos2, k -> new ArrayList<>()).add(pos1);
        BlockState startingState = level.getBlockState(pos1);
        BlockState endingState = level.getBlockState(pos2);
        boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean isLow = isStartingLow || isEndingLow;

        CatenaryConnectionData data = new CatenaryConnectionData(0, isLow, 0);
        CATENARY_DATA.put(new CatenaryConnection(pos1, pos2), data);
        InWorldNode node1 = new InWorldNode(0, pos1);
        InWorldNode node2 = new InWorldNode(0, pos2);
        wireSimulationState.addCatenaryConnection(new InWorldNodeConnection(node1, node2), data, false);
        wireSync.handleCatenaryAdded(pos1, pos2);
    }

    /**
     * Removes the specified catenary connection.
     */
    public void removeCatenary(BlockPos pos1, BlockPos pos2) {
        CATENARY_ADJACENCY.computeIfAbsent(pos1, k -> new ArrayList<>()).remove(pos2);
        CATENARY_ADJACENCY.computeIfAbsent(pos2, k -> new ArrayList<>()).remove(pos1);
        CATENARY_DATA.remove(new CatenaryConnection(pos1, pos2));
        InWorldNode node1 = new InWorldNode(0, pos1);
        InWorldNode node2 = new InWorldNode(0, pos2);
        wireSimulationState.removeConnection(new InWorldNodeConnection(node1, node2));
        wireSync.handleCatenaryRemoved(pos1, pos2);
    }

    /**
     * Creates the specified node at block center.
     * You usually shouldn't call it yourself.
     */
    public void createNode(InWorldNode node) {
        createNode(node, node.sourcePos().getCenter(), VecHelper.CENTER_OF_ORIGIN);
    }

    /**
     * Creates the specified node with the specified position.
     * You usually shouldn't call it yourself.
     */
    public void createNode(InWorldNode node, Vec3 pos, Vec3 localPos) {
        NODES.putIfAbsent(node, new ArrayList<>());
        NODE_POSITIONS.putIfAbsent(node, pos);
        LOCAL_NODE_POSITIONS.putIfAbsent(node, localPos);
        NODES_BY_POS.computeIfAbsent(node.sourcePos(), k -> new ArrayList<>()).add(node);
        wireSimulationState.onNodeChange(NODES.keySet());

        if (isDynamicPosition(level, node))
            DYNAMIC_POSITION_NODES.add(node);

        onNodeUpdateOrCreate(node);
        onNodePosUpdate(node, pos);
    }

    public void removeNodes(BlockPos pos) {
        List<InWorldNode> nodes = NODES_BY_POS.remove(pos);
        if (nodes == null)
            return;

        for (InWorldNode node : nodes) {
            for (InWorldNodeConnection connection : getConnections(node))
                removeAndDropConnection(connection);
            removeNode(node);
        }

        List<BlockPos> catenaryConnections = List.copyOf(CATENARY_ADJACENCY.getOrDefault(pos, new ArrayList<>()));
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.COPPER_WIRE.asStack((catenaryConnections.size()) * CEEConfigs.server().wiresPerSpool.get()));
        for (BlockPos connection : catenaryConnections)
            removeCatenary(pos, connection);
        wireSimulationState.onNodeChange(NODES.keySet());
    }


    /**
     * Removes the specified node from the Infrastructure data and cleans it from all the data structures.
     * You usually shouldn't call it yourself.
     */
    public void removeNode(InWorldNode node) {
        NODES.remove(node);
        NODE_POSITIONS.remove(node);
        LOCAL_NODE_POSITIONS.remove(node);
        boolean wasDynamic = DYNAMIC_POSITION_NODES.remove(node);
        onNodeRemove(node, wasDynamic);
    }

    /**
     * Hook for node update
     */
    private void onNodeUpdateOrCreate(InWorldNode node) {
        if (InWorldNode.isFromSubLevel(level, node.sourcePos()))
            return;

        Set<InWorldNode> chunkNodes = NODES_BY_CHUNK.computeIfAbsent(ChunkPos.asLong(node.sourcePos()),
                c -> new HashSet<>());

        chunkNodes.add(node);
    }

    /**
     * Hook for node pos update
     */
    private void onNodePosUpdate(InWorldNode node, Vec3 newPos) {

    }

    /**
     * Hook for node removal
     */
    private void onNodeRemove(InWorldNode node, boolean wasDynamic) {
        if (wasDynamic)
            return;

        Set<InWorldNode> chunkNodes = NODES_BY_CHUNK.computeIfAbsent(ChunkPos.asLong(node.sourcePos()),
                c -> new HashSet<>());

        chunkNodes.remove(node);

        if (chunkNodes.isEmpty())
            NODES_BY_CHUNK.remove(ChunkPos.asLong(node.sourcePos()));
    }

    /**
     * Removes the specified connection and drops the items.
     * @param connection Connection to remove
     * @return removed connection wireData or null if connection doesn't exist
     */
    public WireData removeAndDropConnection(InWorldNodeConnection connection) {
        WireData data = removeConnection(connection);
        if (data == null)
            return null;
        int n = CEEConfigs.server().wiresPerSpool.get();
        Vec3 pos1 = getNodePositionOrCenter(connection.node1());
        Vec3 pos2 = getNodePositionOrCenter(connection.node2());
        for (int i = 0; i < n; i++) {
            Vec3 pos = QuadraticWireHelper.posAt(pos1, pos2, (float) i / n);
            Containers.dropItemStack(level, pos.x, pos.y, pos.z, new ItemStack(data.wireType().getDrops()));
        }
        return data;
    }

    public Set<CatenaryConnection> getAllCatenaryConnections() {
       return CATENARY_DATA.keySet();
    }

    public boolean hasNode(InWorldNode node) {
        return NODES.containsKey(node);
    }

    public void getNodesInChunk(long chunk, Consumer<InWorldNode> consumer) {
        Set<InWorldNode> nodes = NODES_BY_CHUNK.get(chunk);
        if (nodes != null)
            for (InWorldNode node : nodes)
                consumer.accept(node);
    }
}
