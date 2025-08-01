package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.energy_meter.ChangeEnergyMeterStatePacket;
import com.george_vi.electroenergetics.content.wire.ClearWireConnectionsPacket;
import com.george_vi.electroenergetics.content.wire.InteractWirePacket;
import com.george_vi.electroenergetics.content.wire.SendWireConnectionsPacket;
import com.george_vi.electroenergetics.simulation.SendVoltageDataPacket;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum CEEPackets implements BasePacketPayload.PacketTypeProvider {
    SEND_WIRE_CONNECTIONS(SendWireConnectionsPacket.class, SendWireConnectionsPacket.STREAM_CODEC),
    SEND_VOLTAGE_DATA(SendVoltageDataPacket.class, SendVoltageDataPacket.STREAM_CODEC),
    CLEAR_WIRE_CONNECTIONS(ClearWireConnectionsPacket.class, ClearWireConnectionsPacket.STREAM_CODEC),
    INTERACT_WIRE(InteractWirePacket.class, InteractWirePacket.STREAM_CODEC),
    CHANGE_ENERGY_METER_STATE(ChangeEnergyMeterStatePacket.class, ChangeEnergyMeterStatePacket.STREAM_CODEC);


    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> CEEPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(CreateElecrtoEnergetics.rl(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CreateElecrtoEnergetics.ID, 1);
        for (CEEPackets packet : CEEPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }
}
