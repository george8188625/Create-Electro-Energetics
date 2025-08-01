package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.foundation.Node;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SendVoltageDataPacket(BlockPos pos, int id, float voltage) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendVoltageDataPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SendVoltageDataPacket::pos,
            ByteBufCodecs.INT, SendVoltageDataPacket::id,
            ByteBufCodecs.FLOAT, SendVoltageDataPacket::voltage,
            SendVoltageDataPacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        WireRenderer.addVoltageData(new Node(id, pos), voltage);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_VOLTAGE_DATA;
    }
}
