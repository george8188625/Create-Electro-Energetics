package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Unique
    List<Pair<InWorldNodeConnection, WireData>> electroEnergetics$allWireConnections = new ArrayList<>();

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
            Set<InWorldNode> allNodes = sd.getNodes().stream().filter(d -> d.sourcePos().getX() >= pos.getX() && d.sourcePos().getX() < pos.getX() + size.getX() &&
                    d.sourcePos().getY() >= pos.getY() && d.sourcePos().getY() < pos.getY() + size.getY() &&
                    d.sourcePos().getZ() >= pos.getZ() && d.sourcePos().getZ() < pos.getZ() + size.getZ()).collect(Collectors.toSet());
            Set<InWorldNodeConnection> visitedConnections = new HashSet<>();
            for (InWorldNode node : allNodes) {
                for (InWorldNodeConnection connection : sd.getConnections(node)) {
                    if (visitedConnections.contains(connection) || !allNodes.contains(connection.node2()))
                        continue;
                    visitedConnections.add(connection);
                    WireData connectionData = sd.getConnectionData(connection);
                    if (connectionData == null)
                        continue;
                    electroEnergetics$allWireConnections.add(Pair.of(new InWorldNodeConnection(new InWorldNode(connection.node1().id(), connection.node1().sourcePos().subtract(pos)),
                            new InWorldNode(connection.node2().id(), connection.node2().sourcePos().subtract(pos))), connectionData));
                }
            }

        } else {
            Set<InWorldNodeConnection> visitedConnections = new HashSet<>();
            Set<BlockPos> devicePositions = new HashSet<>();
            for (Pair<InWorldNodeConnection, WireData> wireDataPair : WireRenderer.getAllConnections()) {
                InWorldNodeConnection connection = wireDataPair.getFirst();
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

                    electroEnergetics$allWireConnections.add(Pair.of(new InWorldNodeConnection(new InWorldNode(node1.id(), node1.sourcePos().subtract(pos)),
                            new InWorldNode(node2.id(), node2.sourcePos().subtract(pos))), wireData));
                }
            }
        }
    }

    @Inject(method = "save", at=@At("HEAD"), remap = false)
    public void save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag ceeTag = new CompoundTag();

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
        ceeTag.put("Connections", connectionList);

        tag.put("electroenergetics_data", ceeTag);
    }

    @Inject(method = "load", at=@At("HEAD"), remap = false)
    public void load(HolderGetter<Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        if (!CEEConfigs.server().saveInfrastructureInSchematics.get())
            return;
        CompoundTag ceeTag = tag.getCompound("electroenergetics_data");

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

            electroEnergetics$allWireConnections.add(Pair.of(new InWorldNodeConnection(node1, node2), new WireData(wireType, connectionTag.getFloat("Temperature"), attachments)));
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
                sd.addTemporaryNode(node1);
            if (!sd.hasNode(node2))
                sd.addTemporaryNode(node2);

            WireData wireData = wireDataPair.getSecond();
            sd.setConnectionData(sd.connect(node1, node2, wireData.wireType()), wireData);
        }

    }
}
