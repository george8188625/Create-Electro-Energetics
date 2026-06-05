package com.george_vi.electroenergetics.content.wire.interaction;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.simibubi.create.AllItems;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record InteractDetachedNodePacket(InWorldNode node) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, InteractDetachedNodePacket> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, InteractDetachedNodePacket::node,
            InteractDetachedNodePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        ItemStack stackInHand = player.getMainHandItem();
        if (AllItems.WRENCH.isIn(stackInHand)) {
            InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) player.level());

            InWorldNodeData nodeData = sd.getNodeData(node);
            if (nodeData == null)
                return;

            for (InWorldNodeConnection connection : sd.getConnections(nodeData))
                sd.removeAndDropConnection(connection);
            sd.removeNode(node);
        }
        if (stackInHand.getItem() instanceof IInteractDetachedNodes item) {
            item.interactDetachedNode(node, player, player.level(), stackInHand);
        }

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.INTERACT_DETACHED_NODE;
    }
}
