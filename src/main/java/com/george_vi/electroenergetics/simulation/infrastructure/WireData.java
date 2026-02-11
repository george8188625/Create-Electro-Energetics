package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.simulation.WireType;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Objects;

public class WireData {
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
    private final WireType wireType;
    public float temperature;
    private final List<Pair<Float, WireAttachment>> attachments;

    public WireData(WireType wireType, float temperature, List<Pair<Float, WireAttachment>> attachments) {
        this.wireType = wireType;
        this.temperature = temperature;
        this.attachments = attachments;
    }

    public float getSag() {
        return wireType.getSag();
    }

    public WireType wireType() {
        return wireType;
    }

    public float temperature() {
        return temperature;
    }

    public List<Pair<Float, WireAttachment>> attachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WireData) obj;
        return Objects.equals(this.wireType, that.wireType) &&
                Float.floatToIntBits(this.temperature) == Float.floatToIntBits(that.temperature) &&
                Objects.equals(this.attachments, that.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wireType, temperature, attachments);
    }

    @Override
    public String toString() {
        return "WireData[" +
                "wireType=" + wireType + ", " +
                "temperature=" + temperature + ", " +
                "attachments=" + attachments + ']';
    }

}
