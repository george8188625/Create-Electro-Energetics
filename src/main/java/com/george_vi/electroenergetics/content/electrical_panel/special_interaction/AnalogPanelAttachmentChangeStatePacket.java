package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelDevice;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public record AnalogPanelAttachmentChangeStatePacket(BlockPos pos, int panelSlot, byte signal) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, AnalogPanelAttachmentChangeStatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AnalogPanelAttachmentChangeStatePacket::pos,
            ByteBufCodecs.VAR_INT, AnalogPanelAttachmentChangeStatePacket::panelSlot,
            ByteBufCodecs.BYTE, AnalogPanelAttachmentChangeStatePacket::signal,
            AnalogPanelAttachmentChangeStatePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        if (player.distanceToSqr(pos.getCenter()) > range * range)
            return;

        DevicesSavedData sd = DevicesSavedData.load((ServerLevel) player.level());

        ElectricalPanelDevice device = sd.getDevice(pos, ElectricalPanelDevice.class);
        if (device == null)
            return;

        if (panelSlot >= device.attachments.length || panelSlot < 0)
            return;

        PanelAttachment a = device.attachments[panelSlot];
        if (a instanceof IAnalogPanelAttachment attachment) {
            attachment.setAnalogState(Math.clamp(signal, attachment.getAnalogMin(), attachment.getAnalogBound()));
            a.sendData();
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.ANALOG_LEVER_PANEL_CHANGE_STATE;
    }
}
