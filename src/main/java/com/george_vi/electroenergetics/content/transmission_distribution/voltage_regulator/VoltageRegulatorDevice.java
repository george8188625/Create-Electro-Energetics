package com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator;

import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerElectricalProperties;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VoltageRegulatorDevice extends SimulatedDevice<VoltageRegulatorDevice.DataHolder> {
    private static final double INPUT_RESISTANCE = 0.01;
    private static final double OUTPUT_RESISTANCE = 0.01;
    private static final double VOLTAGE_TOLERANCE = 5.0;
    private static final double RATIO_PER_STEP = 0.01;
    private static final int MAX_STEPS = 32;

    private static final int NODE_INPUT = 0;
    private static final int NODE_OUTPUT = 1;
    private static final int NODE_GROUND = 0;
    private static final int NODE_GROUND_SINGLE = 2;
    private static final int NODE_INPUT_DIV = 6;
    private static final int NODE_OUTPUT_DIV = 7;

    /*
    The bottom is always controller

    This is how it works electrically:
              _______
    INPUT --- |  R  | --- INPUT_DIV
      |       """""""            |
      ------------------------   |
                             |   (
                             (   )
        TRANSFORMER here --> )   (
                             (   )
            _______          |   (
    OUTPUT -|  R  |- OUTPUT_DIV  |
            """""""              |
                              GROUND
    */

    public VoltageRegulatorDevice(ResourceLocation id) {
        super(id);
    }

    @Override
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, DataHolder extraData) {
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (extraData.top && extraData.bottom)
            builder.resistor(extraData.sliced == 0 ? 2 : 0, extraData.sliced == 0 ? 3 : 1, CEEConfigs.server().resistanceValues.wireResistance.get());
        else if (extraData.bottom)
            builder.resistor(0, 1, CEEConfigs.server().resistanceValues.wireResistance.get());

        if (!extraData.bottom)
            return;

        extraData.poleLength = findPoleLength(pos, level, bridges.getSD());
        if (extraData.poleLength < 0)
            return;

        DataHolder dh = getVoltageRegulatorData(bridges.getSD(), pos.above(extraData.poleLength));
        if (dh == null)
            return;

        NodeLayout nodes = resolveNodes(pos, extraData.poleLength, dh.sliced);

        int prevSteps = extraData.steps;
        double ratio = updateStepsAndGetRatio(extraData);
        if (prevSteps != extraData.steps) { // play sounds
            if (extraData.be != null) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                level.playSound(null, pPos.x, pPos.y, pPos.z, CEESoundEvents.VOLTAGE_REGULATOR.get(), SoundSource.BLOCKS, 0.5f, 1f);
            }
        }

        TransformerElectricalProperties ep = new TransformerElectricalProperties(ratio,
                new DirectionalNodeConnection(nodes.input, nodes.outputDiv),
                new DirectionalNodeConnection(nodes.ground, nodes.inputDiv));

        builder.node(nodes.inputDiv)
                .node(nodes.outputDiv)
                .resistor(nodes.input, nodes.inputDiv,  INPUT_RESISTANCE)
                .resistor(nodes.output, nodes.outputDiv, OUTPUT_RESISTANCE)
                .connect(nodes.inputDiv, nodes.ground, ep.getOtherProperties())
                .connect(nodes.input, nodes.outputDiv, ep);


    }

    @Override
    public void postTick(BlockPos pos, Level level, SimulationResults results, DataHolder extraData) {
        if (!extraData.bottom) {
            extraData.be = null;
            return;
        }

        DataHolder topData = extraData.top ? extraData : getVoltageRegulatorData(results.getSD(), pos.above(extraData.poleLength));
        if (topData == null)
            return;

        NodeLayout nodes = resolveNodes(pos, extraData.poleLength, topData.sliced);

        double current;
        double inputVoltage;

        extraData.lastVoltage = results.getVoltageAt(nodes.output, nodes.ground);
        inputVoltage = results.getVoltageAt(nodes.input, nodes.ground);
        current = results.getCurrentThrough(nodes.outputDiv, nodes.output);

        if (extraData.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
                extraData.be = be;

        if (extraData.be != null) {
            if (extraData.be.isRemoved())
                extraData.be = null;
            else {
                extraData.be.power = current * extraData.lastVoltage;
                extraData.be.inputVoltage = inputVoltage;
                extraData.be.outputVoltage = extraData.lastVoltage;
            }
        }
    }

    private double updateStepsAndGetRatio(DataHolder data) {
        if (data.stepTimeout > 0) {
            data.stepTimeout--;
            return data.steps * RATIO_PER_STEP;
        }
        if (Math.abs(data.lastVoltage) < 1)
            return data.steps * RATIO_PER_STEP;
        double diff = data.targetVoltage - Math.abs(data.lastVoltage);
        double tol = RATIO_PER_STEP * data.targetVoltage * 0.7;
        int prevSteps = data.steps;
        if (diff > tol)
            data.steps = Mth.clamp(data.steps + 1, -MAX_STEPS, MAX_STEPS);
        else if (diff < -tol)
            data.steps = Mth.clamp(data.steps - 1, -MAX_STEPS, MAX_STEPS);
        if (prevSteps != data.steps)
            data.stepTimeout = 2;
        return data.steps * RATIO_PER_STEP;
    }

    private DataHolder getVoltageRegulatorData(InfrastructureSavedData sd, BlockPos pos) {
        SimulatedDeviceInstance<?> di = sd.getDevice(pos);
        if (di == null || di.simulatedDevice() != CEESimulatedDevices.VOLTAGE_REGULATOR) return null;
        return di.extraData() instanceof DataHolder dh ? dh : null;
    }

    private NodeLayout resolveNodes(BlockPos bottomPos, int poleLength, int sliced) {
        if (poleLength < 0)
            poleLength = 0;

        InWorldNode inputDiv  = new InWorldNode(NODE_INPUT_DIV,  bottomPos);
        InWorldNode outputDiv = new InWorldNode(NODE_OUTPUT_DIV, bottomPos);

        BlockPos topPos = bottomPos.above(sliced == 0 ? poleLength : poleLength + 1);
        if (poleLength > 0) {
            return new NodeLayout(
                    new InWorldNode(sliced == 2 ? NODE_OUTPUT : NODE_INPUT, topPos),
                    new InWorldNode(sliced == 2 ? NODE_INPUT : NODE_OUTPUT, topPos),
                    new InWorldNode(NODE_GROUND, bottomPos),
                    inputDiv, outputDiv
            );
        } else {
            return new NodeLayout(
                    new InWorldNode(sliced == 2 ? NODE_OUTPUT : NODE_INPUT, topPos),
                    new InWorldNode(sliced == 2 ? NODE_INPUT : NODE_OUTPUT, topPos),
                    new InWorldNode(sliced == 0 ? NODE_GROUND_SINGLE : NODE_GROUND, bottomPos),
                    inputDiv, outputDiv
            );
        }
    }

    private int findPoleLength(BlockPos pos, Level level, InfrastructureSavedData sd) {
        for (int i = 1; i + pos.getY() < level.getMaxBuildHeight(); i += 2) {
            DataHolder dh = getVoltageRegulatorData(sd, pos.above(i));

            if (dh == null) {
                // Overshot - step back and check if previous block was a valid top
                int candidate = i - 1;
                DataHolder top = getVoltageRegulatorData(sd, pos.above(candidate));
                return (top != null && top.top) ? candidate : -1;
            }

            if (dh.top) return i;
        }
        return -1;
    }

    @Override
    public DataHolder read(CompoundTag tag) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.steps = tag.getInt("Steps");
        dataHolder.temp = tag.getFloat("Temp");
        dataHolder.targetVoltage = tag.getDouble("Voltage");
        dataHolder.lastVoltage = tag.getDouble("LastVoltage");
        dataHolder.top = tag.getBoolean("Top");
        dataHolder.bottom = tag.getBoolean("Bottom");
        dataHolder.poleLength = tag.getInt("PoleLength");
        dataHolder.sliced = tag.getInt("Sliced");
        return dataHolder;
    }

    @Override
    public CompoundTag write(DataHolder extraData) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Steps", extraData.steps);
        tag.putInt("PoleLength", extraData.poleLength);
        tag.putFloat("Temp", extraData.temp);
        tag.putDouble("Voltage", extraData.targetVoltage);
        tag.putDouble("LastVoltage", extraData.lastVoltage);
        if (extraData.bottom)
            tag.putBoolean("Bottom", true);
        if (extraData.top)
            tag.putBoolean("Top", true);
        if (extraData.sliced != 0)
            tag.putInt("Sliced", extraData.sliced);
        return tag;
    }

    public static class DataHolder {
        public float temp;
        public VoltageRegulatorBlockEntity be;
        public int steps;
        public double targetVoltage;
        public double lastVoltage;
        public boolean bottom;
        public boolean top;
        public int poleLength;
        public int stepTimeout = 0;
        public int sliced;
    }

    private record NodeLayout(InWorldNode input, InWorldNode output, InWorldNode ground,
                              InWorldNode inputDiv, InWorldNode outputDiv) {}
}
