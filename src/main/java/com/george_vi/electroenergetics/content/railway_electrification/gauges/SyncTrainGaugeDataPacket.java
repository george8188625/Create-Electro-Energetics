package com.george_vi.electroenergetics.content.railway_electrification.gauges;

import com.george_vi.electroenergetics.CEEPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * Packet sent from server to client to sync train voltage and current data for gauge displays.
 * This allows voltmeters and ammeters on train contraptions to display accurate real-time values.
 *
 * @param trainId The UUID of the train
 * @param voltage The train's current voltage in volts
 * @param current The train's total current draw in amps
 */
public record SyncTrainGaugeDataPacket(UUID trainId, double voltage, double current) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, SyncTrainGaugeDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC), SyncTrainGaugeDataPacket::trainId,
            ByteBufCodecs.DOUBLE, SyncTrainGaugeDataPacket::voltage,
            ByteBufCodecs.DOUBLE, SyncTrainGaugeDataPacket::current,
            SyncTrainGaugeDataPacket::new
    );

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ClientTrainGaugeData.update(trainId, voltage, current);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SYNC_TRAIN_GAUGE_DATA;
    }
}

