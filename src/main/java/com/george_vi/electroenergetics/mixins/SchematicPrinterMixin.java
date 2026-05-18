package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(SchematicPrinter.class)
public abstract class SchematicPrinterMixin {
    @Unique
    boolean electroEnergetics$wirePhase;

    @Shadow
    private SchematicLevel blockReader;

    @Shadow
    private BlockPos currentPos;

    @Shadow
    private SchematicPrinter.PrintStage printStage;

    @Shadow
    private BlockPos schematicAnchor;

    @Shadow
    public abstract BlockPos getCurrentTarget();

    @Inject(method = "advanceCurrentPos", at=@At("RETURN"), remap = false, cancellable = true)
    public void electroEnergetics$advanceCurrentPos(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && (ServerLevelAccessor)blockReader instanceof ISchematicInfrastructureList sl) {
            if (sl.getWireConnections().isEmpty() && sl.getCatenaryConnections().isEmpty())
                return;
            electroEnergetics$wirePhase = true;
            InfrastructureSavedData sd = InfrastructureSavedData.load(blockReader.getLevel());

            for (Map.Entry<InWorldNodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                InWorldNodeConnection connection = e.getKey();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (!sd.isConnected(node1, node2)) {
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(node1.sourcePos().getCenter(), node2.sourcePos().getCenter(), 0.5f));
                    cir.setReturnValue(true);
                    return;
                }
            }

