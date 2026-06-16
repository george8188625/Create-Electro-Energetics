package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.Node;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.DoubleSupplier;

public class ConnectionEntry {
    public final Vec3 pos1;
    public final Vec3 pos2;
    public final List<Vec3> points;
    public final WireData wireData;
    public final AABB bb;
    public final boolean isCatenary;
    public final List<WireSimulationState.CutWireEntry> cuts;
    public DoubleSupplier resistanceFunction;
    public double resistance = 0;
    public AABB dangerousBB;
    public double dangerousDistanceSqr = 0;
    public boolean isOvervolted = false;

    public ConnectionEntry(Vec3 pos1, Vec3 pos2, List<Vec3> points, WireData wireData, AABB bb, List<WireSimulationState.CutWireEntry> cuts) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.points = points;
        this.wireData = wireData;
        this.bb = bb.inflate(3);
        this.dangerousBB = bb;
        this.cuts = cuts;
        this.isCatenary = false;
        this.resistanceFunction = wireData.wireType()::getResistance;
        this.resistance = resistanceFunction.getAsDouble();
    }

    public ConnectionEntry(Vec3 pos1, Vec3 pos2, List<Vec3> points, WireData wireData, AABB bb,
                           boolean isCatenary, List<WireSimulationState.CutWireEntry> cuts) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.points = points;
        this.wireData = wireData;
        this.bb = bb;
        this.dangerousBB = bb.inflate(3);
        this.isCatenary = isCatenary;
        this.resistanceFunction = wireData.wireType()::getResistance;
        this.cuts = cuts;
        this.resistance = resistanceFunction.getAsDouble();
    }

    public void onReloadConfigs() {
        this.resistance = resistanceFunction.getAsDouble();
    }

    // Cached node IDS. Not guaranteed to be correct
    private int lastID1 = -1;
    private int lastID2 = -1;


    /**
     * Quickly checks for the highest voltage on the wire in relation to ground using the cached node ids
     */
    public double getWireVoltageToGround(SimulationResults results, Node node1, Node node2) {
        // Very messy stuff but hopefully fast

        CircuitBuilder circuitBuilder = results.circuitBuilder;
        List<WrappedIndexedNode> allIndexedNodes = circuitBuilder.allNodes();
        WrappedIndexedNode wn1;
        if (lastID1 >= 0 && lastID1 < allIndexedNodes.size()) {
            wn1 = allIndexedNodes.get(lastID1);
            if (!wn1.node.equals(node1))
                wn1 = circuitBuilder.getNode(node1);
        } else
            wn1 = circuitBuilder.getNode(node1);
        if (wn1 == null)
            return 0;
        lastID1 = wn1.ordinal;

        WrappedIndexedNode wn2;
        if (lastID2 >= 0 && lastID2 < allIndexedNodes.size()) {
            wn2 = allIndexedNodes.get(lastID2);
            if (!wn2.node.equals(node2))
                wn2 = circuitBuilder.getNode(node2);
        } else
            wn2 = circuitBuilder.getNode(node2);
        if (wn2 == null)
            return 0;
        
        lastID2 = wn2.ordinal;

        return Math.max(Math.abs(wn1.rmsVoltage), Math.abs(wn2.rmsVoltage));
    }

    /**
     * Quickly checks for the RMS voltage difference between the wire
     */
    public double getVoltageOnWire(SimulationResults results, Node node1, Node node2) {
        lastID1 = results.getNodeID(node1, lastID1);
        lastID2 = results.getNodeID(node2, lastID2);

        if (lastID1 == -1 || lastID2 == -1)
            return 0;

        return results.getVoltageAt(lastID1, lastID2);
    }
}
