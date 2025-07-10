package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.CEEShapes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.gauge.GaugeShaper;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class ElectricGaugeShaper extends VoxelShaper {

    private VoxelShaper axisFalse, axisTrue;

    public static ElectricGaugeShaper make(){
        ElectricGaugeShaper shaper = new ElectricGaugeShaper();
        shaper.axisFalse = forDirectional(CEEShapes.ELECTRIC_GAUGE_UP, Direction.UP);
        shaper.axisTrue = forDirectional(rotatedCopy(CEEShapes.ELECTRIC_GAUGE_UP, new Vec3(0, 90, 0)), Direction.UP);
        //shapes for X axis need to be swapped
        Arrays.asList(Direction.EAST, Direction.WEST).forEach(direction -> {
            VoxelShape mem = shaper.axisFalse.get(direction);
            shaper.axisFalse.withShape(shaper.axisTrue.get(direction), direction);
            shaper.axisTrue.withShape(mem, direction);
        });
        return shaper;
    }

    public VoxelShape get(Direction direction, boolean axisAlong) {
        return (axisAlong ? axisTrue : axisFalse).get(direction);
    }
}
