package com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerElectricalProperties;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VoltageRegulatorDevice extends SimpleElectricalDevice {
    private static final double INPUT_RESISTANCE = 0.01;
    private static final double OUTPUT_RESISTANCE = 0.01;
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

    public VoltageRegulatorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        BridgeCollector.Builder builder = bridges.builder(pos);

        if (this.top && this.bottom)
            builder.resistor(this.sliced == 0 ? 2 : 0, this.sliced == 0 ? 3 : 1, CEEConfigs.server().resistanceValues.wireResistance.get());
        else if (this.bottom)
            builder.resistor(0, 1, CEEConfigs.server().resistanceValues.wireResistance.get());

        if (!this.bottom)
            return;

        this.poleLength = findPoleLength(pos, level, bridges.getSD());
        if (this.poleLength < 0)
            return;

        VoltageRegulatorDevice topDevice = this.top ? this : deviceSD.getDevice(pos.above(this.poleLength), VoltageRegulatorDevice.class);
        if (topDevice == null)
            return;

        NodeLayout nodes = resolveNodes(pos, this.poleLength, topDevice.sliced);

        int prevSteps = this.steps;
        boolean shouldPlaySound = stepTimeout == 0;
        double ratio = updateStepsAndGetRatio();
        if (prevSteps != this.steps) { // play sounds
            if (this.be != null) {
                Vec3 pPos = Vec3.atCenterOf(pos);
                if (shouldPlaySound)
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
    public void postTick(SimulationResults results) {
        if (!this.bottom) {
            this.be = null;
            return;
        }

        VoltageRegulatorDevice topDevice = this.top ? this : deviceSD.getDevice(pos.above(this.poleLength), VoltageRegulatorDevice.class);
        if (topDevice == null)
            return;

        NodeLayout nodes = resolveNodes(pos, this.poleLength, topDevice.sliced);

        double current;
        double inputVoltage;

        this.lastVoltage = results.getVoltageAt(nodes.output, nodes.ground);
        inputVoltage = results.getVoltageAt(nodes.input, nodes.ground);
        current = results.getCurrentThrough(nodes.outputDiv, nodes.output);

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof VoltageRegulatorBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.power = current * this.lastVoltage;
                this.be.inputVoltage = inputVoltage;
                this.be.outputVoltage = this.lastVoltage;
            }
        }
    }

    private double updateStepsAndGetRatio() {
        if (this.stepTimeout > 0) {
            this.stepTimeout--;
        }

        if (Math.abs(this.lastVoltage) < 1 || stepTimeout % 2 == 1)
            return this.steps * RATIO_PER_STEP;
        double diff = this.targetVoltage - Math.abs(this.lastVoltage);
        double tol = RATIO_PER_STEP * this.targetVoltage * 0.7;
        int prevSteps = this.steps;
        if (diff > tol)
            this.steps = Mth.clamp(this.steps + 1, -MAX_STEPS, MAX_STEPS);
        else if (diff < -tol)
            this.steps = Mth.clamp(this.steps - 1, -MAX_STEPS, MAX_STEPS);
        if (prevSteps != this.steps)
            this.stepTimeout = 2;
        return this.steps * RATIO_PER_STEP;
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
            VoltageRegulatorDevice otherRegulator = deviceSD.getDevice(pos.above(i), VoltageRegulatorDevice.class);

            if (otherRegulator == null) {
                VoltageRegulatorDevice top = deviceSD.getDevice(pos.above(i - 1), VoltageRegulatorDevice.class);
                return (top != null && top.top) ? i - 1 : -1;
            }

            if (otherRegulator.top)
                return i;
        }
        return -1;
    }

    @Override
    public void read(CompoundTag tag) {
        this.steps = tag.getInt("Steps");
        this.temp = tag.getFloat("Temp");
        this.targetVoltage = tag.getDouble("Voltage");
        this.lastVoltage = tag.getDouble("LastVoltage");
        this.top = tag.getBoolean("Top");
        this.bottom = tag.getBoolean("Bottom");
        this.poleLength = tag.getInt("PoleLength");
        this.sliced = tag.getInt("Sliced");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("Steps", this.steps);
        tag.putInt("PoleLength", this.poleLength);
        tag.putFloat("Temp", this.temp);
        tag.putDouble("Voltage", this.targetVoltage);
        tag.putDouble("LastVoltage", this.lastVoltage);
        if (this.bottom)
            tag.putBoolean("Bottom", true);
        if (this.top)
            tag.putBoolean("Top", true);
        if (this.sliced != 0)
            tag.putInt("Sliced", this.sliced);
    }


    private record NodeLayout(InWorldNode input, InWorldNode output, InWorldNode ground,
                              InWorldNode inputDiv, InWorldNode outputDiv) {}
}

