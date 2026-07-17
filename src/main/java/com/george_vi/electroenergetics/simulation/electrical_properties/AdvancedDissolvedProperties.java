package com.george_vi.electroenergetics.simulation.electrical_properties;

import com.george_vi.electroenergetics.simulation.SimulationNode;

import java.util.Collection;

/**
 * @see DissolvedProperties
 */
public class AdvancedDissolvedProperties extends MicroTickingElectricalProperties implements IDissolvedProperties {
    private final int[] originalNodeIDs;
    /**
     * Stores the original resistances if it is a simple resistor
     */
    private final double[] originalResistances;
    /**
     * Stores the original electrical properties if it's not a simple resistor
     */
    private final ElectricalProperties[] originalProperties;

    private double resistance;
    private double currentSource;
    /**
     * Only a voltage source if it can not be described in Norton form.
     */
    private double voltageSource;
    private boolean hasNonResistor;

    public AdvancedDissolvedProperties(Collection<SimulationNode> originalNodes, Collection<ElectricalProperties> originalResistances) {
        // fill original nodes
        this.originalNodeIDs = new int[originalNodes.size()];
        int i = 0;
        for (SimulationNode node : originalNodes)
            this.originalNodeIDs[i++] = node.ordinal;

        // fill original properties
        this.originalResistances = new double[originalResistances.size()];
        this.originalProperties = new ElectricalProperties[originalResistances.size()];
        i = 0;
        resistance = 0;
        hasNonResistor = false;
        for (ElectricalProperties properties : originalResistances) {
            this.originalResistances[i] = properties.resistance();
            resistance += properties.resistance();
            if (!properties.isSimpleResistor()) {
                hasNonResistor = true;
                originalProperties[i] = properties;
            }

            i++;
        }

    }

    @Override
    public void tick(double[] allVoltages, int microTick, int totalMicroTicks, int n1, int n2) {
        // Ticks every micro ticker inside of it and updates the equivalent data
        double baseSeriesResistance = 0;
        double baseVoltageSource = 0;
        for (int i = 0; i < originalProperties.length; i++) {
            ElectricalProperties properties = originalProperties[i];
            if (properties == null) {
                baseSeriesResistance += originalResistances[i];
                continue;
            }

            int node1 = originalNodeIDs[i];
            int node2 = originalNodeIDs[i + 1];
            if (properties instanceof MicroTickingElectricalProperties microTicking) {
                microTicking.tick(allVoltages, microTick, totalMicroTicks, node1, node2);
            } else if (properties.invert() instanceof MicroTickingElectricalProperties microTicking) {
                microTicking.tick(allVoltages, microTick, totalMicroTicks, node2, node1);
            }

            // Turns every Norton source into a voltage source with series resistance
            if (properties.isVoltageSource()) {
                // not tested
                baseVoltageSource += properties.voltageSource();
            } else {
                baseSeriesResistance += properties.resistance();
                baseVoltageSource += properties.currentSource() * properties.resistance();
            }
        }

        // Now turn that back to Norton
        if (baseSeriesResistance == 0) {
            // This can't be turned to Norton so it's just a voltage source
            // not tested
            resistance = 1e+11d;
            voltageSource = baseVoltageSource;
            currentSource = 0;
        } else {
            resistance = baseSeriesResistance;
            voltageSource = 0;
            currentSource = baseVoltageSource / baseSeriesResistance;
        }
    }

    @Override
    public void afterTick(double[] allVoltages, int n1, int n2, int microTick, int totalMicroTicks) {
        for (int i = 0; i < originalProperties.length; i++) {
            if (originalProperties[i] == null)
                continue;

            int node1 = originalNodeIDs[i];
            int node2 = originalNodeIDs[i + 1];
            if (originalProperties[i] instanceof MicroTickingElectricalProperties microTicking) {
                microTicking.afterTick(allVoltages, node1, node2, microTick, totalMicroTicks);
            } else if (originalProperties[i].invert() instanceof MicroTickingElectricalProperties microTicking) {
                microTicking.afterTick(allVoltages, node2, node1, microTick, totalMicroTicks);
            }
        }
    }

    @Override
    public void getVoltages(double iv1, double iv2, double[] toFill, int microTick, int totalMicroTicks) {
        double totalResistance = resistance;
        double v1 = toFill[originalNodeIDs[0] * totalMicroTicks + microTick];
        double v2 = toFill[originalNodeIDs[originalNodeIDs.length - 1] * totalMicroTicks + microTick];
        double current = (v1 - v2) / totalResistance;
        current -= currentSource;
        double currentVoltage = v1;
        for (int i = 0; i < originalResistances.length; i++) {
            int nextNodeID = originalNodeIDs[i + 1];

            double resistance = originalResistances[i];
            ElectricalProperties properties = originalProperties[i];
            if (properties == null) {
                double voltageDrop = current * resistance;

                currentVoltage = currentVoltage - voltageDrop;

            } else {
                double propertiesResistance = properties.isVoltageSource() ? 1e+11d : properties.resistance();
                double propertiesVoltage = properties.isVoltageSource() ?
                        properties.voltageSource() :
                        propertiesResistance * properties.currentSource();

                double voltageDrop = current * propertiesResistance;
                currentVoltage -= voltageDrop;
                currentVoltage -= propertiesVoltage;
            }
            toFill[nextNodeID * totalMicroTicks + microTick] = currentVoltage;
        }
    }

    @Override
    public double resistance() {
        return resistance;
    }

    @Override
    public double currentSource() {
        return currentSource;
    }

    @Override
    public boolean isCurrentSource() {
        return currentSource != 0;
    }

    @Override
    public double voltageSource() {
        return voltageSource;
    }

    @Override
    public boolean isVoltageSource() {
        return voltageSource != 0;
    }
}
