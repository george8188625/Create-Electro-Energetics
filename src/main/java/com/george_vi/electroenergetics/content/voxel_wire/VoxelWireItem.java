package com.george_vi.electroenergetics.content.voxel_wire;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VoxelWireItem extends Item {
    public VoxelWireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemInHand = player.getItemInHand(usedHand);

        if (player.isShiftKeyDown()) {
            if (itemInHand.has(CEEDataComponents.SELECTED_VOXEL_NODE_PLACEMENT)) {
                itemInHand.remove(CEEDataComponents.SELECTED_VOXEL_NODE_PLACEMENT);
                if (level.isClientSide)
                    player.displayClientMessage(CEELang.translateDirect("wire_spool.cancelled_connection"), true);
            }
            return InteractionResultHolder.success(itemInHand);
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Vec3 clickLocation = context.getClickLocation();
        Direction clickedFace = context.getClickedFace();
        ItemStack itemInHand = context.getItemInHand();
        Level level = context.getLevel();
        if (player == null)
            return super.useOn(context);

        if (player.isShiftKeyDown()) {
            if (itemInHand.has(CEEDataComponents.SELECTED_VOXEL_NODE_PLACEMENT)) {
                itemInHand.remove(CEEDataComponents.SELECTED_VOXEL_NODE_PLACEMENT);
                if (level.isClientSide)
                    player.displayClientMessage(CEELang.translateDirect("wire_spool.cancelled_connection"), true);
            }
            return InteractionResult.SUCCESS;
        }

        VoxelWireNodePlacement placement = VoxelWireNodePlacement.get(pos, clickedFace, clickLocation);
        VoxelWireNodePlacement selectedPlacement = itemInHand.get(CEEDataComponents.SELECTED_VOXEL_NODE_PLACEMENT);

        if (placement.equals(selectedPlacement)) {
            AllSoundEvents.DENY.playOnServer(level, pos);
            return InteractionResult.SUCCESS;
        }
        itemInHand.set(CEEDataComponents.SELECTED_VOXEL_NODE_PLACEMENT, placement);

        return InteractionResult.SUCCESS;
    }
}
