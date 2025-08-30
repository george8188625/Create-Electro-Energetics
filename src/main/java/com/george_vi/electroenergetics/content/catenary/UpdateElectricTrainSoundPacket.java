package com.george_vi.electroenergetics.content.catenary;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.content.wire.SendWireConnectionsPacket;
import com.george_vi.electroenergetics.simulation.WireData;
import com.george_vi.electroenergetics.simulation.simulator.DirectionSensitiveNodeConnection;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record UpdateElectricTrainSoundPacket(UUID trainUUID, Vec3 pos, float volume, float pitch) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, UpdateElectricTrainSoundPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UpdateElectricTrainSoundPacket::trainUUID,
            CatnipStreamCodecs.VEC3, UpdateElectricTrainSoundPacket::pos,
            ByteBufCodecs.FLOAT, UpdateElectricTrainSoundPacket::volume,
            ByteBufCodecs.FLOAT, UpdateElectricTrainSoundPacket::pitch,
            UpdateElectricTrainSoundPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        if (volume == 0 || pitch == 0)
            ElectricTrainSounds.soundProperties.remove(trainUUID);
        else
            ElectricTrainSounds.soundProperties.put(trainUUID, Pair.of(pos, Couple.create(volume, pitch)));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.UPDATE_ELECTRIC_TRAIN_SOUND;
    }
}
