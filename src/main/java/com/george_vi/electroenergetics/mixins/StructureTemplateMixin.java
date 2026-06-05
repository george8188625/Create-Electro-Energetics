package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.client.ClientNodeData;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.BoundingBoxUtils;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Unique
    List<Pair<InWorldNodeConnection, WireData>> electroEnergetics$allWireConnections = new ArrayList<>();

    @Unique
    Map<InWorldNode, String> electroEnergetics$nodeLabels = new HashMap<>();

    @Unique
    List<CatenaryConnection> electroEnergetics$allCatenaryConnections = new ArrayList<>();

    @Inject(method = "fillFromWorld(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Vec3i;ZLnet/minecraft/world/level/block/Block;)V", at=@At("HEAD"), remap = false)
    public void fillFromWorld(Level level, BlockPos pos, Vec3i size, boolean withEntities, @Nullable Block toIgnore, CallbackInfo ci) {
        if (!CEEConfigs.server().saveInfrastructureInSchematics.get())
            return;

        // When saving schematics, Create saves them both on the client and server
        // Client -> saves the schematic to the /schematics path
        // Server -> saves the schematic to the /schematics/uploaded path
        // They both save it on its own, so the client here has to save the wires by itself, and won't unfortunately save device data, since its only server-side.
        // It makes sense, but feels weird.

        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            Set<InWorldNode> allNodes = sd.getNodes().stream()
                    .filter(d -> BoundingBoxUtils.isIn(d.sourcePos(), pos, size))
                    .collect(Collectors.toSet());

            for (CatenaryConnection connection : sd.getAllCatenaryConnections()) {
                BlockPos pos1 = connection.pos1();
                BlockPos pos2 = connection.pos2();
                if (BoundingBoxUtils.isIn(pos1, pos, size) && BoundingBoxUtils.isIn(pos2, pos, size))
                    electroEnergetics$allCatenaryConnections.add(new CatenaryConnection(pos1.subtract(pos), pos2.subtract(pos)));
            }

            for (InWorldNode node : allNodes) {
                InWorldNodeData nodeData = sd.getNodeData(node);
                if (nodeData != null && nodeData.label != null)
                    electroEnergetics$nodeLabels.put(new InWorldNode(node.id(), node.sourcePos().subtract(pos)), nodeData.label);

                for (InWorldNodeConnection connection : sd.getConnections(node)) {
                    // Only runs for the connection in one direction, since nodes in IWNC are canonicalized.
                    if (node == connection.node1())
                        continue;

                    if (!BoundingBoxUtils.isIn(connection.node1().sourcePos(), pos, size))
                        continue;

                    WireData connectionData = sd.getConnectionData(connection);
                    if (connectionData == null)
                        continue;
                    electroEnergetics$allWireConnections.add(Pair.of(
                            new InWorldNodeConnection(
                                    new InWorldNode(connection.node1().id(), connection.node1().sourcePos().subtract(pos)),
                                    new InWorldNode(connection.node2().id(), connection.node2().sourcePos().subtract(pos))
                            ),
                            connectionData));
                }
            }

        } else {
            Set<InWorldNodeConnection> visitedConnections = new HashSet<>();

            for (CatenaryConnection connection : WireRenderer.CATENARY) {
                BlockPos pos1 = connection.pos1();
                BlockPos pos2 = connection.pos2();
                if (BoundingBoxUtils.isIn(pos1, pos, size) && BoundingBoxUtils.isIn(pos2, pos, size))
                    electroEnergetics$allCatenaryConnections.add(new CatenaryConnection(pos1.subtract(pos), pos2.subtract(pos)));
            }

            for (Map.Entry<InWorldNode, ClientNodeData> e : WireRenderer.getNodeData().entrySet()) {
                InWorldNode node = e.getKey();
                ClientNodeData nodeData = e.getValue();
                if (nodeData.label == null)
                    continue;

                BlockPos nodePos = node.sourcePos();
                if (!BoundingBoxUtils.isIn(nodePos, pos, size))
                    continue;

                electroEnergetics$nodeLabels.put(new InWorldNode(node.id(), node.sourcePos().subtract(pos)), nodeData.label);
            }

            for (Pair<InWorldNodeConnection, WireData> wireDataPair : WireRenderer.getAllConnections()) {
                InWorldNodeConnection connection = wireDataPair.getFirst();
                WireData wireData = wireDataPair.getSecond();
                if (visitedConnections.contains(connection))
                    continue;
                visitedConnections.add(connection);
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (BoundingBoxUtils.isIn(node1.sourcePos(), pos, size) &&
                        BoundingBoxUtils.isIn(node2.sourcePos(), pos, size)) {

                    electroEnergetics$allWireConnections.add(Pair.of(new InWorldNodeConnection(new InWorldNode(node1.id(), node1.sourcePos().subtract(pos)),
                            new InWorldNode(node2.id(), node2.sourcePos().subtract(pos))), wireData));
                }
            }
        }
    }

    @Inject(method = "save", at=@At("HEAD"), remap = false)
    public void save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag ceeTag = new CompoundTag();

        ListTag catenaryList = new ListTag();
        for (CatenaryConnection connection : electroEnergetics$allCatenaryConnections) {
            CompoundTag catenaryTag = new CompoundTag();

            catenaryTag.put("Pos1", NbtUtils.writeBlockPos(connection.pos1()));
            catenaryTag.put("Pos2", NbtUtils.writeBlockPos(connection.pos2()));

            catenaryList.add(catenaryTag);
        }

        ListTag nodeList = new ListTag();

        for (Map.Entry<InWorldNode, String> e : electroEnergetics$nodeLabels.entrySet()) {
            CompoundTag nodeTag = new CompoundTag();
            InWorldNode node = e.getKey();
            String label = e.getValue();
            nodeTag.put("Node", InWorldNode.CODEC.encodeStart(NbtOps.INSTANCE, node).getOrThrow());
            nodeTag.putString("Label", label);
            nodeList.add(nodeTag);
        }

        ListTag connectionList = new ListTag();
        for (Pair<InWorldNodeConnection, WireData> wireDataPair : electroEnergetics$allWireConnections) {
            CompoundTag connectionTag = new CompoundTag();

            InWorldNode node1 = wireDataPair.getFirst().node1();
            InWorldNode node2 = wireDataPair.getFirst().node2();
            WireData connectionData = wireDataPair.getSecond();

            connectionTag.put("Node1", InWorldNode.CODEC.encodeStart(NbtOps.INSTANCE, node1).getOrThrow());
            connectionTag.put("Node2", InWorldNode.CODEC.encodeStart(NbtOps.INSTANCE, node2).getOrThrow());

            ListTag attachmentList = new ListTag();
            for (Pair<Float, WireAttachment> attachment : connectionData.attachments()) {
                CompoundTag attachmentTag = attachment.getSecond().write();
                attachmentTag.putFloat("Point", attachment.getFirst());
                attachmentList.add(attachmentTag);
            }
            connectionTag.put("Attachments", attachmentList);

            connectionTag.putString("WireType", CEERegistries.WIRE_TYPE.getKey(connectionData.wireType()).toString());
            connectionTag.putFloat("Temperature", connectionData.temperature());

            connectionList.add(connectionTag);
        }
        if (!connectionList.isEmpty())
            ceeTag.put("Connections", connectionList);
        if (!catenaryList.isEmpty())
            ceeTag.put("Catenary", catenaryList);
        if (!nodeList.isEmpty())
            ceeTag.put("Nodes", nodeList);

        if (!ceeTag.isEmpty())
            tag.put("electroenergetics_data", ceeTag);
    }

    @Inject(method = "load", at=@At("HEAD"), remap = false)
    public void load(HolderGetter<Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        if (!CEEConfigs.server().saveInfrastructureInSchematics.get())
            return;
        CompoundTag ceeTag = tag.getCompound("electroenergetics_data");

        NBTHelper.iterateCompoundList(ceeTag.getList("Catenary", Tag.TAG_COMPOUND), connectionTag -> {
            BlockPos pos1 = NBTHelper.readBlockPos(connectionTag, "Pos1");
            BlockPos pos2 = NBTHelper.readBlockPos(connectionTag, "Pos2");

            electroEnergetics$allCatenaryConnections.add(new CatenaryConnection(pos1, pos2));
        });

        NBTHelper.iterateCompoundList(ceeTag.getList("Nodes", Tag.TAG_COMPOUND), nodeTag -> {
            InWorldNode node = InWorldNode.CODEC.decode(NbtOps.INSTANCE, nodeTag.get("Node")).getOrThrow().getFirst();
            String label = nodeTag.getString("Label");

            electroEnergetics$nodeLabels.put(node, label);
        });

        NBTHelper.iterateCompoundList(ceeTag.getList("Connections", Tag.TAG_COMPOUND), connectionTag -> {
            InWorldNode node1 = InWorldNode.CODEC.decode(NbtOps.INSTANCE, connectionTag.get("Node1")).getOrThrow().getFirst();
            InWorldNode node2 = InWorldNode.CODEC.decode(NbtOps.INSTANCE, connectionTag.get("Node2")).getOrThrow().getFirst();
            WireType wireType;

            try {
                wireType = CEERegistries.WIRE_TYPE.get(ResourceLocation.parse(connectionTag.getString("WireType")));
                if (wireType == null) {
                    InfrastructureSavedData.LOGGER.warn("Could not load wire type between nodes: {}, {} with id: {} in a schematic, changing to standard...", node1, node2, connectionTag.getString("WireType"));
                    wireType = CEEWireTypes.STANDARD.get();
                }
            } catch (Throwable e) {
                InfrastructureSavedData.LOGGER.warn("Could not load wire type between nodes: {}, {} with id: {} in a schematic, changing to standard...", node1, node2, connectionTag.getString("WireType"));
                wireType = CEEWireTypes.STANDARD.get();
            }

            List<Pair<Float, WireAttachment>> attachments = new ArrayList<>();
            NBTHelper.iterateCompoundList(connectionTag.getList("Attachments", Tag.TAG_COMPOUND), attachmentTag -> {
                float point = attachmentTag.getFloat("Point");
                WireAttachment attachment = WireAttachment.read(attachmentTag);
                attachments.add(Pair.of(point, attachment));
            });

            electroEnergetics$allWireConnections.add(Pair.of(new InWorldNodeConnection(node1, node2), new WireData(wireType, connectionTag.getFloat("Temperature"), attachments, connectionTag.getDouble("Length"))));
        });
    }

    @Inject(method = "placeInWorld", at=@At("HEAD"), remap = false)
    public void placeInWorld(ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        BoundingBox boundingBox = settings.getBoundingBox();

        ServerLevel level;
        if (serverLevel instanceof ServerLevel)
            level = (ServerLevel) serverLevel;
        else if (serverLevel instanceof SchematicLevel) {
            if (serverLevel instanceof ISchematicInfrastructureList slm) {
                for (Pair<InWorldNodeConnection, WireData> wireDataPair : electroEnergetics$allWireConnections) {
                    InWorldNode node1 = new InWorldNode(wireDataPair.getFirst().node1().id(), StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node1().sourcePos()).offset(offset));
                    InWorldNode node2 = new InWorldNode(wireDataPair.getFirst().node2().id(), StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node2().sourcePos()).offset(offset));
                    WireData wireData = wireDataPair.getSecond();
                    slm.getWireConnections().put(new InWorldNodeConnection(node1, node2), wireData);
                }

                for (CatenaryConnection connection : electroEnergetics$allCatenaryConnections) {
                    BlockPos pos1 = StructureTemplate.calculateRelativePosition(settings, connection.pos1()).offset(offset);
                    BlockPos pos2 = StructureTemplate.calculateRelativePosition(settings, connection.pos2()).offset(offset);
                    slm.getCatenaryConnections().add(new CatenaryConnection(pos1, pos2));
                }

                for (Map.Entry<InWorldNode, String> e : electroEnergetics$nodeLabels.entrySet()) {
                    InWorldNode node = e.getKey();
                    slm.getNodeLabels().put(new InWorldNode(node.id(), StructureTemplate.calculateRelativePosition(settings, node.sourcePos()).offset(offset)), e.getValue());
                }


            }
            return;
        } else
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        DevicesSavedData deviceSD = DevicesSavedData.load(level);

        for (Pair<InWorldNodeConnection, WireData> wireDataPair : electroEnergetics$allWireConnections) {
            BlockPos pos1 = StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node1().sourcePos()).offset(offset);
            InWorldNode node1 = new InWorldNode(wireDataPair.getFirst().node1().id(), pos1);
            BlockPos pos2 = StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node2().sourcePos()).offset(offset);
            InWorldNode node2 = new InWorldNode(wireDataPair.getFirst().node2().id(), pos2);

            if (deviceSD.getDevice(pos1) == null)
                deviceSD.addDevice(CEESimulatedDevices.TEMPORARY.get(), pos1, new CompoundTag());
            if (deviceSD.getDevice(pos2) == null)
                deviceSD.addDevice(CEESimulatedDevices.TEMPORARY.get(), pos2, new CompoundTag());

            if (!sd.hasNode(node1))
                sd.createNode(node1);
            if (!sd.hasNode(node2))
                sd.createNode(node2);

            WireData wireData = wireDataPair.getSecond();
            sd.setConnectionData(sd.connect(node1, node2, wireData.wireType()), wireData);
        }


        for (CatenaryConnection connection : electroEnergetics$allCatenaryConnections) {
            BlockPos pos1 = StructureTemplate.calculateRelativePosition(settings, connection.pos1()).offset(offset);
            BlockPos pos2 = StructureTemplate.calculateRelativePosition(settings, connection.pos2()).offset(offset);

            sd.connectCatenary(pos1, pos2);
        }

        for (Map.Entry<InWorldNode, String> e : electroEnergetics$nodeLabels.entrySet()) {
            InWorldNode node = e.getKey();
            String label = e.getValue();

            InWorldNodeData nodeData = sd.getNodeData(node);
            if (nodeData == null)
                nodeData = sd.createNode(node);

            nodeData.label = label;
            sd.wireSync.handleNodeLabelRename(nodeData);
        }

    }
}
