package com.george_vi.electroenergetics.simulation.optimization;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;

public record SetVoltageOptimizationEntry(WrappedIndexedNode base, WrappedIndexedNode dead) implements TopologyOptimizationEntry {
}
