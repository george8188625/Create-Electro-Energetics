package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.simulation.WireType;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Objects;

public class WireData {
    public static StreamCodec<ByteBuf, WireData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public WireData decode(ByteBuf buffer) {
            int id = buffer.readInt();
            WireType wireType = CEERegistries.WIRE_TYPE.getHolder(id).get().value();
            float temperature = buffer.readFloat();
            double length = buffer.readDouble();
            return new WireData(wireType, temperature, CatnipStreamCodecBuilders.list(Pair.streamCodec(ByteBufCodecs.FLOAT, WireAttachment.STREAM_CODEC)).decode(buffer), length);
        }

        @Override
        public void encode(ByteBuf buffer, WireData value) {
            buffer.writeInt(CEERegistries.WIRE_TYPE.getId(value.wireType()));
            buffer.writeFloat(value.temperature());
            buffer.writeDouble(value.length);
            CatnipStreamCodecBuilders.list(Pair.streamCodec(ByteBufCodecs.FLOAT, WireAttachment.STREAM_CODEC)).encode(buffer, value.attachments());
        }
    };
    private final WireType wireType;
    public float temperature;
    public double length;
    private final List<Pair<Float, WireAttachment>> attachments;

    public WireData(WireType wireType, float temperature, List<Pair<Float, WireAttachment>> attachments, double length) {
        this.wireType = wireType;
        this.temperature = temperature;
        this.attachments = attachments;
        this.length = length;
    }

    public float getSag() {
        return wireType.getSag();
    }

    public float getSag(double distance) {
        float baseSag = wireType.getSag();

        double factor = length / distance;

        factor *= Mth.lerp(2, 1, factor);
        return (float) (factor * baseSag);
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
                Double.doubleToLongBits(this.length) == Double.doubleToLongBits(that.length) &&
                Objects.equals(this.attachments, that.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wireType, temperature, attachments, length);
    }

    @Override
    public String toString() {
        return "WireData[" +
                "wireType=" + wireType + ", " +
                "temperature=" + temperature + ", " +
                "length=" + length + ", " +
                "attachments=" + attachments + ']';
    }

}
