package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelDevice;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record AnalogLeverPanelAttachmentChangeStatePacket(BlockPos pos, int panelSlot, byte signal) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, AnalogLeverPanelAttachmentChangeStatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AnalogLeverPanelAttachmentChangeStatePacket::pos,
            ByteBufCodecs.VAR_INT, AnalogLeverPanelAttachmentChangeStatePacket::panelSlot,
            ByteBufCodecs.BYTE, AnalogLeverPanelAttachmentChangeStatePacket::signal,
            AnalogLeverPanelAttachmentChangeStatePacket::new
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
        if (a instanceof AnalogLeverPanelAttachment attachment) {
            attachment.redstoneSignal = Math.clamp(signal, 0, 15);
            attachment.sendData();
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.ANALOG_LEVER_PANEL_CHANGE_STATE;
    }
}
