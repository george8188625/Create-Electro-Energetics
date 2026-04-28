package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {

    @Inject(method = "moveBlocks", at=@At("HEAD"), remap = false)
    private static void moveNodesAndWires(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform,
                                          Iterable<BlockPos> blocks, CallbackInfo ci) {
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        sd.sableToUpdate.clear();

        for (BlockPos originalPos : blocks) {
            BlockPos destinationPos = transform.apply(originalPos);

            List<InWorldNode> nodesAtPos = sd.getNodesAt(originalPos);
            List<InWorldNode> destinationNodes = nodesAtPos.stream()
                    .map(iwn -> new InWorldNode(iwn.id(), destinationPos))
                    .toList();

            for (int i = 0; i < nodesAtPos.size(); i++) {
                InWorldNode originalNode = nodesAtPos.get(i);
                InWorldNode destinationNode = destinationNodes.get(i);

                for (InWorldNodeConnection connection : sd.getConnections(originalNode)) {
                    WireData data = sd.removeConnection(connection);
                    if (data == null)
                        continue;

                    Vec3 nodePos = sd.getNodePosition(originalNode);
                    Vec3 localNodePos = sd.getLocalNodePosition(originalNode);
                    if (nodePos != null && localNodePos != null)
                        sd.createNode(destinationNode, nodePos, localNodePos);
                    else
                        continue;

                    sd.sableToUpdate.add(sd.connectNoUpdate(connection.node1().equals(originalNode) ? destinationNode : connection.node1(),
                            connection.node2().equals(originalNode) ? destinationNode : connection.node2(), data));
                }
            }
        }

        DevicesSavedData devicesSD = DevicesSavedData.load(level);
        DevicesSavedData.moveSubLevelDevices(transform, blocks, devicesSD);
    }
}
