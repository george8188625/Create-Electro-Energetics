package com.george_vi.electroenergetics.content.transmission_distribution.current_transformer;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerElectricalProperties;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CurrentTransformerDevice extends SimulatedDevice<CurrentTransformerDevice.DataHolder> {
    private static final double INPUT_RESISTANCE = 0.01;
    private static final double OUTPUT_RESISTANCE = 0.01;

    private static final int NODE_PRIMARY_INPUT = 0;
    private static final int NODE_PRIMARY_OUTPUT = 1;
    private static final int NODE_SECONDARY_INPUT = 2;
    private static final int NODE_SECONDARY_OUTPUT = 3;
    private static final int NODE_PRIMARY_DIV = 6;
    private static final int NODE_SECONDARY_DIV = 7;

    public CurrentTransformerDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (!extraData.bottom)
            return;

        extraData.poleLength = findPoleLength(pos, level, bridges.getSD());
        if (extraData.poleLength < 0)
            return;

        DataHolder dh = getCurrentTransformerData(bridges.getSD(), pos.above(extraData.poleLength));
        if (dh == null)
            return;

        NodeLayout nodes = resolveNodes(pos, extraData.poleLength);

        double ratio = dh.ratio;

        TransformerElectricalProperties ep = new TransformerElectricalProperties(ratio,
                new DirectionalNodeConnection(nodes.primaryIn, nodes.primaryDiv),
                new DirectionalNodeConnection(nodes.secondaryIn, nodes.secondaryDiv));

        builder.node(nodes.primaryDiv)
                .node(nodes.secondaryDiv)
                .resistor(nodes.primaryOut, nodes.primaryDiv,  INPUT_RESISTANCE)
                .resistor(nodes.secondaryOut, nodes.secondaryDiv, OUTPUT_RESISTANCE)
                .connect(nodes.secondaryIn, nodes.secondaryDiv, ep.getOtherProperties())
                .connect(nodes.primaryIn, nodes.primaryDiv, ep);
    }

    private DataHolder getCurrentTransformerData(InfrastructureSavedData sd, BlockPos pos) {
        SimulatedDeviceInstance<?> di = sd.getDevice(pos);
        if (di == null || di.simulatedDevice() != CEESimulatedDevices.CURRENT_TRANSFORMER) return null;
        return di.extraData() instanceof DataHolder dh ? dh : null;
    }

    private NodeLayout resolveNodes(BlockPos bottomPos, int poleLength) {
        if (poleLength < 0)
            poleLength = 0;

        InWorldNode primaryDiv  = new InWorldNode(NODE_PRIMARY_DIV,  bottomPos);
        InWorldNode secondaryDiv = new InWorldNode(NODE_SECONDARY_DIV, bottomPos);

        BlockPos topPos = bottomPos.above(poleLength);
        if (poleLength > 0) {
            return new NodeLayout(
                    new InWorldNode(NODE_PRIMARY_INPUT, topPos),
                    new InWorldNode(NODE_PRIMARY_OUTPUT, topPos),
                    new InWorldNode(NODE_SECONDARY_INPUT, bottomPos),
                    new InWorldNode(NODE_SECONDARY_OUTPUT, bottomPos),
                    primaryDiv, secondaryDiv
            );
        } else {
            return new NodeLayout(
                    new InWorldNode(NODE_PRIMARY_INPUT, topPos),
                    new InWorldNode(NODE_PRIMARY_OUTPUT, topPos),
                    new InWorldNode(NODE_SECONDARY_INPUT, bottomPos),
                    new InWorldNode(NODE_SECONDARY_OUTPUT, bottomPos),
                    primaryDiv, secondaryDiv
            );
        }
    }

    private int findPoleLength(BlockPos pos, Level level, InfrastructureSavedData sd) {
        for (int i = 1; i + pos.getY() < level.getMaxBuildHeight(); i += 2) {
            DataHolder dh = getCurrentTransformerData(sd, pos.above(i));

            if (dh == null) {
                // Overshot - step back and check if previous block was a valid top
                int candidate = i - 1;
                DataHolder top = getCurrentTransformerData(sd, pos.above(candidate));
                return (top != null && top.top) ? candidate : -1;
            }

            if (dh.top) return i;
        }
        return -1;
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.top = tag.getBoolean("Top");
        dataHolder.bottom = tag.getBoolean("Bottom");
        dataHolder.poleLength = tag.getInt("PoleLength");
        dataHolder.ratio = tag.getDouble("Ratio");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("PoleLength", extraData.poleLength);
        tag.putFloat("Temp", extraData.temp);
        tag.putDouble("Ratio", extraData.ratio);
        if (extraData.bottom)
            tag.putBoolean("Bottom", true);
        if (extraData.top)
            tag.putBoolean("Top", true);
        return tag;
    }

    public static class DataHolder {
        public float temp;
        public CurrentTransformerBlockEntity be;
        public boolean bottom;
        public boolean top;
        public int poleLength;
        public double ratio;
    }

    private record NodeLayout(InWorldNode primaryIn, InWorldNode primaryOut,
                              InWorldNode secondaryIn, InWorldNode secondaryOut,
                              InWorldNode primaryDiv, InWorldNode secondaryDiv) {}
}
