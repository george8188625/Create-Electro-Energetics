package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import net.minecraft.core.Direction;

public class ElectricalPanelNodeState {
    public static final NodeConfigurator configurator = new NodeConfigurator.Builder()
            .add(3.5f,  13, 14)
            .add(4,     13, 14)
            .add(6.5f,  13, 14)
            .add(8,     13, 14)
            .add(9.5f,  13, 14)
            .add(12,    13, 14)
            .add(12.5f, 13, 14)
            .add(4,     12, 14)
            .add(8,     12, 14)
            .add(12,    12, 14)
            .add(4,     4,  14)
            .add(8,     4,  14)
            .add(12,    4,  14)
            .add(3.5f,  3,  14)
            .add(4,     3,  14)
            .add(6.5f,  3,  14)
            .add(8,     3,  14)
            .add(9.5f,  3,  14)
            .add(12,    3,  14)
            .add(12.5f, 3,  14)
            .add(5,     11, 14)
            .add(11,    11, 14)
            .add(5,     5,  14)
            .add(11,    5,  14)
            .simple(Direction.NORTH);
}
