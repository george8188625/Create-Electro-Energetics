package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.WireData;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
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
import java.util.List;
import java.util.Map;

@Mixin(SchematicPrinter.class)
public class SchematicPrinterMixin {
    @Unique
    boolean electroEnergetics$wirePhase;

    @Shadow
    private SchematicLevel blockReader;

    @Shadow
    private BlockPos currentPos;

    @Inject(method = "advanceCurrentPos", at=@At("RETURN"), remap = false, cancellable = true)
    public void electroEnergetics$advanceCurrentPos(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && (ServerLevelAccessor)blockReader instanceof ISchematicInfrastructureList sl) {
            if (sl.getWireConnections().isEmpty())
                return;
            electroEnergetics$wirePhase = true;
            InfrastructureSavedData sd = InfrastructureSavedData.load(blockReader.getLevel());

            for (Map.Entry<NodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                NodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();
                if (sd.getDevice(node1.sourcePos()) == null) {
                    SimulatedDeviceInstance<?> di = sl.getDevices().stream().filter(d -> d.pos().equals(node1.sourcePos())).toList().getFirst();
                    if (di == null)
                        continue;
                    sd.addDevice(node1.sourcePos(), di.simulatedDevice(), di.write(), di.nodes().stream().map(InWorldNode::id).toList());
                }

                if (sd.getDevice(node2.sourcePos()) == null) {
                    SimulatedDeviceInstance<?> di = sl.getDevices().stream().filter(d -> d.pos().equals(node2.sourcePos())).toList().getFirst();
                    if (di == null)
                        continue;
                    sd.addDevice(node2.sourcePos(), di.simulatedDevice(), di.write(), di.nodes().stream().map(InWorldNode::id).toList());
                }

                if (!sd.getConnections(node1).contains(connection)) {
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(node1.sourcePos().getCenter(), node2.sourcePos().getCenter(), 0.5f));
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
            if (sl.getWireConnections().isEmpty())
                return;
            InfrastructureSavedData sd = InfrastructureSavedData.load(this.blockReader.getLevel());

            for (Map.Entry<NodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                NodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (!sd.getConnections(node1).contains(node2)) {
                    List<ItemStack> allRequirements = new ArrayList<>();
                    for (Pair<Float, WireAttachment> attachment : wireData.attachments())
                        allRequirements.addAll(attachment.getSecond().getItemRequirement());
                    allRequirements.add(new ItemStack(wireData.wireType().getDrops(), 4));
                    cir.setReturnValue(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, allRequirements));
                    return;
                }
            }
        }
    }

    @Inject(method = "markAllBlockRequirements", at = @At("RETURN"), remap = false)
    public void electroEnergetics$markAllBlockRequirements(MaterialChecklist checklist, Level world, SchematicPrinter.PlacementPredicate predicate, CallbackInfoReturnable<Integer> cir) {
        if (world instanceof ServerLevel level && blockReader instanceof ISchematicInfrastructureList sl) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(level);

            for (Map.Entry<NodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                NodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (!sd.getConnections(node1).contains(new NodeConnection(node1, node2))) {
                    List<ItemStack> allRequirements = new ArrayList<>();
                    for (Pair<Float, WireAttachment> attachment : wireData.attachments())
                        allRequirements.addAll(attachment.getSecond().getItemRequirement());
                    allRequirements.add(new ItemStack(wireData.wireType().getDrops(), 4));
                    checklist.require(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, allRequirements));
                }
            }
        }
    }

    @Inject(method = "handleCurrentTarget", at = @At("HEAD"), remap = false, cancellable = true)
    public void electroEnergetics$handleCurrentTarget(SchematicPrinter.BlockTargetHandler blockHandler, SchematicPrinter.EntityTargetHandler entityHandler, CallbackInfo ci) {
        if (electroEnergetics$wirePhase && (ServerLevelAccessor)blockReader instanceof ISchematicInfrastructureList sl) {
            if (sl.getWireConnections().isEmpty())
                return;
            InfrastructureSavedData sd = InfrastructureSavedData.load(blockReader.getLevel());
            for (Map.Entry<NodeConnection, WireData> e : sl.getWireConnections().entrySet()) {
                NodeConnection connection = e.getKey();
                WireData wireData = e.getValue();
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();
                if (sd.getDevice(node1.sourcePos()) == null) {
                    SimulatedDeviceInstance<?> di = sl.getDevices().stream().filter(d -> d.pos().equals(node1.sourcePos())).toList().getFirst();
                    if (di == null)
                        continue;
                    sd.addDevice(node1.sourcePos(), di.simulatedDevice(), di.write(), di.nodes().stream().map(InWorldNode::id).toList());
                }

                if (sd.getDevice(node2.sourcePos()) == null) {
                    SimulatedDeviceInstance<?> di = sl.getDevices().stream().filter(d -> d.pos().equals(node2.sourcePos())).toList().getFirst();
                    if (di == null)
                        continue;
                    sd.addDevice(node2.sourcePos(), di.simulatedDevice(), di.write(), di.nodes().stream().map(InWorldNode::id).toList());
                }

                if (!sd.getConnections(node1).contains(connection)) {
                    sd.setConnectionData(sd.connect(node1, node2, wireData.wireType()), wireData);
                    sl.getWireConnections().remove(connection);
                    currentPos = BlockPos.containing(QuadraticWireHelper.posAt(node1.sourcePos().getCenter(), node2.sourcePos().getCenter(), 0.5f));
                    ci.cancel();
                    return;
                }
            }
            ci.cancel();
        }
    }

}
