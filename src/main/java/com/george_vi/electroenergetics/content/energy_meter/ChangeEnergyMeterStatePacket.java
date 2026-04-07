package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.simulateddevices.device.DevicesSavedData;
import com.george_vi.simulateddevices.device.SimulatedDevice;
import com.simibubi.create.AllSoundEvents;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record ChangeEnergyMeterStatePacket(boolean reset, boolean disconnect, BlockPos pos) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ChangeEnergyMeterStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChangeEnergyMeterStatePacket::reset,
            ByteBufCodecs.BOOL, ChangeEnergyMeterStatePacket::disconnect,
            BlockPos.STREAM_CODEC, ChangeEnergyMeterStatePacket::pos,
            ChangeEnergyMeterStatePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (!(player.level().getBlockEntity(pos) instanceof EnergyMeterBlockEntity be) || !(player.level() instanceof ServerLevel level))
            return;

        if (be.owner != null && !player.getUUID().equals(be.owner))
            return;

        SimulatedDevice d = DevicesSavedData.load(level).getDevice(pos);

        if (d instanceof EnergyMeterDevice device) {
            if (reset)
                device.totalEnergy = 0;
            device.isClosed = !disconnect;
        } else if (d instanceof TriPolarEnergyMeterDevice device) {
            if (reset)
                device.totalEnergy = 0;
            device.isClosed = !disconnect;
        } else
            return;

        be.disconnected = disconnect;
        be.sendData();

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CHANGE_ENERGY_METER_STATE;
    }
}
