package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import net.minecraft.core.Direction;

public class ElectricalPanelNodeState {
    public static final NodeConfigurator configurator;

    static {
        NodeConfigurator.Builder builder = new NodeConfigurator.Builder()
                .add(3.5f, 13, 14) // 0
                .add(4, 13, 14) // 1
                .add(6.5f, 13, 14) // 2
                .add(8, 13, 14) // 3
                .add(9.5f, 13, 14) // 4
                .add(12, 13, 14) // 5
                .add(12.5f, 13, 14) // 6
                .add(4, 12, 14) // 7
                .add(8, 12, 14) // 8
                .add(12, 12, 14) // 9
                .add(4, 4, 14) // 10
                .add(8, 4, 14) // 11
                .add(12, 4, 14) // 12
                .add(3.5f, 3, 14) // 13
                .add(4, 3, 14) // 14
                .add(6.5f, 3, 14) // 15
                .add(8, 3, 14) // 16
                .add(9.5f, 3, 14) // 17
                .add(12, 3, 14) // 18
                .add(12.5f, 3, 14) // 19
                .add(5, 11, 14) // 20
                .add(11, 11, 14) // 21
                .add(5, 5, 14) // 22
                .add(11, 5, 14); // 23

        // 30 - 325
        // yeah there are unused ids there
        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 16; j++) {
                builder.add(i * 20 + 30 + j, j + 3, i + 1.5f, 14);
            }
        }

        configurator = builder.simple(Direction.NORTH);
    }
}
