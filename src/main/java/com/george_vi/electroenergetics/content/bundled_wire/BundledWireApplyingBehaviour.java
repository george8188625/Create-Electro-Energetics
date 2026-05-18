package com.george_vi.electroenergetics.content.bundled_wire;

import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BundledWireApplyingBehaviour {

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;


        ClientLevel level = mc.level;
        BlockPos pos = result.getBlockPos();
        BlockState state = level.getBlockState(pos);
        LocalPlayer player = mc.player;
        ItemStack heldItemStack = player.getMainHandItem();

        if (!(heldItemStack.getItem() instanceof BundledWireItem heldItem) || state.isAir())
            return;


//        Direction clickedFace = result.getDirection();
//
//        BooleanBooleanPair placement = BundledWireItem.desirableState(result.getLocation().subtract(Vec3.atLowerCornerOf(pos)), clickedFace);
//
//        boolean roll = placement.firstBoolean();
//        boolean flip = placement.secondBoolean();
//
//        int nX = clickedFace.getNormal().getX();
//        int nY = clickedFace.getNormal().getY();
//        int nZ = clickedFace.getNormal().getZ();
//
//        Vec3 lowerCorner1 = new Vec3(
//                pos.getX() + 0.5 + nX / 2f - (nX == 0 ? 0.25 : 0),
//                pos.getY() + 0.5 + nY / 2f - (nY == 0 ? 0.25 : 0),
//                pos.getZ() + 0.5 + nZ / 2f - (nZ == 0 ? 0.25 : 0));
//
//        AABB bb1 = new AABB(
//                lowerCorner1.x,
//                lowerCorner1.y,
//                lowerCorner1.z,
//                lowerCorner1.x + (nX == 0 ? 0.5 : 0),
//                lowerCorner1.y + (nY == 0 ? 0.5 : 0),
//                lowerCorner1.z + (nZ == 0 ? 0.5 : 0)
//        );
//
//
//        Outliner.getInstance().showAABB("cee_bundled_wire_placement_outline", bb1, 2);

    }
}
