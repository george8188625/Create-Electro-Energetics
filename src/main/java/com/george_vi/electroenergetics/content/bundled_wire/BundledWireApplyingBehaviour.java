package com.george_vi.electroenergetics.content.bundled_wire;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllSpecialTextures;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

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


        InWorldNode hoveredNode = null;
        BlockState hoveredBlockState = level.getBlockState(pos);
        Vec3 hoveredNodePos = Vec3.atCenterOf(pos);

        if (hoveredBlockState.getBlock() instanceof BundledWireTerminationBlock db) {

            for (Map.Entry<Integer, Vec3> n : db.getNodePositions(level, pos, hoveredBlockState).entrySet()) {
                int nodeId = n.getKey();
                Vec3 nodePos = n.getValue();
                if (db.isNodeAccessible(level, pos, hoveredBlockState, nodeId))
                    continue;
                hoveredNode = new InWorldNode(nodeId, pos);
                hoveredNodePos = nodePos.add(pos.getX(), pos.getY(), pos.getZ());
            }
        }

        BundledWireTerminationState selectedTermination = heldItemStack.get(CEEDataComponents.SELECTED_WIRE_TERMINATION);
        BlockState selectedState = null;
        BlockPos selectedPos = null;
        Vec3 selectedNodePos = null;
        InWorldNode selectedNode = null;

        if (selectedTermination != null) {
            selectedPos = selectedTermination.pos();
            selectedState = level.getBlockState(selectedPos);
            selectedNodePos = Vec3.atCenterOf(selectedPos);

            if (selectedState.getBlock() instanceof BundledWireTerminationBlock db) {
                for (Map.Entry<Integer, Vec3> n : db.getNodePositions(level, selectedPos, selectedState).entrySet()) {
                    int nodeId = n.getKey();
                    Vec3 nodePos = n.getValue();
                    if (db.isNodeAccessible(level, selectedPos, selectedState, nodeId))
                        continue;
                    Vec3 globalNodePos = nodePos.add(selectedPos.getX(), selectedPos.getY(), selectedPos.getZ());
                    selectedNodePos = globalNodePos;
                    selectedNode = new InWorldNode(nodeId, selectedPos);
                    Outliner.getInstance().showAABB("electroenergetics_csnode_" + nodeId,
                                    AABB.ofSize(globalNodePos, 1/32f, 1/32f, 1/32f), 3)
                            .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                            .lineWidth(1.5f / 32f)
                            .withFaceTexture(AllSpecialTextures.SELECTION);

                    Outliner.getInstance().showAABB("electroenergetics_snode_" + nodeId,
                                    AABB.ofSize(globalNodePos, 4/16f, 4/16f, 4/16f), 3)
                            .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                            .lineWidth(1 / 32f)
                            .withFaceTexture(AllSpecialTextures.SELECTION);
                }
            } else {
                selectedTermination = null;
            }
        }

        BooleanBooleanPair placement = BundledWireItem.desirableState(result.getLocation().subtract(Vec3.atLowerCornerOf(pos)), clickedFace);

        boolean canConnect = true;

        int maxLength = CEEConfigs.server().maxBundledWireLength.get();

        if (selectedPos != null && selectedPos.distSqr(pos) > maxLength * maxLength)
            canConnect = false;

        if (canConnect && selectedNode != null) {
            for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.getAllConnections()) {
                if (new InWorldNodeConnection(selectedNode, hoveredNode).equals(connection.getFirst())) {
                    canConnect = false;
                    break;
                }
            }
        }

        if (hoveredNode != null) {

            Outliner.getInstance().showAABB("electroenergetics_cnode",
                            AABB.ofSize(hoveredNodePos, 1/32f, 1/32f, 1/32f), 3)
                    .colored(new Color(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1f))
                    .lineWidth(1.5f / 32f)
                    .withFaceTexture(AllSpecialTextures.SELECTION);

            Outliner.getInstance().showAABB("electroenergetics_node",
                            AABB.ofSize(hoveredNodePos, 5/16f, 5/16f, 5/16f), 3)
                    .colored(new Color(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1f))
                    .lineWidth(1 / 32f)
                    .withFaceTexture(AllSpecialTextures.SELECTION);
        }


        if (!mc.isPaused() && selectedNodePos != null) {
            for (Vec3 point : QuadraticWireHelper.cablePoints(selectedNodePos, hoveredNodePos, 1)) {
                if (level.random.nextInt(7) != 0)
                    continue;
                level.addParticle(new DustParticleOptions(new Vector3f(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f), 1),
                        point.x(), point.y(), point.z(),
                        0, 0, 0);
            }
        }

    }
}
