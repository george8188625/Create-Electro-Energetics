package com.george_vi.electroenergetics.content.bundled_wire;

import com.simibubi.create.AllSpecialTextures;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
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

import java.util.Map;

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


        Direction clickedFace = result.getDirection();

        BooleanBooleanPair placement = BundledWireItem.desirableState(result.getLocation().subtract(Vec3.atLowerCornerOf(pos)), clickedFace);

        BlockState hoveredBlockState = level.getBlockState(pos);

        if (hoveredBlockState.getBlock() instanceof BundledWireTerminationBlock db) {

            for (Map.Entry<Integer, Vec3> n : db.getNodePositions(level, pos, hoveredBlockState).entrySet()) {
                int nodeId = n.getKey();
                Vec3 nodePos = n.getValue();
                if (db.isNodeAccessible(level, pos, hoveredBlockState, nodeId))
                    continue;
                Outliner.getInstance().showAABB("electroenergetics_cnode_" + nodeId,
                                AABB.ofSize(nodePos.add(pos.getX(), pos.getY(), pos.getZ()), 1/32f, 1/32f, 1/32f), 3)
                        .colored(new Color(.3f, .9f, .5f, 1f))
                        .lineWidth(1.5f / 32f)
                        .withFaceTexture(AllSpecialTextures.SELECTION);

                Outliner.getInstance().showAABB("electroenergetics_node_" + nodeId,
                                AABB.ofSize(nodePos.add(pos.getX(), pos.getY(), pos.getZ()), 5/16f, 5/16f, 5/16f), 3)
                        .colored(new Color(.3f, .9f, .5f, 1f))
                        .lineWidth(1 / 32f)
                        .withFaceTexture(AllSpecialTextures.SELECTION);
            }
        }

    }
}
