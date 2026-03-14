package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.simulation.simulator.CoupledProperties;
import com.george_vi.electroenergetics.simulation.simulator.ElectricalProperties;

public class TransformerElectricalProperties extends ElectricalProperties implements CoupledProperties {
    final DirectionalNodeConnection primaryNodes;
    final DirectionalNodeConnection secondaryNodes;
    final double ratio;
    final boolean isPrimary;

    private TransformerElectricalProperties(double ratio, DirectionalNodeConnection primaryNodes, DirectionalNodeConnection secondaryNodes, boolean isPrimary) {
        super(1e+8d, 0, 0);
        this.primaryNodes = primaryNodes;
        this.secondaryNodes = secondaryNodes;
        this.ratio = ratio;
        this.isPrimary = isPrimary;
    }

    public TransformerElectricalProperties(double ratio, DirectionalNodeConnection primaryNodes, DirectionalNodeConnection secondaryNodes) {
        this(ratio, primaryNodes, secondaryNodes, true);
    }

    public TransformerElectricalProperties getOtherProperties() {
        return new TransformerElectricalProperties(ratio, secondaryNodes, primaryNodes, false);
    }

    @Override
    public DirectionalNodeConnection nodes() {
        return primaryNodes;
    }

    @Override
    public DirectionalNodeConnection coupledNodes() {
        return secondaryNodes;
    }

    @Override
    public double ratio() {
        return ratio;
    }

    @Override
    public boolean isPrimary() {
        return isPrimary;
    }

    @Override
    public boolean isSimpleResistor() {
        return false;
    }

    @Override
    public ElectricalProperties invert() {
        return this;
    }
}
