package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record RequestVoltageDataPacket(InWorldNode node) implements ServerboundPacketPayload {

    public static final StreamCodec<ByteBuf, RequestVoltageDataPacket> STREAM_CODEC = StreamCodec.composite(
            InWorldNode.STREAM_CODEC, RequestVoltageDataPacket::node,
            RequestVoltageDataPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel)player.level());
        if (!sd.hasNode(node))
            return;
        Vec3 position = sd.getNodePosition(node);

        if (position == null || position.distanceToSqr(player.position()) > 30)
            return;

        SendVoltageDataPacket packet = new SendVoltageDataPacket();
        packet.nodes = new InWorldNode[1];
        int microTicks = packet.microTicks = sd.ticker.lastResults.microTicks;
        double[] voltages = sd.ticker.lastResults.voltages;

        packet.voltages = new double[microTicks];
        packet.frequencies = new float[1];

        packet.nodes[0] = node;

        int id1 = sd.ticker.lastResults.circuitBuilder.nodeIndexes.getInt(node);
        if (id1 == -1) {
            for (int j = 0; j < microTicks; j++)
                packet.voltages[j] = 0;
            CatnipServices.NETWORK.sendToClient(player, packet);
            return;
        }

        id1 = id1 * microTicks;
        System.arraycopy(voltages, id1, packet.voltages, 0, microTicks);

        CatnipServices.NETWORK.sendToClient(player, packet);

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.REQUEST_VOLTAGE_DATA;
    }
}
