package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.NodeVoltageHolder;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Objects;
import java.util.stream.DoubleStream;

public final class SendVoltageDataPacket implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, SendVoltageDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SendVoltageDataPacket decode(ByteBuf buffer) {
            SendVoltageDataPacket p = new SendVoltageDataPacket();
            p.microTickBits = buffer.readByte();
            if (p.microTickBits > 6 || p.microTickBits < 0)
                throw new DecoderException(p.microTickBits < 0 ? "Received a voltage sync packet with negative micro-tick bits" : "Received a voltage sync packet with too many micro-ticks!");
            int microTicks = (1 << p.microTickBits);
            int length = buffer.readInt();

            p.nodes = new InWorldNode[length];
            p.frequencies = new float[length];
            p.voltages = new double[length * microTicks];

            for (int i = 0; i < length; i++) {
                p.nodes[i] = InWorldNode.STREAM_CODEC.decode(buffer);
                p.frequencies[i] = buffer.readFloat();
                for (int j = 0; j < microTicks; j++)
                    p.voltages[(i << p.microTickBits) | j] = buffer.readDouble();
            }
            return p;
        }

        @Override
        public void encode(ByteBuf buffer, SendVoltageDataPacket p) {
            buffer.writeByte(p.microTickBits);
            buffer.writeInt(p.nodes.length);
            int microTicks = (1 << p.microTickBits);

            for (int i = 0; i < p.nodes.length; i++) {
                InWorldNode.STREAM_CODEC.encode(buffer, p.nodes[i]);
                buffer.writeFloat(p.frequencies[i]);
                for (int j = 0; j < microTicks; j++)
                    buffer.writeDouble(p.voltages[(i << p.microTickBits) | j]);
            }
        }
    };

    InWorldNode[] nodes;

    // a flattened 2d array
    double[] voltages;
    byte microTickBits;

    float[] frequencies;

    @Override
    public void handle(LocalPlayer player) {
        int microTicks = 1 << microTickBits;
        for (int i = 0; i < nodes.length; i++) {
            InWorldNode node = nodes[i];
            NodeVoltageHolder.VoltageEntry ve = NodeVoltageHolder.getVoltageEntry(node);
            if (ve == null || ve == NodeVoltageHolder.VoltageEntry.ZERO) {
                ve = new NodeVoltageHolder.VoltageEntry();
                ve.frequency = frequencies[i];
                ve.voltages = new double[microTicks];
                for (int j = 0; j < microTicks; j++)
                    ve.voltages[j] = voltages[(i << microTickBits) | j];

                ve.recompute();
                NodeVoltageHolder.addVoltageData(node, ve);
            } else {
                double[] list = (ve.voltages != null && ve.voltages.length == microTicks) ? ve.voltages : (ve.voltages = new double[microTicks]);

                for (int j = 0; j < microTicks; j++)
                    list[j] = voltages[(i << microTickBits) | j];

                ve.frequency = frequencies[i];
                ve.recompute();
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_VOLTAGE_DATA;
    }
}
