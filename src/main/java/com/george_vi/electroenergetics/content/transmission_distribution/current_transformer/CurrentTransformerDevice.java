package com.george_vi.electroenergetics.content.transmission_distribution.current_transformer;

import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerElectricalProperties;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class CurrentTransformerDevice extends SimpleElectricalDevice {
    private static final double INPUT_RESISTANCE = 0.01;
    private static final double OUTPUT_RESISTANCE = 0.01;

    private static final int NODE_PRIMARY_INPUT = 0;
    private static final int NODE_PRIMARY_OUTPUT = 1;
    private static final int NODE_SECONDARY_INPUT = 2;
    private static final int NODE_SECONDARY_OUTPUT = 3;
    private static final int NODE_PRIMARY_DIV = 6;
    private static final int NODE_SECONDARY_DIV = 7;

    public float temp;
    public CurrentTransformerBlockEntity be;
    public boolean bottom;
    public boolean top;
    public int poleLength;
    public double ratio;

    public CurrentTransformerDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }


    @Override
    public void preTick(BridgeCollector bridges) {
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (!bottom)
            return;

        poleLength = findPoleLength(pos, level);
        if (poleLength < 0)
            return;

        CurrentTransformerDevice dh = deviceSD.getDevice(pos.above(poleLength), CurrentTransformerDevice.class);
        if (dh == null)
            return;

        NodeLayout nodes = resolveNodes(pos, poleLength);

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

    private int findPoleLength(BlockPos pos, Level level) {
        for (int i = 1; i + pos.getY() < level.getMaxBuildHeight(); i += 2) {
            CurrentTransformerDevice dh = deviceSD.getDevice(pos.above(i), CurrentTransformerDevice.class);

            if (dh == null) {
                CurrentTransformerDevice top = deviceSD.getDevice(pos.above(i - 1), CurrentTransformerDevice.class);
                return (top != null && top.top) ? i - 1 : -1;
            }

            if (dh.top)
                return i;
        }
        return -1;
    }

    @Override
    public void read(CompoundTag tag) {
        temp = tag.getFloat("Temp");
        top = tag.getBoolean("Top");
        bottom = tag.getBoolean("Bottom");
        poleLength = tag.getInt("PoleLength");
        ratio = tag.getDouble("Ratio");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("PoleLength", poleLength);
        tag.putFloat("Temp", temp);
        tag.putDouble("Ratio", ratio);
        if (bottom)
            tag.putBoolean("Bottom", true);
        if (top)
            tag.putBoolean("Top", true);
    }


    private record NodeLayout(InWorldNode primaryIn, InWorldNode primaryOut,
                              InWorldNode secondaryIn, InWorldNode secondaryOut,
                              InWorldNode primaryDiv, InWorldNode secondaryDiv) {}
}
