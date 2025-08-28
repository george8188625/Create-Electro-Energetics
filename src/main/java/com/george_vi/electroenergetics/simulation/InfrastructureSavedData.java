package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.LoadedWireManager;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.jcraft.jorbis.Block;
import com.mojang.logging.LogUtils;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

public class InfrastructureSavedData extends SavedData {
    Map<BlockPos, SimulatedDeviceInstance> DEVICES = new HashMap<>();
    Map<Node, List<Node>> NODES = new HashMap<>();
    Map<BlockPos, List<Node>> NODES_BY_POS = new HashMap<>();
    Map<BlockPos, CableType> CABLES = new HashMap<>();
    Map<NodeConnection, WireData> CONNECTION_DATA = new HashMap<>();

    // TRAINS
    Map<BlockPos, List<BlockPos>> CATENARY = new HashMap<>();

    // NOT PERSISTENT
    Map<Node, Double> VOLTAGES = new HashMap<>();
    ServerLevel level;
    public static final Logger LOGGER = LogUtils.getLogger();

    public InfrastructureSavedData(ServerLevel level) {
        this.level = level;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {

        // Save Devices

        ListTag deviceList = new ListTag();
        for (Map.Entry<BlockPos, SimulatedDeviceInstance> e : DEVICES.entrySet()) {
            CompoundTag deviceTag = new CompoundTag();

            deviceTag.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
            deviceTag.putString("ID", e.getValue().simulatedDevice.getID().toString());
            deviceTag.put("ExtraData", e.getValue().extraData());

            deviceList.add(deviceTag);
        }
        compoundTag.put("Devices", deviceList);

        // Save Nodes / Node Connections

        ListTag nodeList = new ListTag();
        for (Map.Entry<Node, List<Node>> e : NODES.entrySet()) {
            CompoundTag nodeTag = new CompoundTag();

            nodeTag.put("Pos", NbtUtils.writeBlockPos(e.getKey().sourcePos()));
            nodeTag.putInt("ID", e.getKey().id());

            ListTag connectedNodesList = new ListTag();
            for (Node node : e.getValue()) {
                CompoundTag subNodeTag = new CompoundTag();

                subNodeTag.put("Pos", NbtUtils.writeBlockPos(node.sourcePos()));
                subNodeTag.putInt("ID", node.id());
                WireData connectionData = CONNECTION_DATA.get(new NodeConnection(e.getKey(), node));

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

        // Save Cables

        ListTag cableList = new ListTag();
        for (Map.Entry<BlockPos, CableType> e : CABLES.entrySet()) {
            CompoundTag cableTag = new CompoundTag();
            cableTag.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
            cableTag.putString("ID", CEERegistries.CABLE_TYPE.getKey(e.getValue()).toString());
            cableList.add(cableTag);
        }
        compoundTag.put("Cables", cableList);

        // Save Railway Catenary

        ListTag catenaryList = new ListTag();
        for (Map.Entry<BlockPos, List<BlockPos>> e : CATENARY.entrySet()) {
            CompoundTag catenaryTag = new CompoundTag();
            ListTag connectionList = new ListTag();
            for (BlockPos otherPos : e.getValue())
                connectionList.add(NbtUtils.writeBlockPos(otherPos));
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
        sd.NODES = new HashMap<>();
        sd.NODES_BY_POS = new HashMap<>();
        sd.CABLES = new HashMap<>();
        sd.CONNECTION_DATA = new HashMap<>();
        sd.CATENARY = new HashMap<>();

        // Read Nodes / Node Connections

        NBTHelper.iterateCompoundList(compoundTag.getList("Nodes", Tag.TAG_COMPOUND), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            int id = tag.getInt("ID");
            List<Node> connectedNodes = new ArrayList<>();
            Node node = new Node(id, pos);
            NBTHelper.iterateCompoundList(tag.getList("ConnectedNodes", Tag.TAG_COMPOUND), connectionTag -> {
                Node connectedNode = new Node(connectionTag.getInt("ID"), NBTHelper.readBlockPos(connectionTag, "Pos"));
                connectedNodes.add(connectedNode);
                WireType wireType;

                try {
                    wireType = CEERegistries.WIRE_TYPE.get(ResourceLocation.parse(connectionTag.getString("WireType")));
                    if (wireType == null)
                        wireType = CEEWireTypes.STANDARD.get();
                } catch (Throwable e) {
                    LOGGER.warn("Could not load wire type between nodes: {}, {} with id: {} in: {}", node, connectedNode, connectionTag.getString("WireType"), level.dimension().location());
                    wireType = CEEWireTypes.STANDARD.get();
                }

                List<Pair<Float, WireAttachment>> attachments = new ArrayList<>();
                NBTHelper.iterateCompoundList(connectionTag.getList("Attachments", Tag.TAG_COMPOUND), attachmentTag -> {
                   float point = attachmentTag.getFloat("Point");
                   WireAttachment attachment = WireAttachment.read(attachmentTag);
                   attachments.add(Pair.of(point, attachment));
                });


                WireType finalWireType = wireType;
                sd.CONNECTION_DATA.computeIfAbsent(new NodeConnection(new Node(connectionTag.getInt("ID"), NBTHelper.readBlockPos(connectionTag, "Pos")),
                        new Node(id, pos)), c -> new WireData(finalWireType, connectionTag.getFloat("Temperature"), attachments));
            });
            sd.NODES.put(node, connectedNodes);
            sd.VOLTAGES.put(node, 0d);
            sd.NODES_BY_POS.computeIfAbsent(pos, ((p) -> new ArrayList<>()));
            sd.NODES_BY_POS.computeIfPresent(pos, ((p, nodes) -> {
                nodes.add(node);
                return nodes;
            }));
        });

        // Read Devices

        NBTHelper.iterateCompoundList(compoundTag.getList("Devices", Tag.TAG_COMPOUND), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            SimulatedDevice device = CEESimulatedDevices.get(ResourceLocation.parse(tag.getString("ID")));
            if (device == null) {
                List<Node> nodes = sd.NODES_BY_POS.get(pos);
                for (Node node : nodes) {
                    for (NodeConnection connection : sd.getConnections(node))
                        sd.removeConnection(connection);
                    sd.NODES.remove(node);
                }
                sd.NODES_BY_POS.remove(pos);
                LOGGER.warn("Could not load device: {} at pos: {} in: {}. No device with such ID, removing...", tag.getString("ID"), pos.toShortString(), level.dimension().location());
                return;
            }
            sd.DEVICES.put(pos, new SimulatedDeviceInstance(device, pos, tag.getCompound("ExtraData"), sd.NODES_BY_POS.getOrDefault(pos, new ArrayList<>())));
        });

        // Read Cables

        NBTHelper.iterateCompoundList(compoundTag.getList("Cables", Tag.TAG_COMPOUND), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            CableType cableType = null;
            try {
                cableType = CEERegistries.CABLE_TYPE.get(ResourceLocation.parse(tag.getString("ID")));
            } catch (Throwable e) {
                LOGGER.warn("Could not load cable type: {} at pos: {} in: {}", tag.getString("ID"), pos.toShortString(), level.dimension().location());
            }
            if (cableType == null)
                return;
            sd.CABLES.put(pos, cableType);
        });

        // Read Railway Catenary

        ListTag catenaryList = new ListTag();
        NBTHelper.iterateCompoundList(compoundTag.getList("Catenary", Tag.TAG_COMPOUND), tag -> {
            BlockPos from = NBTHelper.readBlockPos(tag, "From");

            tag.getList("Connections", Tag.TAG_INT_ARRAY).forEach(tg -> {
                int[] arr = ((IntArrayTag)tg).getAsIntArray();
                if (arr.length != 3) {
                    LOGGER.warn("Could not load catenary connection, invalid position, at pos: {} in: {}", from.toShortString(), level.dimension().location());
                    return;
                }

                BlockPos to = new BlockPos(arr[0], arr[1], arr[2]);

                sd.CATENARY.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            });
        });

        return sd;
    }

    public static InfrastructureSavedData load(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(new Factory<>(() -> new InfrastructureSavedData(level), (compoundTag, provider) -> InfrastructureSavedData.load(level, compoundTag, provider)), "electroenergetics_infrastructure");
    }

    public void addCable(BlockPos pos, CableType type) {
        CABLES.put(pos, type);
    }

    public void removeCable(BlockPos pos) {
        CABLES.remove(pos);
    }

    public void addDevice(BlockPos pos, SimulatedDevice device, CompoundTag extraData, List<Integer> nodeIDs) {
        if (DEVICES.containsKey(pos)) {
            SimulatedDeviceInstance di = DEVICES.get(pos);
            if (di.simulatedDevice == device) {
                List<Node> oldNodes = NODES_BY_POS.get(pos);
                List<Node> nodes = nodeIDs.stream().map(id -> new Node(id, pos)).toList();

                if (oldNodes != null && oldNodes.stream().map(Node::id).sorted().toList().equals(nodeIDs.stream().sorted().toList()))
                    return;

                if (oldNodes != null)
                    for (Node node : oldNodes) {
                        getConnections(node).forEach(this::removeConnection);
                        NODES.remove(node);
                    }

                NODES_BY_POS.replace(pos, nodes);

                for (Node node : nodes)
                    NODES.put(node, new ArrayList<>());

                return;
            } else
                removeDevice(pos);
        }

        List<Node> nodes = new ArrayList<>();
        for (int id : nodeIDs)
            nodes.add(new Node(id, pos));


        DEVICES.put(pos, new SimulatedDeviceInstance(device, pos, extraData, nodes));

        for (Node node : nodes)
            NODES.put(node, new ArrayList<>());
        NODES_BY_POS.put(pos, nodes);

        setDirty();
    }

    public void addDevice(BlockPos pos, SimulatedDevice device, List<Integer> nodeIDs) {
        addDevice(pos, device, new CompoundTag(), nodeIDs);
    }


    public void removeDevice(BlockPos pos) {
        DEVICES.remove(pos);
        List<Node> nodes = NODES_BY_POS.get(pos);
        if (nodes != null)
            for (Node node : nodes) {
                List<NodeConnection> connections = getConnections(node);
                connections.forEach(this::removeConnection);
                List<BlockPos> catenaryConnections = List.copyOf(CATENARY.getOrDefault(pos, new ArrayList<>()));
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), CEEItems.INSULATED_WIRE.asStack((catenaryConnections.size() + connections.size()) * CEEConfigs.server().wiresPerSpool.get()));
                for (BlockPos connection : catenaryConnections)
                    removeCatenary(pos, connection);
                NODES.remove(node);
            }
        NODES_BY_POS.remove(pos);
        setDirty();
    }

    public List<SimulatedDeviceInstance> getDevices(){
        return DEVICES.values().stream().toList();
    }

    public SimulatedDeviceInstance getDevice(BlockPos pos) {
        return DEVICES.get(pos);
    }

    public List<NodeConnection> getConnections(Node node) {
        List<NodeConnection> connections = new ArrayList<>();
        List<Node> connectedNodes = NODES.get(node);
        for (Node connectedNode : connectedNodes)
            connections.add(new NodeConnection(node, connectedNode));
        return connections;
    }

    public void removeConnection(NodeConnection connection) {

        NODES.get(connection.node1()).remove(connection.node2());
        NODES.get(connection.node2()).remove(connection.node1());
        WireData connectionData = CONNECTION_DATA.remove(connection);
        if (connectionData != null) {
            for (Pair<Float, WireAttachment> attachment : connectionData.attachments()) {
                Vec3 pos;
                if (connection.node1().compareTo(connection.node2()) > 0)
                    pos = QuadraticWireHelper.posAt(Vec3.atCenterOf(connection.node1().sourcePos()), Vec3.atCenterOf(connection.node2().sourcePos()), 1.0f - attachment.getFirst());
                else
                    pos = QuadraticWireHelper.posAt(Vec3.atCenterOf(connection.node1().sourcePos()), Vec3.atCenterOf(connection.node2().sourcePos()), attachment.getFirst());

                for (ItemStack stack : attachment.getSecond().getDrops(level))
                    Containers.dropItemStack(level, pos.x(), pos.y(), pos.z(), stack);

            }
        }
        LoadedWireManager.handleWireRemoved(connection, level);
        setDirty();
    }

    public NodeConnection connect(Node node1, Node node2, WireType wireType) {

        if (!(NODES.containsKey(node1) && NODES.containsKey(node2)))
            throw new IllegalArgumentException("Node: " + (NODES.containsKey(node2) ? node1.toString() : node1) + "doesn't exist.");
        NODES.compute(node1, (node, nodes) -> {
            nodes.add(node2);
            return nodes;
        });
        NODES.compute(node2, (node, nodes) -> {
            nodes.add(node1);
            return nodes;
        });
        NodeConnection connection = new NodeConnection(node1, node2);
        WireData data = new WireData(wireType, 0f, Collections.emptyList());
        CONNECTION_DATA.put(connection, data);
        setDirty();

        LoadedWireManager.handleWireAdded(connection, data, level);
        return connection;
    }

    public boolean isConnected(Node node1, Node node2) {
        return NODES.get(node1).contains(node2) || (node1.id() == 0 && node2.id() == 0 && CATENARY.getOrDefault(node1.sourcePos(), Collections.emptyList()).contains(node2.sourcePos()));
    }

    public float getConnectionTemperature(NodeConnection connection) {
        WireData data = CONNECTION_DATA.get(connection);
        return data == null ? 0f : data.temperature();
    }

    public void setConnectionTemperature(NodeConnection connection, float temp) {
        CONNECTION_DATA.computeIfPresent(connection, (k, v) -> new WireData(v.wireType(), temp, v.attachments()));
    }

    public WireData getConnectionData(NodeConnection connection) {
        return CONNECTION_DATA.get(connection);
    }

    public void setConnectionData(NodeConnection connection, WireData data) {
        LoadedWireManager.handleWireAdded(connection, data, level);
        CONNECTION_DATA.put(connection, data);
    }

    public List<Node> getNodesAt(BlockPos pos) {
        return NODES_BY_POS.getOrDefault(pos, new ArrayList<>());
    }

    public Node getNode(BlockPos pos, int id) {
        if (!NODES_BY_POS.containsKey(pos))
            return null;
        List<Node> nodes = NODES_BY_POS.get(pos);
        for (Node node : nodes)
            if (node.id() == id)
                return node;
        return null;
    }

    public List<Node> getNodes(){
        return NODES.keySet().stream().toList();
    }

    public double getVoltageAt(Node node) {
        return VOLTAGES.getOrDefault(node, 0d);
    }

    public void setVoltage(Node node, double v) {
        VOLTAGES.put(node, v);
    }

    public void connectCatenary(BlockPos pos1, BlockPos pos2) {
        CATENARY.computeIfAbsent(pos1, k -> new ArrayList<>()).add(pos2);
        CATENARY.computeIfAbsent(pos2, k -> new ArrayList<>()).add(pos1);
        LoadedWireManager.handleCatenaryAdded(pos1, pos2, level);
    }

    public void removeCatenary(BlockPos pos1, BlockPos pos2) {
        CATENARY.computeIfAbsent(pos1, k -> new ArrayList<>()).remove(pos2);
        CATENARY.computeIfAbsent(pos2, k -> new ArrayList<>()).remove(pos1);
        LoadedWireManager.handleCatenaryRemoved(pos1, pos2, level);
    }

    public List<Couple<BlockPos>> getAllCatenaryConnections() {
        List<Couple<BlockPos>> connections = new ArrayList<>();
        Map<BlockPos, List<BlockPos>> catenary = Map.copyOf(CATENARY);
        for (Map.Entry<BlockPos, List<BlockPos>> e : catenary.entrySet()) {
            BlockPos from = e.getKey();
            for (BlockPos to : e.getValue())
                if (!connections.contains(Couple.create(to, from)))
                    connections.add(Couple.create(from, to));
        }
        return connections;
    }


    public record SimulatedDeviceInstance(SimulatedDevice simulatedDevice, BlockPos pos, CompoundTag extraData, List<Node> nodes) { }
}
