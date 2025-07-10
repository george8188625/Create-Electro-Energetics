package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.NodeConfigurator;
import net.minecraft.world.phys.Vec3;

public class CEENodeConfigurations {

    public static final NodeConfigurator DOUBLE_CONNECTOR_ROLL =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 13/16f)).simple();

    public static final NodeConfigurator SINGLE_MIDDLE_TOP =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 14/16f, 0.5f)).simple();

    public static final NodeConfigurator DOUBLE_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f)).simple();

    public static final NodeConfigurator TRIPLE_CONNECTOR_ROLL =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 13/16f)).simple();

    public static final NodeConfigurator TRIPLE_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f)).simple();

    public static final NodeConfigurator QUAD_CONNECTOR =  new NodeConfigurator.Builder()
            .add(new Vec3(3/16f, 5/16f, 0.5f))
            .add(new Vec3(13/16f, 5/16f, 0.5f))
            .add(new Vec3(0.5f, 5/16f, 3/16f))
            .add(new Vec3(0.5f, 5/16f, 13/16f)).simple();

    public static final NodeConfigurator BI_POLAR_DIRECTIONAL =  new NodeConfigurator.Builder()
            .add(new Vec3(0.5f, 0, 0.5f))
            .add(new Vec3(0.5f, 1, 0.5f)).simple();
}
