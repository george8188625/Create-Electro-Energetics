package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.wire_spool.ClearWireConnectionsPacket;
import com.george_vi.electroenergetics.content.wire_spool.SendWireConnectionsPacket;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class InfrastructureSavedData extends SavedData {
    Map<BlockPos, SimulatedDeviceInstance> DEVICES = new HashMap<>();
    Map<Node, List<Node>> NODES = new HashMap<>();
    Map<BlockPos, List<Node>> NODES_BY_POS = new HashMap<>();
    Map<BlockPos, CableTypes.CableType> CABLES = new HashMap<>();
    // CONNECTION - WIRE TYPE, TEMPERATURE
    Map<NodeConnection, Pair<WireTypes.WireType, Float>> CONNECTION_DATA = new HashMap<>();
    ServerLevel level;

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
            SimulatedDevice device = SimulatedDevices.get(ResourceLocation.parse(tag.getString("ID")));
            if (device == null)
                return;
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
        if (DEVICES.containsKey(pos))
            if (DEVICES.get(pos).simulatedDevice == device)
                return;
            else
                removeDevice(pos);

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
        for (ServerPlayer player : level.getPlayers(player -> player.blockPosition().atY(0).distManhattan(connection.node1().sourcePos().atY(0)) < 300
                || player.blockPosition().atY(0).distManhattan(connection.node2().sourcePos().atY(0)) < 300))
            CatnipServices.NETWORK.sendToClient(player, new ClearWireConnectionsPacket(connection.node1().sourcePos(), connection.node1().id(), connection.node2().sourcePos(), connection.node2().id(), false));

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
        CONNECTION_DATA.put(new NodeConnection(node1, node2), Pair.of(WireTypes.STANDARD, 0f));
        setDirty();

        for (ServerPlayer player : level.getPlayers(player -> player.blockPosition().atY(0).distManhattan(node1.sourcePos().atY(0)) < 250
         || player.blockPosition().atY(0).distManhattan(node2.sourcePos().atY(0)) < 250))
            CatnipServices.NETWORK.sendToClient(player, new SendWireConnectionsPacket(node1.sourcePos(), node1.id(), node2.sourcePos(), node2.id()));

        return new NodeConnection(node1, node2);
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
