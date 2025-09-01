package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.CEEPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record UpdateElectricTrainSoundPacket(UUID trainUUID, Vec3 pos, float speed, float acceleration, boolean active) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, UpdateElectricTrainSoundPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UpdateElectricTrainSoundPacket::trainUUID,
            CatnipStreamCodecs.VEC3, UpdateElectricTrainSoundPacket::pos,
            ByteBufCodecs.FLOAT, UpdateElectricTrainSoundPacket::speed,
            ByteBufCodecs.FLOAT, UpdateElectricTrainSoundPacket::acceleration,
            ByteBufCodecs.BOOL, UpdateElectricTrainSoundPacket::active,
            UpdateElectricTrainSoundPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        if (!active)
            ElectricTrainSounds.soundProperties.remove(trainUUID);
        else
            ElectricTrainSounds.soundProperties.put(trainUUID, new ElectricTrainSoundEntry(pos, speed, acceleration, active, 3));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.UPDATE_ELECTRIC_TRAIN_SOUND;
    }
}
