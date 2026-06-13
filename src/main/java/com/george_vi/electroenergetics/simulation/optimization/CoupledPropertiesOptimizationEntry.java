package com.george_vi.electroenergetics.simulation.optimization;

public record CoupledPropertiesOptimizationEntry(int leftNode, int node,
                                                 int rightNode, int leftPrimary,
                                                 int rightPrimary, double replacementResistance,
                                                 double leftResistance, double rightResistance, double ratio) implements TopologyOptimizationEntry {
}
