package com.george_vi.electroenergetics.simulation.optimization;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import com.george_vi.electroenergetics.simulation.electrical_properties.IDissolvedProperties;

public record SimpleTopologyOptimizationEntry(IDissolvedProperties properties, WrappedIndexedNode node1, WrappedIndexedNode node2) implements TopologyOptimizationEntry {
}
