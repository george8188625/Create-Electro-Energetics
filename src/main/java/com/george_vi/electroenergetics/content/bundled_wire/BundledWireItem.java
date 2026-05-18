package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.CEEDataComponents;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BundledWireItem extends Item {
    public BundledWireItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(CEEDataComponents.SELECTED_FREE_POS);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace().getOpposite();

        return super.useOn(context);
    }



    /**
     * @param position Clicked position x: [0.0 - 1.0], y: [0.0 - 1.0], z: [0.0 - 1.0]. Basically block-position-local
     * @return first: roll second: flip
     */
    public static BooleanBooleanPair desirableState(Vec3 position, Direction clickedFace) {
        double x = position.x();
        double y = position.y();
        double z = position.z();

        boolean roll = false;
        boolean flip = false;

        if (clickedFace.getAxis().isVertical()) {
            if (z > x && z > 1 - x) roll = true;
            else if (z < x && z < 1 - x) roll = flip = true;
            else if (z < x && z > 1 - x) flip = true;
            if (clickedFace == Direction.DOWN && roll)
                flip ^= true;
        } else {
            if (clickedFace.getAxis() == Direction.Axis.X) {
                x = x + z;
                z = x - z;
                x = x - z;
            }
            if (y > x && y > 1 - x) roll = true;
            else if (y < x && y < 1 - x) roll = flip = true;
            else if (y < x && y > 1 - x) flip = true;
            if (clickedFace.getAxisDirection() == Direction.AxisDirection.NEGATIVE && roll)
                flip ^= true;
            if (clickedFace == Direction.NORTH && !roll)
                flip ^= true;
            if (clickedFace == Direction.SOUTH && roll)
                flip ^= true;
            if (clickedFace == Direction.EAST)
                flip ^= true;
        }

        return BooleanBooleanPair.of(roll, flip);
    }
}
