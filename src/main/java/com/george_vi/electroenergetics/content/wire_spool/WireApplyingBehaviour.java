package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.client.NodeVoltageHolder;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.RequestVoltageDataPacket;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeHelper;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSpecialTextures;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
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

public class WireApplyingBehaviour {

    /**
     * This exists for detached node interaction input.
     */
    public static InWorldNode targetingDetachedNode = null;

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ElectricPropertiesOverlay.INSTANCE.invalidConnection = false;
        ElectricPropertiesOverlay.INSTANCE.connectionTooLong = false;

        ClientLevel level = mc.level;
        BlockPos pos = result.getBlockPos();
        LocalPlayer player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        if (!(heldItem.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(heldItem) || heldItem.is(CEETags.SEE_NODE_DATA))) {
            if (AllItems.WRENCH.isIn(heldItem) && player.isShiftKeyDown()) {
                InWorldNode hoveredNode = InWorldNode.getHitNode(result, level);

                if (hoveredNode == null) {
                    targetingDetachedNode = null;
                    ElectricPropertiesOverlay.INSTANCE.removeHoveredNode();
                    return;
                }

                if (DetachedNodeHelper.isDetached(hoveredNode)) {
                    targetingDetachedNode = hoveredNode;
                    Vec3 nodePos = hoveredNode.getPosition(level);
                    if (nodePos != null)
                        Outliner.getInstance().showAABB("electroenergetics_selected_node", AABB.ofSize(nodePos, 4 / 17f, 4 / 17f, 4 / 17f), 3)
                                .lineWidth(2/128f)
                                .disableLineNormals()
                                .colored(0xff7171);
                }
            } else
                targetingDetachedNode = null;
            ElectricPropertiesOverlay.INSTANCE.removeHoveredNode();
            return;
        }

        boolean toRemove = CEEItems.EMPTY_SPOOL.isIn(heldItem);

        Vec3 selectedPos = null;
        InWorldNode selectedNode = heldItem.get(CEEDataComponents.SELECTED_NODE);
        if (selectedNode != null) {
            BlockState selectedState = level.getBlockState(selectedNode.sourcePos());

            selectedPos = selectedNode.getLocalPosition(level);
            selectedPos = selectedNode.toGlobalPosNoSable(selectedPos, level);
            if (selectedPos == null)
                return;

            Outliner.getInstance().showAABB("electroenergetics_selected_node", AABB.ofSize(selectedPos, 4 / 16f, 4 / 16f, 4 / 16f), 3)
                    .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                    .withFaceTexture(AllSpecialTextures.SELECTION);
        }

        // Display all nodes of block

        BlockState hoveredBlockState = level.getBlockState(pos);

        if (hoveredBlockState.getBlock() instanceof ElectricalDeviceBlock<?> db) {

            for (Map.Entry<Integer, Vec3> n : db.getNodePositions(level, pos, hoveredBlockState).entrySet()) {
                int nodeId = n.getKey();
                Vec3 nodePos = n.getValue();
                if (db.isNodeAccessible(level, pos, hoveredBlockState, nodeId))
                    Outliner.getInstance().showAABB("electroenergetics_node_" + nodeId, AABB.ofSize(nodePos.add(pos.getX(), pos.getY(), pos.getZ()), 4 / 16f, 4 / 16f, 4 / 16f), 3)
                            .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                            .withFaceTexture(AllSpecialTextures.SELECTION);
            }
        }

        // hovered node
        InWorldNode hoveredNode = InWorldNode.getHitNode(result, level);

        if (hoveredNode == null) {
            targetingDetachedNode = null;
            ElectricPropertiesOverlay.INSTANCE.removeHoveredNode();
            return;
        }

        if (DetachedNodeHelper.isDetached(hoveredNode))
            targetingDetachedNode = hoveredNode;

        Vec3 hoveredPos = null;
        if (hoveredBlockState.getBlock() instanceof ElectricalDeviceBlock<?> db)
            hoveredPos = db.getNodePosition(level, pos, hoveredBlockState, hoveredNode.id());

