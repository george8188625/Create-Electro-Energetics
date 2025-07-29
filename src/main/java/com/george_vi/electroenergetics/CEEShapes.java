package com.george_vi.electroenergetics;

import com.simibubi.create.AllShapes;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CEEShapes {
    public static final VoxelShaper CONNECTOR = shape(6,0, 6,10,10,10).forDirectional();

    public static final VoxelShaper CONNECTOR_SHORT = shape(6,0, 6,10,7,10).forDirectional();

    public static final VoxelShaper QUAD_CONNECTOR = shape(6,0, 1,10,7,15)
            .add(1, 0, 6, 15, 7, 10).forDirectional();

    public static final VoxelShaper ALTERNATOR_BRUSHES = shape(2,0, 2,14,11,14)
            .add(AllShapes.SIX_VOXEL_POLE.get(Direction.Axis.Y)).forDirectional();

    public static final VoxelShaper GROUND_ROD = shape(6,0, 6,10,5,10).forDirectional();

    public static final VoxelShaper TRANSFORMER = shape(1,0, 0,15,14,16).forHorizontal(Direction.NORTH);

    public static final VoxelShaper VOLTAGE_REGULATOR = shape(3,0, 1,13,16,15).forHorizontal(Direction.NORTH);

    public static final VoxelShape ELECTRIC_GAUGE_UP = shape(1,0, 0,15,2,16)
            .add(2,2, 0,14,14,16).build();

    public static final VoxelShaper BULB = shape(4,0, 4,12,11,12)
            .add(0,0, 6,16,5,10).forDirectional();

    public static final VoxelShaper BULB_ROLL = shape(4,0, 4,12,11,12)
            .add(6,0, 0,10,5,16).forDirectional();

    public static final VoxelShaper DOUBLE_CONNECTOR = shape(1,0, 6,15,7,10).forDirectional();

    public static final VoxelShaper DOUBLE_CONNECTOR_ROLL = shape(6,0, 1,10,7,15).forDirectional();

    public static final VoxelShaper CUT_OFF_SWITCH = shape(4,0, 4,12,5,12)
            .add(0,0, 6,16,5,10).forDirectional();

    public static final VoxelShaper CUT_OFF_SWITCH_ROLL = shape(4,0, 4,12,5,12)
            .add(6,0, 0,10,5,16).forDirectional();

    public static final VoxelShaper DOUBLE_SWITCH = shape(2,0, 2,14,5,14).forDirectional();

    public static final VoxelShaper REDSTONE_RELAY = shape(2,0, 2,14,4,14).forDirectional();

    public static final VoxelShaper ENERGY_METER = shape(2,2, 9,14,14,16).forHorizontal(Direction.NORTH);

    public static final VoxelShaper ELECTRIC_MOTOR = shape(0,5, 0,16,15,16)
            .add(2,3, 2,14,5,14)
            .add(AllShapes.SIX_VOXEL_POLE.get(Direction.Axis.Y))
            .forDirectional();

    public static final VoxelShaper CONVERTER = shape(0, 0, 0, 16, 5, 16).forDirectional();

    public static final VoxelShaper POLE_MOUNT = shape(5, 13, 5, 11, 16, 11)
            .add(7, 5, 7, 9, 15, 22).forHorizontal(Direction.SOUTH);

    public static final VoxelShaper POLE_MOUNT_INVERTED = shape(5, 0, 5, 11, 3, 11)
            .add(7, 1, 7, 9, 11, 22).forHorizontal(Direction.SOUTH);

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    private static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AllShapes.Builder(cuboid(x1, y1, z1, x2, y2, z2));
    }
}
