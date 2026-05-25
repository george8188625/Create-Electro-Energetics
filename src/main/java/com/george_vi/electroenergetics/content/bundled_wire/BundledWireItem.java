package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.WireType;
import com.simibubi.create.AllSoundEvents;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BundledWireItem extends Item {
    public final BundledWireType wireType;

    public BundledWireItem(Properties properties, BundledWireType wireType) {
        super(properties);
        this.wireType = wireType;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(CEEDataComponents.SELECTED_WIRE_TERMINATION);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace().getOpposite();
        BlockState state = level.getBlockState(pos);
        ItemStack heldItemStack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (heldItemStack.getComponents().has(CEEDataComponents.SELECTED_WIRE_TERMINATION)) {
                heldItemStack.remove(CEEDataComponents.SELECTED_WIRE_TERMINATION);
                if (level.isClientSide)
                    player.displayClientMessage(CEELang.translateDirect("wire_spool.cancelled_connection"), true);
            }
            return InteractionResult.SUCCESS;
        }

        BundledWireType selectedType = null;

        boolean selectedRoll;
        boolean selectedFlip;
        if (state.getBlock() instanceof BundledWireTerminationBlock block) {
            selectedRoll = state.getValue(BundledWireTerminationBlock.ROLL);
            selectedFlip = state.getValue(BundledWireTerminationBlock.FLIP);
            selectedType = block.wireType;
        } else
            return InteractionResult.PASS;

        if (selectedType != null && selectedType != wireType) {
            player.displayClientMessage(CEELang.translateDirect("bundled_wire_spool.incompatible_types"), true);
            AllSoundEvents.DENY.playOnServer(level, pos);
            return InteractionResult.FAIL;
        }

        BundledWireTerminationState selectedTermination = heldItemStack.get(CEEDataComponents.SELECTED_WIRE_TERMINATION);

        if (selectedTermination == null) {
            heldItemStack.set(CEEDataComponents.SELECTED_WIRE_TERMINATION,
                    new BundledWireTerminationState(pos, face, selectedRoll, selectedFlip));
            AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos);
            return InteractionResult.SUCCESS;
        }


        InWorldNode selectedNode = BundledWireNodeConfigurator.getCableNodeFor(selectedTermination.pos(), selectedTermination.roll(), selectedTermination.flip());
        InWorldNode hoveredNode = BundledWireNodeConfigurator.getCableNodeFor(pos, selectedRoll, selectedFlip);

        WireType decorativeWireType = CEEWireTypes.DUPLEX.get();
        WireType conductorWireType = CEEWireTypes.BUNDLE_CONDUCTOR.get();



        return InteractionResult.SUCCESS;
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