            for (CatenaryConnection e : sl.getCatenaryConnections()) {
                BlockPos pos1 = e.pos1();
                BlockPos pos2 = e.pos2();

                if (!sd.isConnected(new InWorldNode(0, pos1), new InWorldNode(1, pos2))) {
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(pos1.getCenter(), pos2.getCenter(), 0.5f));
                    cir.setReturnValue(true);
                    return;
                }
            }
        } else {
            electroEnergetics$wirePhase = false;
        }
    }

    @Inject(method = "getCurrentRequirement", at = @At("HEAD"), remap = false, cancellable = true)
    public void electroEnergetics$getCurrentRequirement(CallbackInfoReturnable<ItemRequirement> cir) {
        if (electroEnergetics$wirePhase && blockReader instanceof ISchematicInfrastructureList sl) {
            if (sl.getWireConnections().isEmpty() && sl.getCatenaryConnections().isEmpty())
                return;
            InfrastructureSavedData sd = InfrastructureSavedData.load(this.blockReader.getLevel());

            for (Map.Entry<InWorldNodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                InWorldNodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (sd.getConnections(node1).stream().noneMatch(c -> c.isAny(node2))) {
                    List<ItemStack> allRequirements = new ArrayList<>();
                    for (Pair<Float, WireAttachment> attachment : wireData.attachments())
                        allRequirements.addAll(attachment.getSecond().getItemRequirement());
                    allRequirements.add(new ItemStack(wireData.wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
                    cir.setReturnValue(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, allRequirements));
                    return;
                }
            }

            for (CatenaryConnection e : sl.getCatenaryConnections()) {
                BlockPos pos1 = e.pos1();
                BlockPos pos2 = e.pos2();

                if (!sd.isConnected(new InWorldNode(0, pos1), new InWorldNode(1, pos2))) {
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(pos1.getCenter(), pos2.getCenter(), 0.5f));
                    cir.setReturnValue(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, List.of(new ItemStack(CEEWireTypes.COPPER.get().getDrops(), CEEConfigs.server().wiresPerSpool.get()))));
                    return;
                }
            }
        }
    }

    @Inject(method = "markAllBlockRequirements", at = @At("RETURN"), remap = false)
    public void electroEnergetics$markAllBlockRequirements(MaterialChecklist checklist, Level world, SchematicPrinter.PlacementPredicate predicate, CallbackInfoReturnable<Integer> cir) {
        if (world instanceof ServerLevel level && blockReader instanceof ISchematicInfrastructureList sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(level);

            for (Map.Entry<InWorldNodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                InWorldNodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (!sd.getConnections(node1).contains(new InWorldNodeConnection(node1, node2))) {
                    List<ItemStack> allRequirements = new ArrayList<>();
                    for (Pair<Float, WireAttachment> attachment : wireData.attachments())
                        allRequirements.addAll(attachment.getSecond().getItemRequirement());
                    allRequirements.add(new ItemStack(wireData.wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
                    checklist.require(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, allRequirements));
                }
            }


            for (CatenaryConnection e : sl.getCatenaryConnections()) {
                BlockPos pos1 = e.pos1();
                BlockPos pos2 = e.pos2();

                if (!sd.isConnected(new InWorldNode(0, pos1), new InWorldNode(1, pos2))) {
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(pos1.getCenter(), pos2.getCenter(), 0.5f));
                    checklist.require(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, new ItemStack(CEEWireTypes.COPPER.get().getDrops(), CEEConfigs.server().wiresPerSpool.get())));
                }
            }
        }
    }

    @Inject(method = "handleCurrentTarget", at = @At("HEAD"), remap = false, cancellable = true)
    public void electroEnergetics$handleCurrentTarget(SchematicPrinter.BlockTargetHandler blockHandler, SchematicPrinter.EntityTargetHandler entityHandler, CallbackInfo ci) {
        if (!((ServerLevelAccessor)blockReader instanceof ISchematicInfrastructureList sl))
            return;
        if (printStage == SchematicPrinter.PrintStage.BLOCKS) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(blockReader.getLevel());

            for (Map.Entry<InWorldNode, String> e : sl.getNodeLabels().entrySet()) {
                InWorldNode node = e.getKey();
                String label = e.getValue();
                if (node.sourcePos().equals(getCurrentTarget())) {
                    InWorldNodeData data = sd.getNodeData(node);
                    if (data == null)
                        data = sd.createNode(node);

                    data.label = label;
                    sd.wireSync.handleNodeLabelRename(data);
                }

            }
        }
        if (electroEnergetics$wirePhase) {
            if (sl.getWireConnections().isEmpty() && sl.getCatenaryConnections().isEmpty())
                return;
            InfrastructureSavedData sd = InfrastructureSavedData.load(blockReader.getLevel());
            DevicesSavedData deviceSD = DevicesSavedData.load(blockReader.getLevel());
            for (Map.Entry<InWorldNodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                InWorldNodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (deviceSD.getDevice(node1.sourcePos()) == null)
                    deviceSD.addDevice(CEESimulatedDevices.TEMPORARY.get(), node1.sourcePos(), new CompoundTag());
                if (deviceSD.getDevice(node2.sourcePos()) == null)
                    deviceSD.addDevice(CEESimulatedDevices.TEMPORARY.get(), node2.sourcePos(), new CompoundTag());

                if (!sd.hasNode(node1))
                    sd.createNode(node1);
                if (!sd.hasNode(node2))
                    sd.createNode(node2);

                if (!sd.getConnections(node1).contains(connection)) {
                    sd.setConnectionData(sd.connect(node1, node2, wireData.wireType()), wireData);
                    sl.getWireConnections().remove(connection);
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(node1.sourcePos().getCenter(), node2.sourcePos().getCenter(), 0.5f));
                    ci.cancel();
                    return;
                }
            }


            for (Iterator<CatenaryConnection> iterator = sl.getCatenaryConnections().iterator(); iterator.hasNext(); ) {
                CatenaryConnection e = iterator.next();
                BlockPos pos1 = e.pos1();
                BlockPos pos2 = e.pos2();

                if (!sd.isConnected(new InWorldNode(0, pos1), new InWorldNode(1, pos2))) {
                    sd.connectCatenary(pos1, pos2);
                    iterator.remove();
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(pos1.getCenter(), pos2.getCenter(), 0.5f));
                    ci.cancel();
                    return;
                }
            }

            ci.cancel();
        }
    }

}
