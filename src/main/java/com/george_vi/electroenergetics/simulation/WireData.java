package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record WireData(WireType wireType, float temperature, List<Pair<Float, WireAttachment>> attachments) {
    public static StreamCodec<ByteBuf, WireData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public WireData decode(ByteBuf buffer) {
            int id = buffer.readInt();
            WireType wireType = CEERegistries.WIRE_TYPE.getHolder(id).get().value();
            float temperature = buffer.readFloat();
            return new WireData(wireType, temperature, CatnipStreamCodecBuilders.list(Pair.streamCodec(ByteBufCodecs.FLOAT, WireAttachment.STREAM_CODEC)).decode(buffer));
        }

        @Override
        public void encode(ByteBuf buffer, WireData value) {
            buffer.writeInt(CEERegistries.WIRE_TYPE.getId(value.wireType()));
            buffer.writeFloat(value.temperature());
            CatnipStreamCodecBuilders.list(Pair.streamCodec(ByteBufCodecs.FLOAT, WireAttachment.STREAM_CODEC)).encode(buffer, value.attachments());
        }
    };
}
