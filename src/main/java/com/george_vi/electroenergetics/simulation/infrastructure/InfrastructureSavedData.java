package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.simulation.*;
import com.george_vi.electroenergetics.simulation.simulator.SimulationStats;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.george_vi.electroenergetics.simulation.util.SimulatorProfiler;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class InfrastructureSavedData extends SavedData {
    Map<BlockPos, SimulatedDeviceInstance<?>> DEVICES = new TreeMap<>();
    Map<BlockPos, SimulatedDeviceInstance<?>> TICKING_DEVICES = new HashMap<>();
    Map<InWorldNode, List<InWorldNode>> NODES = new HashMap<>();
    Map<InWorldNode, Vec3> NODE_POSITIONS = new HashMap<>();
    Map<BlockPos, List<InWorldNode>> NODES_BY_POS = new HashMap<>();
    Map<InWorldNodeConnection, WireData> CONNECTION_DATA = new HashMap<>();

    // Trains
    Map<BlockPos, List<BlockPos>> CATENARY_ADJACENCY = new HashMap<>();
    Map<CatenaryConnection, CatenaryConnectionData> CATENARY_DATA = new HashMap<>();

    public ServerLevel level;
    public SimulationTicker ticker;

    // Wire infrastructure
    public WireSimulationState wireSimulationState;
    public WireAssemblerModule wireAssemblerModule;
    public WireLifetimeModule wireLifetimeModule;
    public WireCrossContactModule wireCrossContactModule;
    public CatenaryModule catenaryModule;
    public WireElectrocutionModule wireElectrocutionModule;

    public static final Logger LOGGER = LogUtils.getLogger();

    public InfrastructureSavedData(ServerLevel level) {
        this.level = level;
        wireSimulationState = new WireSimulationState(this, level);
        wireAssemblerModule = new WireAssemblerModule(this, level, this.wireSimulationState);
        wireLifetimeModule = new WireLifetimeModule(this, level, this.wireSimulationState);
        wireCrossContactModule = new WireCrossContactModule(this, level, this.wireSimulationState);
        catenaryModule = new CatenaryModule(this, level, this.wireSimulationState);
        wireElectrocutionModule = new WireElectrocutionModule(this, level, this.wireSimulationState);
        ticker = new SimulationTicker(level, this);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {

        // Save Devices

        ListTag deviceList = new ListTag();
        for (Map.Entry<BlockPos, SimulatedDeviceInstance<?>> e : DEVICES.entrySet()) {
            CompoundTag deviceTag = new CompoundTag();

            deviceTag.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
            deviceTag.putString("ID", e.getValue().simulatedDevice().getID().toString());
            deviceTag.put("ExtraData", e.getValue().write());

            deviceList.add(deviceTag);
        }
        compoundTag.put("Devices", deviceList);

        // Save Nodes / Node Connections

        ListTag nodeList = new ListTag();
        for (Map.Entry<InWorldNode, List<InWorldNode>> e : NODES.entrySet()) {
            CompoundTag nodeTag = new CompoundTag();

            nodeTag.put("Pos", NbtUtils.writeBlockPos(e.getKey().sourcePos()));
            nodeTag.putInt("ID", e.getKey().id());

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
        sd.DEVICES = new HashMap<>();
        sd.TICKING_DEVICES = new HashMap<>();
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
            Vec3 lastKnownPos = tag.contains("LastKnownPos") ? new Vec3(lastKnownPosTag.getDouble("X"), lastKnownPosTag.getDouble("Y"), lastKnownPosTag.getDouble("Z")) : null;
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
                WireData wireData = new WireData(wireType, Float.isNaN(connectionTag.getFloat("Temperature")) ? 0f : connectionTag.getFloat("Temperature"), attachments);

                sd.wireSimulationState.addConnection(connection, wireData, true);
                sd.CONNECTION_DATA.computeIfAbsent(connection, c -> wireData);
            });
            sd.NODE_POSITIONS.put(node, lastKnownPos);
            sd.NODES.put(node, connectedNodes);
            sd.NODES_BY_POS.computeIfAbsent(pos, ((p) -> new ArrayList<>()));
            sd.NODES_BY_POS.computeIfPresent(pos, ((p, nodes) -> {
                nodes.add(node);
                return nodes;
            }));
        });

        // Read Devices

        NBTHelper.iterateCompoundList(compoundTag.getList("Devices", Tag.TAG_COMPOUND), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            SimulatedDevice<?> device = CEESimulatedDevices.get(ResourceLocation.parse(tag.getString("ID")));
            if (device == null) {
                List<InWorldNode> nodes = sd.NODES_BY_POS.get(pos);
                for (InWorldNode node : nodes) {
                    for (InWorldNodeConnection connection : sd.getConnections(node))
                        sd.removeConnection(connection);
                    sd.NODES.remove(node);
                }
                sd.NODES_BY_POS.remove(pos);
                LOGGER.warn("Could not load device: {} at pos: {} in: {}. No device with such ID, removing...", tag.getString("ID"), pos.toShortString(), level.dimension().location());
                return;
            }
            SimulatedDeviceInstance<?> di = SimulatedDeviceInstance.read(device, pos, tag.getCompound("ExtraData"), sd.NODES_BY_POS.getOrDefault(pos, new ArrayList<>()));
            sd.DEVICES.put(pos, di);
            if (device.ticks())
                sd.TICKING_DEVICES.put(pos, di);
        });

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
                CatenaryConnectionData catenaryData = new CatenaryConnectionData(0, false);
                sd.CATENARY_DATA.put(new CatenaryConnection(from, to), catenaryData);
                sd.wireSimulationState.addCatenaryConnection(new InWorldNodeConnection(new InWorldNode(0, from), new InWorldNode(0, to)), catenaryData, true);

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
                CatenaryConnectionData catenaryData = new CatenaryConnectionData(Float.isNaN(tg.getFloat("Temperature")) ? 0f : tg.getFloat("Temperature"), isLow);
                sd.CATENARY_DATA.put(new CatenaryConnection(from, to), catenaryData);
                sd.wireSimulationState.addCatenaryConnection(new InWorldNodeConnection(new InWorldNode(0, from), new InWorldNode(0, to)), catenaryData, true);
            });
        });

