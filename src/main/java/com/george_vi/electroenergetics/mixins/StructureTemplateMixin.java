package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.*;
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
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Unique
    List<InfrastructureSavedData.SimulatedDeviceInstance> electroEnergetics$allDevices = new ArrayList<>();

    @Unique
    List<Pair<NodeConnection, WireData>> electroEnergetics$allWireConnections = new ArrayList<>();

    @Inject(method = "fillFromWorld(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Vec3i;ZLnet/minecraft/world/level/block/Block;)V", at=@At("HEAD"), remap = false)
    public void electroEnergetics$fillFromWorld(Level level, BlockPos pos, Vec3i size, boolean withEntities, @Nullable Block toIgnore, CallbackInfo ci) {
        if (!CEEConfigs.server().saveInfrastructureInSchematics.get())
            return;

        // When saving schematics, Create saves them both on the client and server
        // Client -> saves the schematic to the /schematics path
        // Server -> saves the schematic to the /schematics/uploaded path
        // They both save it on its own, so the client here has to save the wires by itself, and won't unfortunately save device data, since its only server-side.
        // It makes sense, but feels weird.

        if (level instanceof ServerLevel sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
            electroEnergetics$allDevices = sd.getDevices().stream().filter(d -> d.pos().getX() >= pos.getX() && d.pos().getX() < pos.getX() + size.getX() &&
                                                                        d.pos().getY() >= pos.getY() && d.pos().getY() < pos.getY() + size.getY() &&
                                                                        d.pos().getZ() >= pos.getZ() && d.pos().getZ() < pos.getZ() + size.getZ())
                    .map(d -> new InfrastructureSavedData.SimulatedDeviceInstance(d.simulatedDevice(), d.pos().subtract(pos), d.extraData(), d.nodes().stream().map(n -> new InWorldNode(n.id(), n.sourcePos().subtract(pos))).toList())).toList();
            Set<InWorldNode> allNodes = sd.getNodes().stream().filter(d -> d.sourcePos().getX() >= pos.getX() && d.sourcePos().getX() < pos.getX() + size.getX() &&
                    d.sourcePos().getY() >= pos.getY() && d.sourcePos().getY() < pos.getY() + size.getY() &&
                    d.sourcePos().getZ() >= pos.getZ() && d.sourcePos().getZ() < pos.getZ() + size.getZ()).collect(Collectors.toSet());
            Set<NodeConnection> visitedConnections = new HashSet<>();
            for (InWorldNode node : allNodes) {
                for (NodeConnection connection : sd.getConnections(node)) {
                    if (visitedConnections.contains(connection) || !allNodes.contains(connection.node2()))
                        continue;
                    visitedConnections.add(connection);
                    WireData connectionData = sd.getConnectionData(connection);
                    if (connectionData == null)
                        continue;
                    electroEnergetics$allWireConnections.add(Pair.of(new NodeConnection(new InWorldNode(connection.node1().id(), connection.node1().sourcePos().subtract(pos)),
                            new InWorldNode(connection.node2().id(), connection.node2().sourcePos().subtract(pos))), connectionData));
                }
            }

        } else {
            Set<NodeConnection> visitedConnections = new HashSet<>();
            Set<BlockPos> devicePositions = new HashSet<>();
            for (Pair<NodeConnection, WireData> wireDataPair : WireRenderer.getAllConnections()) {
                NodeConnection connection = wireDataPair.getFirst();
                WireData wireData = wireDataPair.getSecond();
                if (visitedConnections.contains(connection))
                    continue;
                visitedConnections.add(connection);
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (node1.sourcePos().getX() >= pos.getX() && node1.sourcePos().getX() < pos.getX() + size.getX() &&
                        node1.sourcePos().getY() >= pos.getY() && node1.sourcePos().getY() < pos.getY() + size.getY() &&
                        node1.sourcePos().getZ() >= pos.getZ() && node1.sourcePos().getZ() < pos.getZ() + size.getZ() &&
                        node2.sourcePos().getX() >= pos.getX() && node2.sourcePos().getX() < pos.getX() + size.getX() &&
                        node2.sourcePos().getY() >= pos.getY() && node2.sourcePos().getY() < pos.getY() + size.getY() &&
                        node2.sourcePos().getZ() >= pos.getZ() && node2.sourcePos().getZ() < pos.getZ() + size.getZ()) {
                    devicePositions.add(node1.sourcePos());
                    devicePositions.add(node2.sourcePos());

                    electroEnergetics$allWireConnections.add(Pair.of(new NodeConnection(new InWorldNode(node1.id(), node1.sourcePos().subtract(pos)),
                            new InWorldNode(node2.id(), node2.sourcePos().subtract(pos))), wireData));
                }
            }

            for (BlockPos devicePos : devicePositions) {
                BlockState state = level.getBlockState(devicePos);
                if (state.getBlock() instanceof DeviceBlock db) {
                    // Connector as placeholder, since the blocks should be ticked after being loaded, so the proper device should be added.
                    electroEnergetics$allDevices.add(new InfrastructureSavedData.SimulatedDeviceInstance(CEESimulatedDevices.TEMPORARY, devicePos.subtract(pos), new CompoundTag(), db.getNodePositions(level, devicePos, state).keySet().stream().map(id -> new InWorldNode(id, devicePos)).toList()));
                }
            }
        }
    }

    @Inject(method = "save", at=@At("HEAD"), remap = false)
    public void electroEnergetics$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (electroEnergetics$allDevices.isEmpty())
            return;
        CompoundTag ceeTag = new CompoundTag();

        ListTag deviceList = new ListTag();
        for (InfrastructureSavedData.SimulatedDeviceInstance deviceInstance : electroEnergetics$allDevices) {
            CompoundTag deviceTag = new CompoundTag();

            deviceTag.put("Pos", NbtUtils.writeBlockPos(deviceInstance.pos()));
            deviceTag.putString("ID", deviceInstance.simulatedDevice().getID().toString());
            deviceTag.put("ExtraData", deviceInstance.extraData());

            ListTag deviceNodeList = new ListTag();
            for (InWorldNode node : deviceInstance.nodes())
                deviceNodeList.add(InWorldNode.CODEC.encodeStart(NbtOps.INSTANCE, node).getOrThrow());

            deviceTag.put("Nodes", deviceNodeList);

            deviceList.add(deviceTag);
        }
        ceeTag.put("Devices", deviceList);

        ListTag connectionList = new ListTag();
        for (Pair<NodeConnection, WireData> wireDataPair : electroEnergetics$allWireConnections) {
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
        ceeTag.put("Connections", connectionList);

        tag.put("electroenergetics_data", ceeTag);
    }

    @Inject(method = "load", at=@At("HEAD"), remap = false)
    public void electroEnergetics$load(HolderGetter<Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        if (!CEEConfigs.server().saveInfrastructureInSchematics.get())
            return;
        CompoundTag ceeTag = tag.getCompound("electroenergetics_data");
        NBTHelper.iterateCompoundList(ceeTag.getList("Devices", Tag.TAG_COMPOUND), deviceTag -> {
            BlockPos pos = NBTHelper.readBlockPos(deviceTag, "Pos");
            SimulatedDevice device = CEESimulatedDevices.get(ResourceLocation.parse(deviceTag.getString("ID")));
            if (device == null) {
                InfrastructureSavedData.LOGGER.warn("Could not load device: {} at pos: {}, while trying to load a schematic, removing...", deviceTag.getString("ID"), pos.toShortString());
                return;
            }

            ListTag nodeListTag = deviceTag.getList("Nodes", Tag.TAG_INT_ARRAY);

            electroEnergetics$allDevices.add(new InfrastructureSavedData.SimulatedDeviceInstance(device, pos, deviceTag.getCompound("ExtraData"), nodeListTag.stream().map(t -> InWorldNode.CODEC.decode(NbtOps.INSTANCE, t).getOrThrow().getFirst()).toList()));
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

            electroEnergetics$allWireConnections.add(Pair.of(new NodeConnection(node1, node2), new WireData(wireType, connectionTag.getFloat("Temperature"), attachments)));
        });
    }

    @Inject(method = "placeInWorld", at=@At("HEAD"), remap = false)
    public void electroEnergetics$placeInWorld(ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (electroEnergetics$allDevices.isEmpty())
            return;

        BoundingBox boundingBox = settings.getBoundingBox();

        ServerLevel level;
        if (serverLevel instanceof ServerLevel)
            level = (ServerLevel) serverLevel;
        else if (serverLevel instanceof SchematicLevel) {
            if (serverLevel instanceof ISchematicInfrastructureList slm) {
                for (InfrastructureSavedData.SimulatedDeviceInstance deviceInstance : electroEnergetics$allDevices) {
                    if (boundingBox == null || boundingBox.isInside(deviceInstance.pos().offset(offset)))
                        slm.electroEnergetics$getDevices().add(new InfrastructureSavedData.SimulatedDeviceInstance(deviceInstance.simulatedDevice(), StructureTemplate.calculateRelativePosition(settings, deviceInstance.pos()).offset(offset), deviceInstance.extraData(), deviceInstance.nodes()));
                }

                for (Pair<NodeConnection, WireData> wireDataPair : electroEnergetics$allWireConnections) {
                    InWorldNode node1 = new InWorldNode(wireDataPair.getFirst().node1().id(), StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node1().sourcePos()).offset(offset));
                    InWorldNode node2 = new InWorldNode(wireDataPair.getFirst().node2().id(), StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node2().sourcePos()).offset(offset));
                    WireData wireData = wireDataPair.getSecond();
                    slm.electroEnergetics$getWireConnections().put(new NodeConnection(node1, node2), wireData);
                }
            }
            return;
        } else
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        for (InfrastructureSavedData.SimulatedDeviceInstance deviceInstance : electroEnergetics$allDevices) {
            if (boundingBox == null || boundingBox.isInside(deviceInstance.pos().offset(offset)))
                sd.addDevice(StructureTemplate.calculateRelativePosition(settings, deviceInstance.pos()).offset(offset), deviceInstance.simulatedDevice(), deviceInstance.extraData(), deviceInstance.nodes().stream().map(InWorldNode::id).toList());
        }

        for (Pair<NodeConnection, WireData> wireDataPair : electroEnergetics$allWireConnections) {
            InWorldNode node1 = new InWorldNode(wireDataPair.getFirst().node1().id(), StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node1().sourcePos()).offset(offset));
            InWorldNode node2 = new InWorldNode(wireDataPair.getFirst().node2().id(), StructureTemplate.calculateRelativePosition(settings, wireDataPair.getFirst().node2().sourcePos()).offset(offset));
            WireData wireData = wireDataPair.getSecond();
            sd.setConnectionData(sd.connect(node1, node2, wireData.wireType()), wireData);
        }

    }
}
