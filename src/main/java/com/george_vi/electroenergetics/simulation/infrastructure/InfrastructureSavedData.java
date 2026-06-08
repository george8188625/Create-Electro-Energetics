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
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeHelper;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeType;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.companion.SableCompanion;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class InfrastructureSavedData extends SavedData {
    public LongList sableToUpdate = new LongArrayList();
    public IntList sableNodesToUpdate = new IntArrayList();

    private final IntArrayList freeDetachedNodeIDs = new IntArrayList();
    private int lastDetachedNodeId = 0;

    // NODES:
    Map<InWorldNode, InWorldNodeData> ALL_NODES = new HashMap<>();
    List<InWorldNodeData> NODES_BY_ID = new ArrayList<>();
    private final IntStack freeNodeIDs = new IntArrayList();

    Map<BlockPos, List<InWorldNodeData>> NODES_BY_POS = new HashMap<>();

    // These nodes are in sublevels and their positions are updated frequently
    public Set<InWorldNodeData> DYNAMIC_POSITION_NODES = new HashSet<>();

    // Only non-dynamic nodes!
    Long2ObjectMap<Set<InWorldNode>> NODES_BY_CHUNK = new Long2ObjectOpenHashMap<>();

    // CONNECTIONS:
    // Packed longs are used instead of IWNC to join node identity with wires
    Long2ObjectMap<WireData> CONNECTION_DATA = new Long2ObjectOpenHashMap<>();

    // TRAINS:
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
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {

        // Save Nodes / Node Connections

        ListTag freeDetachedNodeList = new ListTag();
        for (int id : freeDetachedNodeIDs)
            freeDetachedNodeList.add(IntTag.valueOf(id));

        if (!freeDetachedNodeList.isEmpty())
            compoundTag.put("FreeDetachedNodes", freeDetachedNodeList);
        if (lastDetachedNodeId != 0)
            compoundTag.putInt("LastDetachedNode", lastDetachedNodeId);

        ListTag nodeList = new ListTag();
        for (Map.Entry<InWorldNode, InWorldNodeData> e : ALL_NODES.entrySet()) {
            InWorldNode node = e.getKey();
            InWorldNodeData nodeData = e.getValue();

            CompoundTag nodeTag = new CompoundTag();

            nodeTag.put("Pos", NbtUtils.writeBlockPos(node.sourcePos()));
            nodeTag.putInt("ID", node.id());
            nodeTag.putBoolean("DynamicPos", nodeData.isDynamic);
            if (nodeData.detachedNodeType != null) {
                nodeTag.putString("DetachedNodeType", nodeData.detachedNodeType.getSerializedName());
                if (nodeData.detachedNodeEntityId != null)
                    nodeTag.putUUID("DetachedNodeUUID", nodeData.detachedNodeEntityId);
            }

            if (nodeData.label != null)
                nodeTag.putString("Label", nodeData.label);

            Vec3 lastKnownPos = getNodePosition(node);
            if (lastKnownPos != null) {
                CompoundTag lastKnownPosTag = new CompoundTag();
                lastKnownPosTag.putDouble("X", lastKnownPos.x);
                lastKnownPosTag.putDouble("Y", lastKnownPos.y);
                lastKnownPosTag.putDouble("Z", lastKnownPos.z);
                nodeTag.put("LastKnownPos", lastKnownPosTag);
            }

            ListTag connectedNodesList = new ListTag();

            // Save adjacent nodes and connections.
            nodeData.adjacency.forEach((adjacentNodeID, connectionData) -> {
                InWorldNodeData adjacentNodeData = NODES_BY_ID.get(adjacentNodeID);
                if (adjacentNodeData == null) {
                    // This should never happen, but if it does, oh well
                    LOGGER.warn("Could not save wire connection. connection to an invalid node, skipping");
                    return;
                }
                InWorldNode adjacentNode = adjacentNodeData.node;

                CompoundTag subNodeTag = new CompoundTag();

                subNodeTag.put("Pos", NbtUtils.writeBlockPos(adjacentNode.sourcePos()));
                subNodeTag.putInt("ID", adjacentNode.id());

                ListTag attachmentList = new ListTag();
                for (Pair<Float, WireAttachment> attachment : connectionData.attachments()) {
                    CompoundTag attachmentTag = attachment.getSecond().write();
                    attachmentTag.putFloat("Point", attachment.getFirst());
                    attachmentList.add(attachmentTag);
                }
                subNodeTag.put("Attachments", attachmentList);

                ResourceLocation key = CEERegistries.WIRE_TYPE.getKey(connectionData.wireType());
                if (key != null) // This would never happen
                    subNodeTag.putString("WireType", key.toString());
                subNodeTag.putFloat("Temperature", connectionData.temperature());
                subNodeTag.putDouble("Length", connectionData.length);

                connectedNodesList.add(subNodeTag);
            });

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

    static InfrastructureSavedData load(ServerLevel level, CompoundTag infrastructureTag) {
        InfrastructureSavedData sd = new InfrastructureSavedData(level);
        sd.ALL_NODES = new HashMap<>();
        sd.NODES_BY_POS = new HashMap<>();
        sd.CONNECTION_DATA = new Long2ObjectOpenHashMap<>();
        sd.CATENARY_ADJACENCY = new HashMap<>();
        sd.CATENARY_DATA = new HashMap<>();

        // Read nodes
        ListTag nodeList = infrastructureTag.getList("Nodes", Tag.TAG_COMPOUND);

        NBTHelper.iterateCompoundList(nodeList, nodeTag -> {
            InWorldNode node = InWorldNode.readFromTag(nodeTag);
            InWorldNodeData nodeData = sd.newNode(node);

            CompoundTag lastKnownGlobalPosTag = nodeTag.getCompound("LastKnownPos");
            CompoundTag lastKnownLocalPosTag = nodeTag.getCompound("LastKnownLocalPos");

            Vec3 lastKnownGlobalPos = nodeTag.contains("LastKnownPos") ?
                    new Vec3(lastKnownGlobalPosTag.getDouble("X"),
                            lastKnownGlobalPosTag.getDouble("Y"),
                            lastKnownGlobalPosTag.getDouble("Z")) : null;

            Vec3 lastKnownLocalPos = nodeTag.contains("LastKnownLocalPos") ?
                    new Vec3(lastKnownLocalPosTag.getDouble("X"),
                            lastKnownLocalPosTag.getDouble("Y"),
                            lastKnownLocalPosTag.getDouble("Z")) : null;

            // It can be null, as InWorldNodeData returns center when its null.
            nodeData.globalPos = lastKnownGlobalPos;
            nodeData.localPos = lastKnownLocalPos;

            if (nodeTag.contains("DetachedNodeType"))
                nodeData.detachedNodeType = DetachedNodeType.byName(nodeTag.getString("DetachedNodeType"));
            if (nodeTag.contains("DetachedNodeUUID"))
                nodeData.detachedNodeEntityId = nodeTag.getUUID("DetachedNodeUUID");

            if (nodeTag.contains("Label"))
                nodeData.label = nodeTag.getString("Label");

            if (nodeTag.getBoolean("DynamicPos")) {
                sd.DYNAMIC_POSITION_NODES.add(nodeData);
                nodeData.isDynamic = true;
            }

            sd.NODES_BY_POS.computeIfAbsent(node.sourcePos(), ((p) -> new ArrayList<>())).add(nodeData);
        });

        // Read node connections
        NBTHelper.iterateCompoundList(nodeList, nodeTag -> {
            InWorldNode node = InWorldNode.readFromTag(nodeTag);
            InWorldNodeData nodeData = sd.getNodeData(node);

            NBTHelper.iterateCompoundList(nodeTag.getList("ConnectedNodes", Tag.TAG_COMPOUND), connectionTag -> {
                InWorldNode adjacentNode = InWorldNode.readFromTag(connectionTag);
                InWorldNodeData adjacentNodeData = sd.getNodeData(adjacentNode);
                if (adjacentNodeData == null) {
                    LOGGER.warn("Could not load wire connection. connection to an invalid node, skipping");
                    return;
                }


                if (adjacentNode.equals(node)) {
                    LOGGER.warn("Could not load wire connection. both ends connect to the same node: {}, skipping", adjacentNode);
                    return;
                }

                // Since the nodes are stored as an adjacency graph, only read a connection once
                if (adjacentNode.compareTo(node) > 0)
                    return;

                // Parse wire type
                String wireTypeID = connectionTag.getString("WireType");
                WireType wireType = CEERegistries.WIRE_TYPE.get(ResourceLocation.tryParse(wireTypeID));

                if (wireType == null) {
                    LOGGER.warn("Could not load wire type between: {}, {} with id: {} in: {}, changing to standard...",
                            node, adjacentNode, wireTypeID, level.dimension().location());
                    wireType = CEEWireTypes.STANDARD.get();
                }

                // Parse attachments
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

                // Finalize everything
                double length = connectionTag.getDouble("Length");
                float temperature = Float.isNaN(connectionTag.getFloat("Temperature")) ? 0f : connectionTag.getFloat("Temperature");
                WireData wireData = new WireData(wireType, temperature, attachments, length);

                nodeData.adjacency.put(adjacentNodeData.id, wireData);
                adjacentNodeData.adjacency.put(nodeData.id, wireData);
                sd.CONNECTION_DATA.computeIfAbsent(DataPacker.packAndCanonicalize(nodeData.id, adjacentNodeData.id), c -> wireData);
            });
        });

        // Migrate devices from old versions
        if (infrastructureTag.contains("Devices", Tag.TAG_LIST)) {
            DevicesSavedData deviceSD = DevicesSavedData.load(level);
            migrateFromLegacy(infrastructureTag.getList("Devices", Tag.TAG_COMPOUND), deviceSD);
        }

        // Read Railway Catenary
        ListTag catenaryList = infrastructureTag.getList("Catenary", Tag.TAG_COMPOUND);
        NBTHelper.iterateCompoundList(catenaryList, tag -> {
            BlockPos from = NBTHelper.readBlockPos(tag, "From");

            // Legacy
            tag.getList("Connections", Tag.TAG_INT_ARRAY).forEach(connectionTag -> {
                int[] arr = ((IntArrayTag)connectionTag).getAsIntArray();
                if (arr.length != 3) {
                    LOGGER.warn("Could not load catenary connection, at pos: {} in: {}",
                            from.toShortString(), level.dimension().location());
                    return;
                }

                BlockPos to = new BlockPos(arr[0], arr[1], arr[2]);

                sd.CATENARY_ADJACENCY.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                CatenaryConnectionData catenaryData = new CatenaryConnectionData(0, false, 0);
                sd.CATENARY_DATA.put(new CatenaryConnection(from, to), catenaryData);

            });

            NBTHelper.iterateCompoundList(tag.getList("Connections", Tag.TAG_COMPOUND), connectionTag -> {
                int[] arr = connectionTag.getIntArray("Pos");
                if (arr.length != 3) {
                    LOGGER.warn("Could not load catenary connection, invalid position at pos: {} in: {}",
                            from.toShortString(), level.dimension().location());
                    return;
                }

                BlockPos to = new BlockPos(arr[0], arr[1], arr[2]);

                sd.CATENARY_ADJACENCY.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                boolean isLow = connectionTag.getBoolean("IsLow");
                float temperature = Float.isNaN(connectionTag.getFloat("Temperature")) ? 0f : connectionTag.getFloat("Temperature");
                double length = connectionTag.getDouble("Length");
                CatenaryConnectionData catenaryData = new CatenaryConnectionData(temperature, isLow, length);
                sd.CATENARY_DATA.put(new CatenaryConnection(from, to), catenaryData);
            });
        });
        return sd;
    }

    public static boolean isDynamicPosition(ServerLevel level, InWorldNode node) {
        if (DetachedNodeHelper.isDetached(node))
            return true;
        return SableCompanion.INSTANCE.getContaining(level, node.sourcePos()) != null;
    }

    boolean simulationInitialized = false;
    private void loadSimulationState() {
        if (simulationInitialized)
            return;
        if (ALL_NODES.isEmpty() && CONNECTION_DATA.isEmpty() && CATENARY_DATA.isEmpty())
            return;

        simulationInitialized = true;

        ALL_NODES.forEach(this::onNodeUpdateOrCreate);

        for (Long2ObjectMap.Entry<WireData> e : CONNECTION_DATA.long2ObjectEntrySet()) {
            int node1 = DataPacker.unpackFirstI(e.getLongKey());
            int node2 = DataPacker.unpackSecondI(e.getLongKey());
            InWorldNodeData data1 = NODES_BY_ID.get(node1);
            InWorldNodeData data2 = NODES_BY_ID.get(node2);
            wireSimulationState.addConnection(new InWorldNodeConnection(data1.node, data2.node), e.getValue(), true);
        }

        for (Map.Entry<CatenaryConnection, CatenaryConnectionData> e : CATENARY_DATA.entrySet()) {
            InWorldNodeConnection connection = new InWorldNodeConnection(
                    new InWorldNode(0, e.getKey().pos1()), new InWorldNode(0, e.getKey().pos2()));
            wireSimulationState.addCatenaryConnection(connection, e.getValue(), true);
        }

        wireSimulationState.onNodeChange(ALL_NODES.keySet());
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
                                InfrastructureSavedData.load(level, compoundTag)),
                        "electroenergetics_infrastructure");
        sd.loadSimulationState();
        return sd;
    }

    public void registerOrUpdateNodes(BlockPos pos, List<Integer> nodeIDs) {
        // Put the nodes list into the by pos map, get the previous one into oldNodesList.
        List<InWorldNodeData> nodes = new ArrayList<>(nodeIDs.size());
        List<InWorldNodeData> oldNodesList = NODES_BY_POS.put(pos, nodes);
        Set<InWorldNodeData> unvisitedNodes = oldNodesList == null ? new HashSet<>() : new HashSet<>(oldNodesList);

        // Update node positions
        for (int id : nodeIDs) {
            InWorldNode node = new InWorldNode(id, pos);
            InWorldNodeData nodeData = getNodeData(node);

            if (nodeData == null)
                nodeData = newNode(node);

            unvisitedNodes.remove(nodeData);
            nodes.add(nodeData);

            if (isDynamicPosition(level, node)) {
                nodeData.isDynamic = true;
                DYNAMIC_POSITION_NODES.add(nodeData);
            }

            Vec3 newPos = node.getLocalPosition(level);
            if (newPos == null)
                newPos = InWorldNodeData.CENTER;
            if (!Objects.equals(newPos, nodeData.getLocalPos())) {
                nodeData.globalPos = node.toGlobalPos(newPos, level);

                onNodeUpdateOrCreate(node, nodeData);
                onNodePosUpdate(node, nodeData);
                for (InWorldNodeConnection connection : getConnections(node)) {
                    wireSimulationState.removeConnection(connection);
                    wireSimulationState.addConnection(connection, getConnectionData(connection), false);
                }
            }
        }

        // All forgotten nodes are going to be removed
        for (InWorldNodeData nodeData : unvisitedNodes) {
            for (InWorldNodeConnection connection : getConnections(nodeData))
                removeAndDropConnection(connection);
            removeNode(nodeData.node);
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
        wireSimulationState.onNodeChange(ALL_NODES.keySet());
    }

    private final List<InWorldNodeData> nodesToRemove = new ArrayList<>();
    public void removeNodeDefer(InWorldNodeData nodeData) {
        if (!nodeData.isValid())
            return;

        nodesToRemove.add(nodeData);
    }

    public void tick() {
        updateAllNodePositions();

        for (long connection : sableToUpdate) {

            WireData connectionData = getConnectionData(connection);
            if (connectionData != null)
                setConnectionData(connection, connectionData);
        }

        sableToUpdate.clear();

        for (int nodeID : sableNodesToUpdate) {
            InWorldNodeData nodeData = NODES_BY_ID.get(nodeID);
            if (nodeData != null)
                wireSync.handleNodeLabelRename(nodeData);
        }

        sableNodesToUpdate.clear();

        for (InWorldNodeData nodeData : nodesToRemove) {
            removeNode(nodeData.node);
        }

        nodesToRemove.clear();
    }

    private final List<InWorldNodeConnection> dynamicConnectionsToUpdate = new ArrayList<>();
    private void updateAllNodePositions() {

        // Update them
        for (InWorldNodeConnection connection : dynamicConnectionsToUpdate) {
            WireData connectionData = getConnectionData(connection);
            if (connectionData == null)
                continue;

            if (wireSimulationState.relocateConnection(connection, connectionData)) {
                removeAndDropConnection(connection);
            } else
                wireSync.handleWireRepositioned(connection, connectionData);
        }
        dynamicConnectionsToUpdate.clear();

        // for Sable and detached nodes
        for (InWorldNodeData nodeData : DYNAMIC_POSITION_NODES) {
            InWorldNode node = nodeData.node;
            if (nodeData.detachedNodeType != null) {
                Vec3 newPos = DetachedNodeHelper.tickPhysicsNode(nodeData, this);

                if (nodeData.globalPos == null || newPos.distanceToSqr(nodeData.globalPos) > 0.0001) {
                    nodeData.globalPos = newPos;
                    getConnections(nodeData, dynamicConnectionsToUpdate::add);
                    wireSync.handleNodeMoved(nodeData);
                }
                continue;
            }
            Vec3 localPos = nodeData.getLocalPos();
            if (!node.isFullyLoadable(level))
                continue;
            Vec3 pos = node.toGlobalPos(localPos, level);
            if (pos == null)
                continue;

            // If the position changed, mark the connections for an update
            if (nodeData.globalPos == null || nodeData.globalPos.distanceToSqr(pos) > 0.003) {
                getConnections(nodeData, dynamicConnectionsToUpdate::add);
                nodeData.globalPos = pos;
                wireSync.handleNodeMoved(nodeData);
            }
        }
    }

    /**
     * Returns all connections of the specified node
     */
    public List<InWorldNodeConnection> getConnections(InWorldNode node) {
        InWorldNodeData data = getNodeData(node);
        if (data == null)
            return Collections.emptyList();
        return getConnections(data);
    }

    /**
     * Returns all connections of the specified node
     */
    public List<InWorldNodeConnection> getConnections(InWorldNodeData nodeData) {
        List<InWorldNodeConnection> connections = new ArrayList<>();
        getConnections(nodeData, connections::add);
        return connections;
    }

    /**
     * Supplies all connections of the specified node to the consumer
     */
    public void getConnections(InWorldNodeData nodeData, Consumer<InWorldNodeConnection> consumer) {
        for (int connectedNodeID : nodeData.adjacency.keySet()) {
            InWorldNodeData adjacentNodeData = NODES_BY_ID.get(connectedNodeID);
            if (adjacentNodeData == null)
                continue;
            consumer.accept(new InWorldNodeConnection(nodeData.node, adjacentNodeData.node));
        }
    }

    /**
     * Removes the specified connection, and drops wire attachments.
     * @return The previous {@link WireData} if the connection existed. Otherwise, null.
     */
    public WireData removeConnection(InWorldNodeConnection connection) {

        InWorldNodeData node1Data = getNodeDataOrThrow(connection.node1());
        InWorldNodeData node2Data = getNodeDataOrThrow(connection.node2());

        WireData wireData = node1Data.adjacency.remove(node2Data.id);
        node2Data.adjacency.remove(node1Data.id);

        CONNECTION_DATA.remove(DataPacker.packAndCanonicalize(node1Data.id, node2Data.id));
        if (wireData != null) {
            for (Pair<Float, WireAttachment> attachment : wireData.attachments()) {
                Vec3 pos = QuadraticWireHelper.posAt(Vec3.atCenterOf(connection.node1().sableSourcePos(level)),
                        Vec3.atCenterOf(connection.node2().sableSourcePos(level)), attachment.getFirst(),
                        wireData.wireType().getSag());

                for (ItemStack stack : attachment.getSecond().getDrops(level))
                    Containers.dropItemStack(level, pos.x(), pos.y(), pos.z(), stack);

            }
        }

        wireSync.handleWireRemoved(connection);
        wireSimulationState.removeConnection(connection);
        setDirty();
        return wireData;
    }


    /**
     * Removes the specified connection.
     * @return The previous {@link WireData} if the connection existed. Otherwise, null.
     */
    public WireData removeConnectionNoDrops(InWorldNodeConnection connection) {

        InWorldNodeData node1Data = getNodeDataOrThrow(connection.node1());
        InWorldNodeData node2Data = getNodeDataOrThrow(connection.node2());

        WireData wireData = node1Data.adjacency.remove(node2Data.id);
        node2Data.adjacency.remove(node1Data.id);
        CONNECTION_DATA.remove(DataPacker.packAndCanonicalize(node1Data.id, node2Data.id));
        wireSync.handleWireRemoved(connection);
        wireSimulationState.removeConnection(connection);
        setDirty();
        return wireData;
    }

    /**
     * Creates the node connection at the specified position.
     * @return The {@link InWorldNodeConnection} object describing the wire connection.
     */
    public long connect(InWorldNode node1, InWorldNode node2, WireType wireType) {
        return connect(node1, node2, WireData.ofNoLength(wireType));
    }

    /**
     * Creates the node connection at the specified position.
     * @return The {@link InWorldNodeConnection} object describing the wire connection.
     */
    public long connect(InWorldNode node1, InWorldNode node2, WireData wireData) {
        if (node1.equals(node2))
            throw new IllegalArgumentException("Tried to connect a wire between a single node: " + node1);

        InWorldNodeData node1Data = getNodeDataOrThrow(node1);
        InWorldNodeData node2Data = getNodeDataOrThrow(node2);

        InWorldNodeConnection connection = new InWorldNodeConnection(node1, node2);

        node1Data.adjacency.put(node2Data.id, wireData);
        node2Data.adjacency.put(node1Data.id, wireData);

        long packedConnection = DataPacker.packAndCanonicalize(node1Data.id, node2Data.id);
        CONNECTION_DATA.put(packedConnection, wireData);
        setDirty();

        wireSimulationState.addConnection(connection, wireData, false);
        wireSync.handleWireAdded(connection, wireData);
        return packedConnection;
    }


    /**
     * Creates a connection, but doesn't update it. Should be updated afterward. Internal. Don't use yourself.
     */
    public long connectNoUpdate(InWorldNode node1, InWorldNode node2, @NotNull WireData wireData) {
        if (node1.equals(node2))
            throw new IllegalArgumentException("Tried to connect a wire between a single node: " + node1);

        InWorldNodeData node1Data = getNodeDataOrThrow(node1);
        InWorldNodeData node2Data = getNodeDataOrThrow(node2);

        node1Data.adjacency.put(node2Data.id, wireData);
        node2Data.adjacency.put(node1Data.id, wireData);
        long packedConnection = DataPacker.packAndCanonicalize(node1Data.id, node2Data.id);
        CONNECTION_DATA.put(packedConnection, wireData);
        setDirty();
        return packedConnection;
    }

    /**
     * @return {@code false} if the connections don't exist or aren't connected, {@code true} if they are connected.
     */
    public boolean isConnected(InWorldNode node1, InWorldNode node2) {
        InWorldNodeData node1Data = getNodeData(node1);
        InWorldNodeData node2Data = getNodeData(node2);

        if (node1Data != null && node2Data != null && node1Data.adjacency.containsKey(node2Data.id))
            return true;

        // Catenary connections are handled separately

        return node1.id() == 0 && node2.id() == 0 && CATENARY_ADJACENCY
                .getOrDefault(node1.sourcePos(), Collections.emptyList()).contains(node2.sourcePos());
    }

    public WireData getConnectionData(InWorldNodeConnection connection) {
        InWorldNodeData node1Data = getNodeData(connection.node1());
        InWorldNodeData node2Data = getNodeData(connection.node2());
        if (node1Data == null || node2Data == null)
            return null;
        return CONNECTION_DATA.get(DataPacker.packAndCanonicalize(node1Data.id, node2Data.id));
    }

    public WireData getConnectionData(long connection) {
        return CONNECTION_DATA.get(connection);
    }

    public void setConnectionData(InWorldNodeConnection connection, WireData data) {
        InWorldNodeData node1Data = getNodeDataOrThrow(connection.node1());
        int node1Id = node1Data.id;
        InWorldNodeData node2Data = getNodeDataOrThrow(connection.node2());
        int node2Id = node2Data.id;
        CONNECTION_DATA.put(DataPacker.packAndCanonicalize(node1Id, node2Id), data);
        node1Data.adjacency.put(node2Id, data);
        node2Data.adjacency.put(node1Id, data);

        wireSync.handleWireAdded(connection, data);
        wireSimulationState.removeConnection(connection);
        wireSimulationState.addConnection(connection, data, false);
    }

    public void setConnectionData(long connection, WireData data) {
        InWorldNodeConnection nodeConnection = resolveConnection(connection);
        wireSync.handleWireAdded(nodeConnection, data);
        wireSimulationState.removeConnection(nodeConnection);
        wireSimulationState.addConnection(nodeConnection, data, false);
        CONNECTION_DATA.put(connection, data);
    }

    public InWorldNodeConnection resolveConnection(long connection) {
        return new InWorldNodeConnection(
                NODES_BY_ID.get(DataPacker.unpackFirstI(connection)).node,
                NODES_BY_ID.get(DataPacker.unpackSecondI(connection)).node);
    }

    /**
     * Returns all nodes at the specified position.
     */
    public List<InWorldNodeData> getNodesAt(BlockPos pos) {
        return NODES_BY_POS.getOrDefault(pos, Collections.emptyList());
    }

    /**
     * @return All nodes
     */
    public Set<InWorldNode> getNodes() {
        return ALL_NODES.keySet();
    }

    /**
     * @return All dynamically-positioned nodes
     */
    public Set<InWorldNodeData> getDynamicNodes() {
        return DYNAMIC_POSITION_NODES;
    }

    /**
     * Tries to update the global node position and returns it.
     * @return node position or center if it can't find the position.
     */
    public Vec3 getNodePositionOrCenter(InWorldNode node) {
        InWorldNodeData nodeData = getNodeData(node);
        if (nodeData == null)
            return node.sourcePos().getCenter();
        updateNodePosition(nodeData);
        Vec3 pos = nodeData.globalPos;
        return pos == null ? node.sourcePos().getCenter() : pos;
    }

    /**
     * Tries to update the global node position and returns it.
     * @return null if it can't find the position.
     */
    public Vec3 getNodePosition(InWorldNode node) {
        InWorldNodeData nodeData = getNodeDataOrThrow(node);
        updateNodePosition(nodeData);
        return nodeData.globalPos;
    }

    /**
     * Tries to update the local node position and returns it.
     * @return null if it can't find the position.
     */
    public Vec3 getLocalNodePosition(InWorldNode node) {
        InWorldNodeData nodeData = getNodeDataOrThrow(node);
        updateNodePosition(nodeData);
        return nodeData.localPos;
    }

    private void updateNodePosition(InWorldNodeData nodeData) {
        InWorldNode node = nodeData.node;
        if (nodeData.globalPos == null || level.isLoaded(node.sourcePos())) {
            Vec3 newPos = node.getLocalPosition(level);

            if (node.isFullyLoadable(level)) {
                if (newPos == null)
                    newPos = VecHelper.CENTER_OF_ORIGIN;

                nodeData.localPos = newPos;
                Vec3 pos = node.toGlobalPos(newPos, level);

                if (nodeData.globalPos == null || nodeData.globalPos.distanceToSqr(pos) > 0.003) {
                    nodeData.globalPos = pos;
                    onNodePosUpdate(node, nodeData);
                }
            }
        }
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
    public InWorldNodeData createNode(InWorldNode node) {
        return createNode(node, node.sourcePos().getCenter(), VecHelper.CENTER_OF_ORIGIN);
    }

    /**
     * Creates the specified node with the specified position.
     * You usually shouldn't call it yourself.
     */
    public InWorldNodeData createNode(InWorldNode node, Vec3 globalPos, Vec3 localPos) {
        InWorldNodeData nodeData = newNode(node);
        nodeData.localPos = localPos;
        nodeData.globalPos = globalPos;
        NODES_BY_POS.computeIfAbsent(node.sourcePos(), k -> new ArrayList<>()).add(nodeData);

        if (isDynamicPosition(level, node)) {
            nodeData.isDynamic = true;
            DYNAMIC_POSITION_NODES.add(nodeData);
        }

        onNodeUpdateOrCreate(node, nodeData);
        onNodePosUpdate(node, nodeData);

        return nodeData;
    }

    public void removeNodes(BlockPos pos) {
        List<InWorldNodeData> nodeIDs = NODES_BY_POS.remove(pos);
        if (nodeIDs == null)
            return;

        for (InWorldNodeData nodeData : nodeIDs) {
            for (InWorldNodeConnection connection : getConnections(nodeData))
                removeAndDropConnection(connection);
            removeNode(nodeData.node);
        }

        List<BlockPos> catenaryConnections = List.copyOf(CATENARY_ADJACENCY.getOrDefault(pos, new ArrayList<>()));
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.COPPER_WIRE.asStack((catenaryConnections.size()) * CEEConfigs.server().wiresPerSpool.get()));
        for (BlockPos connection : catenaryConnections)
            removeCatenary(pos, connection);
        wireSimulationState.onNodeChange(ALL_NODES.keySet());
    }

    // NODE LIFETIME MANAGEMENT:

    /**
     * Creates a detached node at the specified position of type.
     * @return the created {@link InWorldNodeData} object
     *
     * @see DetachedNodeHelper
     */
    @CanIgnoreReturnValue
    public InWorldNodeData createDetachedNode(@NotNull DetachedNodeType detachedNodeType, @NotNull Vec3 pos) {
        int id = freeDetachedNodeIDs.isEmpty() ?
                lastDetachedNodeId + 1 :
                freeDetachedNodeIDs.popInt();
        InWorldNode node = DetachedNodeHelper.getFromId(id);
        InWorldNodeData nodeData = createNode(node, pos, Vec3.ZERO);
        if (detachedNodeType.isPhysics())
            DetachedNodeHelper.createDetachedNodeEntity(nodeData, pos, level);
        nodeData.detachedNodeType = detachedNodeType;
        wireSync.handleNodeCreate(nodeData);

        return nodeData;
    }

    /**
     * Creates a new node, assigns it an ID and
     * @return the created {@link InWorldNodeData} object
     */
    private InWorldNodeData newNode(InWorldNode node) {
        InWorldNodeData prevNodeData = getNodeData(node);
        if (prevNodeData != null)
            return prevNodeData;

        if (DetachedNodeHelper.isDetached(node)) {
            if (lastDetachedNodeId < node.id())
                lastDetachedNodeId = node.id();
        }

        InWorldNodeData data;
        if (freeNodeIDs.isEmpty()) {
            data = new InWorldNodeData(NODES_BY_ID.size(), node);
            NODES_BY_ID.add(data);
        } else {
            int index = freeNodeIDs.popInt();
            data = new InWorldNodeData(index, node);
            NODES_BY_ID.set(index, data);
        }
        ALL_NODES.put(node, data);
        wireSimulationState.onNodeChange(ALL_NODES.keySet());

        if (!InWorldNode.isFromSubLevel(level, node.sourcePos())) {

            Set<InWorldNode> chunkNodes = NODES_BY_CHUNK.computeIfAbsent(ChunkPos.asLong(node.sourcePos()),
                    c -> new HashSet<>());

            chunkNodes.add(node);
        }

        return data;
    }

    /**
     * Removes the specified node from the Infrastructure data and cleans it from all the data structures.
     * You usually shouldn't call it yourself.
     */
    @CanIgnoreReturnValue
    public InWorldNodeData removeNode(InWorldNode node) {
        InWorldNodeData nodeData = ALL_NODES.remove(node);
        if (nodeData == null)
            return null;
        freeNodeIDs.push(nodeData.id);
        NODES_BY_ID.set(nodeData.id, null);
        nodeData.invalidate();
        onNodeRemove(nodeData.node, nodeData);
        DYNAMIC_POSITION_NODES.remove(nodeData);
        List<InWorldNodeData> nodesAtPos = NODES_BY_POS.get(nodeData.node.sourcePos());
        if (nodesAtPos != null)
            nodesAtPos.remove(nodeData);
        Set<InWorldNode> chunkNodes = NODES_BY_CHUNK.computeIfAbsent(ChunkPos.asLong(node.sourcePos()),
                c -> new HashSet<>());

        chunkNodes.remove(node);

        if (chunkNodes.isEmpty())
            NODES_BY_CHUNK.remove(ChunkPos.asLong(node.sourcePos()));
        return nodeData;
    }

    public InWorldNodeData getNodeData(InWorldNode node) {
        return ALL_NODES.get(node);
    }

    public InWorldNodeData getNodeData(int nodeID) {
        if (nodeID < 0 || nodeID > NODES_BY_ID.size() - 1)
            return null;
        return NODES_BY_ID.get(nodeID);
    }

    public InWorldNodeData getNodeDataOrThrow(InWorldNode node) {
        if (node == null)
            throw new IllegalArgumentException("node can't be null!");
        InWorldNodeData data = ALL_NODES.get(node);
        if (data == null)
            throw new IllegalArgumentException("Node: " + node + " doesn't exist!");
        return data;
    }

    // HOOKS & STUFF:

    /**
     * Hook for node update
     */
    @SuppressWarnings("unused")
    private void onNodeUpdateOrCreate(InWorldNode node, InWorldNodeData nodeData) {

    }

    /**
     * Hook for node pos update
     */
    @SuppressWarnings("unused")
    private void onNodePosUpdate(InWorldNode node, InWorldNodeData nodeData) {

    }

    /**
     * Hook for node removal
     */
    private void onNodeRemove(InWorldNode node, InWorldNodeData nodeData) {
        wireSync.handleNodeRemove(nodeData);

        if (DetachedNodeHelper.isDetached(node)) {
            freeDetachedNodeIDs.add(node.id());
        }
    }

    // UTILS:

    /**
     * Removes the specified connection and drops the items.
     * @param connection Connection to remove
     * @return removed connection wireData or null if connection doesn't exist
     */
    @CanIgnoreReturnValue
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

    /**
     * Moves all the connections of {@code source} to {@code destination}
     * If there is already a connection between {@code source} and {@code destination}, it will be skipped
     */
    public void migrateConnections(InWorldNode source, InWorldNode destination) {
        InWorldNodeData sourceData = getNodeData(source);
        InWorldNodeData destinationData = getNodeData(destination);
        if (sourceData == null)
            sourceData = createNode(source);

        if (destinationData == null)
            destinationData = createNode(destination);

        // clone to prevent CME
        for (Int2ObjectMap.Entry<WireData> entry : sourceData.adjacency.clone().int2ObjectEntrySet()) {
            InWorldNodeData otherNode = NODES_BY_ID.get(entry.getIntKey());
            if (otherNode == destinationData)
                continue;
            WireData wireData = entry.getValue();
            InWorldNodeConnection connection = new InWorldNodeConnection(source, otherNode.node);

            removeConnectionNoDrops(connection);
            connect(destination, otherNode.node, wireData);
        }
    }

    public Set<CatenaryConnection> getAllCatenaryConnections() {
       return CATENARY_DATA.keySet();
    }

    public boolean hasNode(InWorldNode node) {
        return ALL_NODES.containsKey(node);
    }

    public void getNodesInChunk(long chunk, Consumer<InWorldNode> consumer) {
        Set<InWorldNode> nodes = NODES_BY_CHUNK.get(chunk);
        if (nodes != null)
            for (InWorldNode node : nodes)
                consumer.accept(node);
    }
}
