package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
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

        for (BlockPos originalPos : blocks) {
            BlockPos destinationPos = transform.apply(originalPos);

            List<InWorldNodeData> nodesAtPos = sd.getNodesAt(originalPos);
            List<InWorldNode> destinationNodes = nodesAtPos.stream()
                    .map(iwn -> new InWorldNode(iwn.node.id(), destinationPos))
                    .toList();

            for (int i = 0; i < nodesAtPos.size(); i++) {
                InWorldNodeData originalNodeData = nodesAtPos.get(i);
                InWorldNode originalNode = originalNodeData.node;
                InWorldNode destinationNode = destinationNodes.get(i);
                InWorldNodeData destinationNodeData = sd.getNodeData(destinationNode);

                if (destinationNodeData == null) {
                    // Create the node if it doesn't exist yet
                    Vec3 nodePos = sd.getNodePosition(originalNode);
                    Vec3 localNodePos = sd.getLocalNodePosition(originalNode);
                    if (nodePos != null && localNodePos != null)
                        sd.createNode(destinationNodes.get(i), nodePos, localNodePos);
                    else
                        continue;
                }

                for (InWorldNodeConnection connection : sd.getConnections(originalNodeData)) {
                    // Remove and place a connection and defer the update to the next tick.
                    WireData data = sd.removeConnection(connection);
                    if (data == null)
                        continue;

                    long resolvedConnection = sd.connectNoUpdate(connection.node1().equals(originalNode) ? destinationNode : connection.node1(),
                            connection.node2().equals(originalNode) ? destinationNode : connection.node2(), data);

                    sd.sableToUpdate.add(resolvedConnection);
                }
            }
        }

        DevicesSavedData devicesSD = DevicesSavedData.load(level);
        DevicesSavedData.moveSubLevelDevices(transform, blocks, devicesSD);
    }
}
