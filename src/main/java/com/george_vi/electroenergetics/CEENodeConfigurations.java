package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.foundation.nodes.NodeConfigurator;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class CEENodeConfigurations {

    public static final NodeConfigurator SINGLE_MIDDLE_TOP = new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 14/16f, 0.5f))
            .simple();

    public static final NodeConfigurator SHORT_CONNECTOR = new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .simple();

    public static final NodeConfigurator ELECTRIC_MOTOR = new NodeConfigurator.Builder()
            .add(2, 13, 8)
            .add(14, 13, 8)
            .simple();

    public static final NodeConfigurator ALTERNATOR_BRUSHES = new NodeConfigurator.Builder()
            .add(2, 9, 8)
            .add(14, 9, 8)
            .simple();

    public static final NodeConfigurator DOUBLE_CONNECTOR = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .simple();

    public static final NodeConfigurator ACCUMULATOR_SINGLE = new NodeConfigurator.Builder()
            .add(4, 11, 8) // +
            .add(12, 11, 8) // -
            .simple();

    public static final NodeConfigurator ACCUMULATOR_PARALLEL = new NodeConfigurator.Builder()
            .add(4, 11, 4) // +
            .add(12, 11, 4) // -
            .add(4, 11, 12) // +
            .add(12, 11, 12) // -
            .simple();

    public static final NodeConfigurator ACCUMULATOR_OPPOSITE = new NodeConfigurator.Builder()
            .add(12, 11, 4) // +
            .add(4, 11, 4) // -
            .add(4, 11, 12) // +
            .add(12, 11, 12) // -
            .simple();

    public static final NodeConfigurator DOUBLE_CONNECTOR_MEDIUM = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 8/16f, 0.5f))
            .add(new Vec3(13/16f, 8/16f, 0.5f))
            .simple();

    public static final NodeConfigurator DOUBLE_CONNECTOR_LARGE = new NodeConfigurator.Builder()
            .add(new Vec3(0/16f, 17/16f, 0.5f))
            .add(new Vec3(16/16f, 17/16f, 0.5f))
            .simple();

    public static final NodeConfigurator DOUBLE_CONNECTOR_LARGE_ANGLED = new NodeConfigurator.Builder()
            .add(new Vec3(-2/16f, 16/16f, 0.5f))
            .add(new Vec3(18/16f, 16/16f, 0.5f))
            .simple();

    public static final NodeConfigurator BULB = new NodeConfigurator.Builder()
            .add(new Vec3(2/16f, 3/16f, 0.5f))
            .add(new Vec3(14/16f, 3/16f, 0.5f))
            .simple();

    public static final NodeConfigurator BULB_COMPACT = new NodeConfigurator.Builder()
            .add(new Vec3(6/16f, 3/16f, 0.5f))
            .add(new Vec3(10/16f, 3/16f, 0.5f))
            .simple();

    public static final NodeConfigurator TRIPLE_CONNECTOR = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .simple();

    public static final NodeConfigurator TRIPLE_CONNECTOR_DIAGONAL = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 13/16f))
            .simple();

    public static final NodeConfigurator QUAD_CONNECTOR = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 13/16f))
            .simple();

    public static final NodeConfigurator BI_POLAR_DIRECTIONAL = new NodeConfigurator.Builder()
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

    public static final NodeConfigurator METERING = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 14/16f, 14/16f))
            .add(new Vec3(8/16f, 2/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator TRI_POLAR_METERING = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 14/16f, 14/16f))
            .add(new Vec3(8/16f, 14/16f, 14/16f))
            .add(new Vec3(11/16f, 14/16f, 14/16f))
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(8/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator TRI_POLAR_METERING_MIRRORED = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(8/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .add(new Vec3(5/16f, 14/16f, 14/16f))
            .add(new Vec3(8/16f, 14/16f, 14/16f))
            .add(new Vec3(11/16f, 14/16f, 14/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator CONCRETE_POLE = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 14/16f, 4/16f))
            .add(new Vec3(8/16f, 14/16f, 12/16f))
            .add(new Vec3(4/16f, 14/16f, 8/16f))
            .add(new Vec3(12/16f, 14/16f, 8/16f))
            .simple();

    public static final NodeConfigurator TRANSFORMER = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 14/16f, 13/16f))
            .add(new Vec3(11/16f, 14/16f, 13/16f))
            .add(new Vec3(5/16f, 14/16f, 3/16f))
            .add(new Vec3(11/16f, 14/16f, 3/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator TRANSFORMER_CORE = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 8/16f, 13/16f))
            .add(new Vec3(13/16f, 8/16f, 13/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator HV_CAPACITOR = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 19/16f, 8/16f))
            .add(new Vec3(13/16f, 19/16f, 8/16f))
            .simple();

    public static final NodeConfigurator VOLTAGE_REGULATOR_TOP = new NodeConfigurator.Builder()
            .add(new Vec3(4/16f, 19/16f, 8/16f))
            .add(new Vec3(12/16f, 19/16f, 8/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator VOLTAGE_REGULATOR_BOTTOM = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 3/16f, 3/16f))
            .add(new Vec3(8/16f, 3/16f, 13/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator VOLTAGE_REGULATOR_BOTH = new NodeConfigurator.Builder()
            .add(new Vec3(4/16f, 19/16f, 8/16f))
            .add(new Vec3(12/16f, 19/16f, 8/16f))
            .add(new Vec3(8/16f, 3/16f, 3/16f))
            .add(new Vec3(8/16f, 3/16f, 13/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator REDSTONE_RELAY = new NodeConfigurator.Builder()
            .add(new Vec3(2/16f, 2/16f, 8/16f))
            .add(new Vec3(14/16f, 2/16f, 8/16f))
            .simple();

    public static final NodeConfigurator DOUBLE_SWITCH = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 2/16f, 2/16f))
            .add(new Vec3(11/16f, 2/16f, 2/16f))
            .add(new Vec3(5/16f, 2/16f, 14/16f))
            .add(new Vec3(11/16f, 2/16f, 14/16f))
            .simple();

    public static final NodeConfigurator PUMP = new NodeConfigurator.Builder()
            .add(new Vec3(1/16f, 8/16f, 8/16f))
            .add(new Vec3(15/16f, 8/16f, 8/16f))
            .simple();

    public static final NodeConfigurator HV_SWITCH = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 14/16f, 11/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator ELECTRONIC_8 = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 2/16f, 3/16f))
            .add(new Vec3(8/16f, 2/16f, 13/16f))
            .simple();

    public static final NodeConfigurator ELECTRONIC_10 = new NodeConfigurator.Builder()
            .add(new Vec3(2/16f, 2/16f, 8/16f))
            .add(new Vec3(14/16f, 2/16f, 8/16f))
            .simple(Direction.UP);

    public static final NodeConfigurator ELECTRONIC_4 = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 3/16f, 0.5f))
            .add(new Vec3(11/16f, 3/16f, 0.5f))
            .simple();

    public static final NodeConfigurator RELAY = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 2/16f, 4/16f))
            .add(new Vec3(8/16f, 2/16f, 12/16f))
            .add(new Vec3(2/16f, 2/16f, 8/16f))
            .add(new Vec3(14/16f, 2/16f, 8/16f))
            .simple(Direction.UP);

    public static final NodeConfigurator MOMENTARY_SWITCH = new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 2/16f, 8/16f))
            .add(new Vec3(13/16f, 2/16f, 8/16f))
            .simple(Direction.UP);

    public static final NodeConfigurator INDICATOR_BULB_0 = new NodeConfigurator.Builder()
            .add(new Vec3(4/16f, 2/16f, 5/16f))
            .add(new Vec3(4/16f, 2/16f, 11/16f))
            .simple(Direction.UP);

    public static final NodeConfigurator INDICATOR_BULB_FULL = new NodeConfigurator.Builder()
            .add(new Vec3(4/16f, 2/16f, 5/16f))
            .add(new Vec3(4/16f, 2/16f, 11/16f))
            .add(new Vec3(12/16f, 2/16f, 5/16f))
            .add(new Vec3(12/16f, 2/16f, 11/16f))
            .simple(Direction.UP);

    public static final NodeConfigurator POTENTIOMETER = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 2/16f, 4/16f))
            .add(new Vec3(8/16f, 2/16f, 4/16f))
            .add(new Vec3(11/16f, 2/16f, 4/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator RESISTIVE_HEATER = new NodeConfigurator.Builder()
            .add(new Vec3(5/16f, 4/16f, 2/16f))
            .add(new Vec3(11/16f, 4/16f, 2/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator THREE_PHASE_BRUSH = new NodeConfigurator.Builder()
            .add(8, 7, 2)
            .add(13, 7, 13)
            .add(8, 7, 14)
            .add(3, 7, 13)
            .simple();

    public static final NodeConfigurator CURRENT_TRANSFORMER_BOTH = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 14/16f, 3/16f))
            .add(new Vec3(8/16f, 14/16f, 13/16f))
            .add(new Vec3(5/16f, 4/16f, 3/16f))
            .add(new Vec3(11/16f, 4/16f, 3/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator CURRENT_TRANSFORMER_TOP = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 14/16f, 3/16f))
            .add(new Vec3(8/16f, 14/16f, 13/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator CURRENT_TRANSFORMER_BOTTOM = new NodeConfigurator.Builder()
            .skip(2)
            .add(new Vec3(5/16f, 4/16f, 3/16f))
            .add(new Vec3(11/16f, 4/16f, 3/16f))
            .simple(Direction.NORTH);

    public static final NodeConfigurator RAIL_CONTACT_SHOE = new NodeConfigurator.Builder()
            .add(new Vec3(8/16f, 8/16f, 14/16f))
            .simple(Direction.NORTH);
}