//        try {
//            sd.wireConnectionManager.rebuild();
//        } catch (Throwable err) {
//            LOGGER.error("Could not build the wire connection graph!", err);
//        }

        return sd;
    }

    public static InfrastructureSavedData load(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(new Factory<>(() -> new InfrastructureSavedData(level), (compoundTag, provider) -> InfrastructureSavedData.load(level, compoundTag, provider)), "electroenergetics_infrastructure");
    }

    public <T> void addDevice(BlockPos pos, SimulatedDevice<T> device, CompoundTag extraData, List<Integer> nodeIDs) {
        // I'm sorry, this is a mess.
        List<InWorldNode> nodes = nodeIDs.stream().map(id -> new InWorldNode(id, pos)).toList();
        SimulatedDeviceInstance<?> di = DEVICES.get(pos);
        if (di != null) {
            di.invalidate();
            if (di.simulatedDevice() == device) { // Set a device of the same type. It's possible nodes might have changed
                List<InWorldNode> oldNodes = NODES_BY_POS.get(pos);
                if (oldNodes != null && oldNodes.stream().map(InWorldNode::id).sorted().toList().equals(nodeIDs.stream().sorted().toList())) {
                    // Nodes changed
                    for (InWorldNode node : oldNodes) {
                        Vec3 newPos = node.getPosition(level);
                        if (NODE_POSITIONS.replace(node, newPos) != newPos) {
                            for (InWorldNodeConnection connection : getConnections(node)) {
                                wireSimulationState.removeConnection(connection);
                                wireSimulationState.addConnection(connection, getConnectionData(connection), false);
                            }
                        }
                    }
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
                                InWorldNodeConnection nodeConnection = new InWorldNodeConnection(new InWorldNode(0, pos), new InWorldNode(0, neighbour));
                                wireSimulationState.removeConnection(nodeConnection);
                                wireSimulationState.addCatenaryConnection(nodeConnection, connectionData, false);
                            }
                        }
                    }

                    return;
                }

                if (oldNodes != null)
                    for (InWorldNode node : oldNodes) {
                        if (nodes.contains(node))
                            continue;
                        for (InWorldNodeConnection connection : getConnections(node))
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                                    new ItemStack(removeConnection(connection).wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
                        NODES.remove(node);
                        NODE_POSITIONS.remove(node);
                    }

                NODES_BY_POS.replace(pos, nodes);

                for (InWorldNode node : nodes) {
                    NODES.putIfAbsent(node, new ArrayList<>());
                    NODE_POSITIONS.put(node, node.getPosition(level));

                    for (InWorldNodeConnection connection : getConnections(node)) {
                        wireSimulationState.removeConnection(connection);
                        if (NODES.containsKey(connection.node1()) && NODES.containsKey(connection.node2()))
                            wireSimulationState.addConnection(connection, getConnectionData(connection), false);
                    }
                }

                return;
            } else { // New device
                SimulatedDeviceInstance<T> ndi = new SimulatedDeviceInstance<>(device, pos, device.read(extraData), nodes);
                DEVICES.put(pos, ndi);
                if (device.ticks())
                    TICKING_DEVICES.put(pos, ndi);

                List<InWorldNode> oldNodes = NODES_BY_POS.get(pos);
                if (oldNodes != null)
                    for (InWorldNode node : oldNodes) {
                        if (nodes.contains(node))
                            continue;
                        List<InWorldNodeConnection> connections = getConnections(node);
                        for (InWorldNodeConnection connection : connections)
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(removeConnection(connection).wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));

                        List<BlockPos> catenaryConnections = List.copyOf(CATENARY_ADJACENCY.getOrDefault(pos, new ArrayList<>()));
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.COPPER_WIRE.asStack((catenaryConnections.size()) * CEEConfigs.server().wiresPerSpool.get()));
                        for (BlockPos connection : catenaryConnections)
                            removeCatenary(pos, connection);
                        NODES.remove(node);
                        NODE_POSITIONS.remove(node);
                    }

                NODES_BY_POS.put(pos, nodes);
                setDirty();
                return;
            }
        }


        SimulatedDeviceInstance<T> ndi = new SimulatedDeviceInstance<>(device, pos, device.read(extraData), nodes);
        DEVICES.put(pos, ndi);
        if (device.ticks())
            TICKING_DEVICES.put(pos, ndi);

        for (InWorldNode node : nodes) {
            Vec3 nodePos = node.getPosition(level);
            // Shouldn't ever happen, but it's better to be safe.
            if (nodePos == null)
                continue;
            NODES.put(node, new ArrayList<>());
            NODE_POSITIONS.put(node, nodePos);
        }
        NODES_BY_POS.put(pos, nodes);

        setDirty();
    }

    public void addDevice(BlockPos pos, SimulatedDevice<?> device, List<Integer> nodeIDs) {
        addDevice(pos, device, new CompoundTag(), nodeIDs);
    }

    public void removeDevice(BlockPos pos) {
        SimulatedDeviceInstance<?> di = DEVICES.remove(pos);
        TICKING_DEVICES.remove(pos);
        if (di != null)
            di.invalidate();
        List<InWorldNode> nodes = NODES_BY_POS.get(pos);
        if (nodes != null)
            for (InWorldNode node : nodes) {
                List<InWorldNodeConnection> connections = getConnections(node);
                for (InWorldNodeConnection connection : connections)
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(removeConnection(connection).wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));

                List<BlockPos> catenaryConnections = List.copyOf(CATENARY_ADJACENCY.getOrDefault(pos, new ArrayList<>()));
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.COPPER_WIRE.asStack((catenaryConnections.size()) * CEEConfigs.server().wiresPerSpool.get()));
                for (BlockPos connection : catenaryConnections)
                    removeCatenary(pos, connection);
                NODES.remove(node);
                NODE_POSITIONS.remove(node);
            }
        NODES_BY_POS.remove(pos);
        setDirty();
    }

    public Collection<SimulatedDeviceInstance<?>> getDevices() {
        return new ArrayList<>(DEVICES.values());
    }

    public Collection<SimulatedDeviceInstance<?>> getTickingDevices() {
        return new ArrayList<>(TICKING_DEVICES.values());
    }

    public SimulatedDeviceInstance<?> getDevice(BlockPos pos) {
        return DEVICES.get(pos);
    }

    public List<InWorldNodeConnection> getConnections(InWorldNode node) {
        List<InWorldNode> connectedNodes = NODES.get(node);
        if (connectedNodes == null)
            return Collections.emptyList();
        List<InWorldNodeConnection> connections = new ArrayList<>();
        for (InWorldNode connectedNode : connectedNodes)
            connections.add(new InWorldNodeConnection(node, connectedNode));
        return connections;
    }

    public WireData removeConnection(InWorldNodeConnection connection) {

        NODES.get(connection.node1()).remove(connection.node2());
        NODES.get(connection.node2()).remove(connection.node1());
        WireData connectionData = CONNECTION_DATA.remove(connection);
        if (connectionData != null) {
            for (Pair<Float, WireAttachment> attachment : connectionData.attachments()) {
                Vec3 pos = QuadraticWireHelper.posAt(Vec3.atCenterOf(connection.node1().sourcePos()), Vec3.atCenterOf(connection.node2().sourcePos()), attachment.getFirst(), connectionData.wireType().getSag());

                for (ItemStack stack : attachment.getSecond().getDrops(level))
                    Containers.dropItemStack(level, pos.x(), pos.y(), pos.z(), stack);

            }
        }

        WireSync.handleWireRemoved(connection, level);
        wireSimulationState.removeConnection(connection);
//        wireConnectionManager.wireRemoved(connection);
        setDirty();
        return connectionData;
    }

    public Map<InWorldNodeConnection, WireData> getAllConnections() {
        return CONNECTION_DATA;
    }

    public InWorldNodeConnection connect(InWorldNode node1, InWorldNode node2, WireType wireType) {
        if (node1.equals(node2))
            throw new IllegalArgumentException("Tried to connect a wire between a single node: " + node1);

        if (!(NODES.containsKey(node1) && NODES.containsKey(node2)))
            throw new IllegalArgumentException("Node: " + (NODES.containsKey(node2) ? node1.toString() : node2.toString()) + "doesn't exist.");
        NODES.compute(node1, (node, nodes) -> {
            nodes.add(node2);
            return nodes;
        });
        NODES.compute(node2, (node, nodes) -> {
            nodes.add(node1);
            return nodes;
        });
        InWorldNodeConnection connection = new InWorldNodeConnection(node1, node2);
        WireData data = new WireData(wireType, 0f, Collections.emptyList());
        CONNECTION_DATA.put(connection, data);
        setDirty();

        WireSync.handleWireAdded(connection, data, level);
//        wireConnectionManager.wireAdded(connection, data);
        wireSimulationState.addConnection(connection, data, false);
        return connection;
    }

    public boolean isConnected(InWorldNode node1, InWorldNode node2) {
        return NODES.getOrDefault(node1, Collections.emptyList()).contains(node2) || (node1.id() == 0 && node2.id() == 0 && CATENARY_ADJACENCY.getOrDefault(node1.sourcePos(), Collections.emptyList()).contains(node2.sourcePos()));
    }

    public float getConnectionTemperature(InWorldNodeConnection connection) {
        WireData data = CONNECTION_DATA.get(connection);
        return data == null ? 0f : data.temperature;
    }

    public void setConnectionTemperature(InWorldNodeConnection connection, float temp) {
        WireData wireData = CONNECTION_DATA.get(connection);
        if (wireData != null)
            wireData.temperature = temp;
    }

    public float updateConnectionTemperature(InWorldNodeConnection connection, Float2FloatFunction function) {
        WireData wireData = CONNECTION_DATA.get(connection);
        if (wireData == null)
            return 0;
        return wireData.temperature = function.get(wireData.temperature);
    }

    public WireData getConnectionData(InWorldNodeConnection connection) {
        return CONNECTION_DATA.get(connection);
    }

    public void setConnectionData(InWorldNodeConnection connection, WireData data) {
        WireSync.handleWireAdded(connection, data, level);
        wireSimulationState.removeConnection(connection);
        wireSimulationState.addConnection(connection, data, false);
//        wireConnectionManager.wireRemoved(connection);
//        wireConnectionManager.wireAdded(connection, data);
        CONNECTION_DATA.put(connection, data);
    }

    public List<InWorldNode> getNodesAt(BlockPos pos) {
        return NODES_BY_POS.getOrDefault(pos, new ArrayList<>());
    }

    public InWorldNode getNode(BlockPos pos, int id) {
        if (!NODES_BY_POS.containsKey(pos))
            return null;
        List<InWorldNode> nodes = NODES_BY_POS.get(pos);
        for (InWorldNode node : nodes)
            if (node.id() == id)
                return node;
        return null;
    }

    public Set<InWorldNode> getNodes() {
        return NODES.keySet();
    }

    public Vec3 getNodePosition(InWorldNode node) {
        Vec3 pos = NODE_POSITIONS.get(node);
        if (pos == null || level.isLoaded(node.sourcePos())) {
            Vec3 newPos = node.getPosition(level);
            // how would that happen??
            if (newPos == null)
                return null;
            NODE_POSITIONS.replace(node, pos = newPos);
        }
        return pos;
    }

    public void connectCatenary(BlockPos pos1, BlockPos pos2) {
        CATENARY_ADJACENCY.computeIfAbsent(pos1, k -> new ArrayList<>()).add(pos2);
        CATENARY_ADJACENCY.computeIfAbsent(pos2, k -> new ArrayList<>()).add(pos1);
        BlockState startingState = level.getBlockState(pos1);
        BlockState endingState = level.getBlockState(pos2);
        boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
        boolean isLow = isStartingLow || isEndingLow;

        CatenaryConnectionData data = new CatenaryConnectionData(0, isLow);
        CATENARY_DATA.put(new CatenaryConnection(pos1, pos2), data);
        InWorldNode node1 = new InWorldNode(0, pos1);
        InWorldNode node2 = new InWorldNode(0, pos2);
        wireSimulationState.addCatenaryConnection(new InWorldNodeConnection(node1, node2), data, false);
        WireSync.handleCatenaryAdded(pos1, pos2, level);
    }

    public void removeCatenary(BlockPos pos1, BlockPos pos2) {
        CATENARY_ADJACENCY.computeIfAbsent(pos1, k -> new ArrayList<>()).remove(pos2);
        CATENARY_ADJACENCY.computeIfAbsent(pos2, k -> new ArrayList<>()).remove(pos1);
        CATENARY_DATA.remove(new CatenaryConnection(pos1, pos2));
        InWorldNode node1 = new InWorldNode(0, pos1);
        InWorldNode node2 = new InWorldNode(0, pos2);
        wireSimulationState.removeConnection(new InWorldNodeConnection(node1, node2));
        WireSync.handleCatenaryRemoved(pos1, pos2, level);
    }

    public Set<CatenaryConnection> getAllCatenaryConnections() {
       return CATENARY_DATA.keySet();
    }
}
