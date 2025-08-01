package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.foundation.NodeConfigurator;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class CEENodeConfigurations {

    public static final NodeConfigurator DOUBLE_CONNECTOR_ROLL =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 13/16f))
            .simple();

    public static final NodeConfigurator SINGLE_MIDDLE_TOP =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 14/16f, 0.5f))
            .simple();

    public static final NodeConfigurator SHORT_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .simple();

    public static final NodeConfigurator DOUBLE_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .simple();

    public static final NodeConfigurator TRIPLE_CONNECTOR_ROLL =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 13/16f))
            .simple();

    public static final NodeConfigurator TRIPLE_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .simple();

    public static final NodeConfigurator QUAD_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 13/16f))
            .simple();

    public static final NodeConfigurator BI_POLAR_DIRECTIONAL =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 0, 0.5f))
            .add(new Vec3(0.5f, 1, 0.5f))
            .simple();

    public static final NodeConfigurator BI_POLAR_METERING = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 14/16f, 14/16f))
            .add(new Vec3(11/16f, 14/16f, 14/16f))
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator BI_POLAR_METERING_MIRRORED = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .add(new Vec3(5/16f, 14/16f, 14/16f))
            .add(new Vec3(11/16f, 14/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator TRI_POLAR_METERING =  new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 14/16f, 14/16f))
            .add(new Vec3(8/16f, 14/16f, 14/16f))
            .add(new Vec3(11/16f, 14/16f, 14/16f))
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(8/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator TRI_POLAR_METERING_MIRRORED =  new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(8/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .add(new Vec3(5/16f, 14/16f, 14/16f))
            .add(new Vec3(8/16f, 14/16f, 14/16f))
            .add(new Vec3(11/16f, 14/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator CONCRETE_POLE =  new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 14/16f, 3/16f))
            .add(new Vec3(8/16f, 14/16f, 13/16f))
            .add(new Vec3(3/16f, 14/16f, 8/16f))
            .add(new Vec3(13/16f, 14/16f, 8/16f))
            .simple();

    public static final NodeConfigurator TRANSFORMER =  new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 14/16f, 13/16f))
            .add(new Vec3(11/16f, 14/16f, 13/16f))
            .add(new Vec3(5/16f, 14/16f, 3/16f))
            .add(new Vec3(11/16f, 14/16f, 3/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator REDSTONE_RELAY = new NodeConfigurator.Builder()
            .add(new Vec3(2/16f, 2/16f, 8/16f))
            .add(new Vec3(14/16f, 2/16f, 8/16f))
            .simple();

    public static final NodeConfigurator REDSTONE_RELAY_ROLL = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 2/16f, 2/16f))
            .add(new Vec3(8/16f, 2/16f, 14/16f))
            .simple();

    public static final NodeConfigurator DOUBLE_SWITCH = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 2/16f, 2/16f))
            .add(new Vec3(11/16f, 2/16f, 2/16f))
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .simple();

    public static final NodeConfigurator DOUBLE_SWITCH_ROLL = new NodeConfigurator.Builder()
            .add(new Vec3(2/16f, 2/16f, 5/16f))
            .add(new Vec3(2/16f, 2/16f, 11/16f))
            .add(new Vec3(14/16f, 2/16f, 5/16f))
            .add(new Vec3(14/16f, 2/16f, 11/16f))
            .simple();

    public static final NodeConfigurator PUMP = new NodeConfigurator.Builder()
            .add(new Vec3(1/16f, 8/16f, 8/16f))
            .add(new Vec3(15/16f, 8/16f, 8/16f))
            .simple();

    public static final NodeConfigurator PUMP_ROLL = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 8/16f, 1/16f))
            .add(new Vec3(8/16f, 8/16f, 15/16f))
            .simple();
}