        hoveredPos = hoveredNode.toGlobalPosNoSable(hoveredPos, level);
        if (hoveredPos == null) {
            ElectricPropertiesOverlay.INSTANCE.removeHoveredNode();
            return;
        }

        // display node overlay
        CatnipServices.NETWORK.sendToServer(new RequestVoltageDataPacket(hoveredNode));
        NodeVoltageHolder.VoltageEntry ve = NodeVoltageHolder.getVoltageEntryOrNull(hoveredNode);
        if (ve != null)
            ElectricPropertiesOverlay.INSTANCE.setHoveredNode(ve, hoveredNode);
        //

        // display hovered node outline
        Outliner.getInstance().showAABB("electroenergetics_node_selection", AABB.ofSize(hoveredPos, 5/16f, 5/16f, 5/16f), 3)
                .colored(FontHelper.Palette.STANDARD_CREATE.highlight().getColor().getValue())
                .withFaceTexture(AllSpecialTextures.SELECTION);
        //

        if (selectedNode == null || !(heldItem.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(heldItem)))
            return;
        // Player has selected a node and is looking at one

        boolean canConnect = !selectedNode.equals(hoveredNode);
        boolean isCatenary = false;

        if (hoveredBlockState.getBlock() instanceof ElectricalDeviceBlock<?> db) {

            isCatenary = db instanceof CatenaryHolderBlock && (level.getBlockState(hoveredNode.sourcePos()).getBlock() instanceof CatenaryHolderBlock) &&
                    (heldItem.getItem() instanceof WireSpoolItem wsi) && wsi.wireType.get() == CEEWireTypes.COPPER.get();

            // Don't 'self-connect' a block's nodes
            if (canConnect &&
                    hoveredNode.sourcePos().equals(selectedNode.sourcePos()) &&
                    (!db.canSelfConnect(level, pos, hoveredBlockState, selectedNode.id(), hoveredNode.id()))) {
                ElectricPropertiesOverlay.INSTANCE.invalidConnection = true;
                canConnect = false;
            }
        }

        // Wire too long
        double wireDistance = Math.sqrt(selectedNode.sableSourcePos(level).distSqr(hoveredNode.sableSourcePos(level)));
        if (canConnect &&
                wireDistance > (isCatenary ? CEEConfigs.server().maxCatenaryLength.get() :
                (heldItem.getItem() instanceof WireSpoolItem wsi ? wsi.wireType.get().getMaxLength() : CEEConfigs.server().maxWireLength.get()))) {
            ElectricPropertiesOverlay.INSTANCE.connectionTooLong = true;

            canConnect = false;
        }

        if (canConnect) {
            for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.getAllConnections())
                if (new InWorldNodeConnection(selectedNode, hoveredNode).equals(connection.getFirst())) {
                    canConnect = false;
                    break;
                }
            for (CatenaryConnection connection : WireRenderer.CATENARY)
                if ((connection.pos1().equals(selectedNode.sourcePos()) && connection.pos2().equals(hoveredNode.sourcePos())) ||
                        (connection.pos2().equals(selectedNode.sourcePos()) && connection.pos1().equals(hoveredNode.sourcePos()))) {
                    canConnect = false;
                    break;
                }
        }

        Outliner.getInstance().showAABB("electroenergetics_node_selection", AABB.ofSize(hoveredPos, 5/16f, 5/16f, 5/16f), 3)
                .colored(new Color(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1f))
                .withFaceTexture(AllSpecialTextures.SELECTION);

        if (!toRemove && !mc.isPaused() && wireDistance < 512) {
            for (Vec3 point : QuadraticWireHelper.cablePoints(selectedPos, hoveredPos, isCatenary ? 0 : (heldItem.getItem() instanceof WireSpoolItem wsi ? wsi.wireType.get().getSag() : 1))) {
                if (level.random.nextInt(7) != 0)
                    continue;
                level.addParticle(new DustParticleOptions(new Vector3f(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f), 1),
                        point.x(), point.y(), point.z(),
                        0, 0, 0);
            }
        }
    }

}
