package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelDevice;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.EnergyMeterAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.simibubi.create.AllSoundEvents;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ChangeEnergyMeterStatePacket(boolean reset, boolean disconnect, BlockPos pos) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ChangeEnergyMeterStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChangeEnergyMeterStatePacket::reset,
            ByteBufCodecs.BOOL, ChangeEnergyMeterStatePacket::disconnect,
            BlockPos.STREAM_CODEC, ChangeEnergyMeterStatePacket::pos,
            ChangeEnergyMeterStatePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {

        ServerLevel level = (ServerLevel) player.level();
        BlockEntity blockEntity = player.level().getBlockEntity(pos);

        if (blockEntity instanceof EnergyMeterBlockEntity be) {
            if (be.owner != null && !player.getUUID().equals(be.owner))
                return;

            be.disconnected = disconnect;
            be.sendData();
        } else if (!(blockEntity instanceof ElectricalPanelBlockEntity))
            return;


        SimulatedDevice d = DevicesSavedData.load(level).getDevice(pos);

        switch (d) {
            case EnergyMeterDevice device -> {
                if (reset)
                    device.totalEnergy = 0;
                device.isClosed = !disconnect;
            }
            case TriPolarEnergyMeterDevice device -> {
                if (reset)
                    device.totalEnergy = 0;
                device.isClosed = !disconnect;
            }
            case ElectricalPanelDevice device -> {
                if (device.attachments.length != 1)
                    return;
                PanelAttachment attachment = device.attachments[0];
                if (!(attachment instanceof EnergyMeterAttachment meter))
                    return;
                if (reset)
                    meter.totalEnergy = 0;
                meter.disconnected = disconnect;
                meter.sendData();
            }
            case null, default -> {
                return;
            }
        }
        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CHANGE_ENERGY_METER_STATE;
    }
}
