package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ChangeTrainSoundTypePacket(UUID uuid, int soundId) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ChangeTrainSoundTypePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ChangeTrainSoundTypePacket::uuid,
            ByteBufCodecs.INT, ChangeTrainSoundTypePacket::soundId,
            ChangeTrainSoundTypePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        Train train = Create.RAILWAYS.trains.get(uuid);
        if (train == null)
            return;
        if (!(train instanceof ICEETrainExtension trainExtension))
            return;
        var optionalSoundType = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getHolder(soundId);
        if (optionalSoundType.isEmpty())
            return;
        trainExtension.setSoundType(optionalSoundType.get().value());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CHANGE_TRAIN_SOUND_TYPE;
    }
}
