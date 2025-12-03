package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
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

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        SimulatedDeviceInstance<?> device = sd.getDevice(pos);

        if (device == null || !(device.simulatedDevice() instanceof EnergyMeterDevice || device.simulatedDevice() instanceof TriPolarEnergyMeterDevice) || !(device.extraData() instanceof EnergyMeterDevice.DataHolder dataHolder))
            return;

        if (reset)
            dataHolder.totalEnergy = 0;
        dataHolder.isClosed = !disconnect;
        be.disconnected = disconnect;
        be.sendData();

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CHANGE_ENERGY_METER_STATE;
    }
}
