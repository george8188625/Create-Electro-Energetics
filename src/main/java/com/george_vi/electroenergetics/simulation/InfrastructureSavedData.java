package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.wire_spool.LoadedWireManager;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import com.mojang.logging.LogUtils;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.*;

public class InfrastructureSavedData extends SavedData {
    Map<BlockPos, SimulatedDeviceInstance> DEVICES = new HashMap<>();
    Map<Node, List<Node>> NODES = new HashMap<>();
    Map<BlockPos, List<Node>> NODES_BY_POS = new HashMap<>();
    Map<BlockPos, CableTypes.CableType> CABLES = new HashMap<>();
    // CONNECTION - WIRE TYPE, TEMPERATURE
    Map<NodeConnection, Pair<WireTypes.WireType, Float>> CONNECTION_DATA = new HashMap<>();
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
                subNodeTag.putString("WireType", CreateElecrtoEnergetics.rl("standard").toString());
                subNodeTag.putFloat("Temperature", CONNECTION_DATA.get(new NodeConnection(e.getKey(), node)).getSecond());

                connectedNodesList.add(subNodeTag);
            }

            nodeTag.put("ConnectedNodes", connectedNodesList);
            nodeList.add(nodeTag);
        }
        compoundTag.put("Nodes", nodeList);

        // Save Cables

        ListTag cableList = new ListTag();
        for (Map.Entry<BlockPos, CableTypes.CableType> e : CABLES.entrySet()) {
            CompoundTag cableTag = new CompoundTag();
            cableTag.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
            cableTag.putString("ID", e.getValue().getID().toString());
            cableList.add(cableTag);
        }
        compoundTag.put("Cables", cableList);

        return compoundTag;
    }

    static InfrastructureSavedData load(ServerLevel level, CompoundTag compoundTag, HolderLookup.Provider provider) {
        InfrastructureSavedData sd = new InfrastructureSavedData(level);
        sd.DEVICES = new HashMap<>();
        sd.NODES = new HashMap<>();
        sd.NODES_BY_POS = new HashMap<>();
        sd.CABLES = new HashMap<>();
        sd.CONNECTION_DATA = new HashMap<>();

        // Read Nodes / Node Connections

        NBTHelper.iterateCompoundList(compoundTag.getList("Nodes", Tag.TAG_COMPOUND), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            int id = tag.getInt("ID");
            List<Node> connectedNodes = new ArrayList<>();
            NBTHelper.iterateCompoundList(tag.getList("ConnectedNodes", Tag.TAG_COMPOUND), connectionTag -> {
                connectedNodes.add(new Node(connectionTag.getInt("ID"), NBTHelper.readBlockPos(connectionTag, "Pos")));
                sd.CONNECTION_DATA.computeIfAbsent(new NodeConnection(new Node(connectionTag.getInt("ID"), NBTHelper.readBlockPos(connectionTag, "Pos")),
                        new Node(id, pos)), c -> Pair.of(WireTypes.STANDARD, connectionTag.getFloat("Temperature")));
            });
            Node node = new Node(id, pos);
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
            SimulatedDevice device = CEESimulatedDevices.get(ResourceLocation.parse(tag.getString("ID")));
            if (device == null) {
                List<Node> nodes = sd.NODES_BY_POS.get(pos);
                for (Node node : nodes) {
                    for (NodeConnection connection : sd.getConnections(node))
                        sd.removeConnection(connection);
                    sd.NODES.remove(node);
                }
                sd.NODES_BY_POS.remove(pos);
                LOGGER.warn("Could not load device: {} at pos: {} in: {}. No device with such ID, removing...", tag.getString("ID"), pos.toShortString(), level.dimension().location().toString());
                return;
            }
            sd.DEVICES.put(pos, new SimulatedDeviceInstance(device, pos, tag.getCompound("ExtraData"), sd.NODES_BY_POS.getOrDefault(pos, new ArrayList<>())));
        });

        // Read Cables

        NBTHelper.iterateCompoundList(compoundTag.getList("Cables", Tag.TAG_COMPOUND), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            CableTypes.CableType type = CableTypes.get(ResourceLocation.parse(tag.getString("ID")));
            if (type == null)
                return;
            sd.CABLES.put(pos, type);
        });
        return sd;
    }

    public static InfrastructureSavedData load(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(new Factory<>(() -> new InfrastructureSavedData(level), (compoundTag, provider) -> InfrastructureSavedData.load(level, compoundTag, provider)), "electroenergetics_infrastructure");
    }

    public void addCable(BlockPos pos, CableTypes.CableType type) {
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
                getConnections(node).forEach(this::removeConnection);
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
        CONNECTION_DATA.remove(connection);
        LoadedWireManager.handleWireRemoved(connection, level);
        setDirty();
    }

    public NodeConnection connect(Node node1, Node node2) {
        if (!(NODES.containsKey(node1) && NODES.containsKey(node2)))
            throw new IllegalArgumentException("Node: " + (NODES.containsKey(node2) ? node1.toString() : node1) + "Doesn't exist.");
        NODES.compute(node1, (node, nodes) -> {
            nodes.add(node2);
            return nodes;
        });
        NODES.compute(node2, (node, nodes) -> {
            nodes.add(node1);
            return nodes;
        });
        NodeConnection connection = new NodeConnection(node1, node2);
        CONNECTION_DATA.put(connection, Pair.of(WireTypes.STANDARD, 0f));
        setDirty();

        LoadedWireManager.handleWireAdded(connection, level);
        return connection;
    }

    public boolean isConnected(Node node1, Node node2) {
        return NODES.get(node1).contains(node2);
    }

    public float getConnectionTemperature(NodeConnection connection) {
        return CONNECTION_DATA.getOrDefault(connection, Pair.of(WireTypes.STANDARD, 0f)).getSecond();
    }

    public void setConnectionTemperature(NodeConnection connection, float temp) {
        CONNECTION_DATA.computeIfPresent(connection, (k, v) -> Pair.of(v.getFirst(), temp));
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

    public record SimulatedDeviceInstance(SimulatedDevice simulatedDevice, BlockPos pos, CompoundTag extraData, List<Node> nodes) { }
}
