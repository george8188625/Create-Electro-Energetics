package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.client.NodeVoltageHolder;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.foundation.*;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.RequestVoltageDataPacket;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllSpecialTextures;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
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

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ClientLevel level = mc.level;
        BlockPos pos = result.getBlockPos();
        LocalPlayer player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        if (!(heldItem.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(heldItem))) {
            ElectricPropertiesOverlay.INSTANCE.removeHoveredNode();
            return;
        }

        boolean toRemove = CEEItems.EMPTY_SPOOL.isIn(heldItem);

        Vec3 selectedPos = null;
        InWorldNode selectedNode = null;
        if (heldItem.has(CEEDataComponents.SELECTED_NODE)) {
            selectedNode = heldItem.get(CEEDataComponents.SELECTED_NODE);
            BlockState selectedState = level.getBlockState(selectedNode.sourcePos());

            if (!(selectedState.getBlock() instanceof DeviceBlock db))
                return;

            selectedPos = selectedNode.toGlobalPos(db.getNodePosition(level, pos, selectedState, selectedNode.id()));
            Outliner.getInstance().showAABB("electroenergetics_selected_node", AABB.ofSize(selectedPos, 4/16f, 4/16f, 4/16f), 3)
                    .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                    .withFaceTexture(AllSpecialTextures.SELECTION);
        }

        InWorldNode hoveredNode = InWorldNode.closestNode(level, mc.hitResult.getLocation(), 1f);
        Vec3 hoveredPos = null;

        // Display all nodes of block

        BlockState hoveredBlockState = level.getBlockState(pos);

        if (hoveredBlockState.getBlock() instanceof DeviceBlock db) {

            for (Map.Entry<Integer, Vec3> n : db.getNodePositions(level, pos, hoveredBlockState).entrySet()) {
                int nodeId = n.getKey();
                Vec3 nodePos = n.getValue();
                Outliner.getInstance().showAABB("electroenergetics_node_" + nodeId, AABB.ofSize(nodePos.add(pos.getX(), pos.getY(), pos.getZ()), 4/16f, 4/16f, 4/16f), 3)
                        .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                        .withFaceTexture(AllSpecialTextures.SELECTION);
            }

            if (hoveredNode != null) {

                CatnipServices.NETWORK.sendToServer(new RequestVoltageDataPacket(hoveredNode));
                NodeVoltageHolder.VoltageEntry ve = NodeVoltageHolder.getVoltageEntryOrNull(hoveredNode);
                if (ve != null)
                    ElectricPropertiesOverlay.INSTANCE.setHoveredNode(ve, hoveredNode);
            }
        }

        // Player is not looking at a node, return and say it's too far away

        if (hoveredNode == null) {
            ElectricPropertiesOverlay.INSTANCE.removeHoveredNode();
            CEELang.builder()
                    .translate("wire_spool.too_far_away")
                    .style(ChatFormatting.RED)
                    .component();
            return;
        }

        // Player has selected a node and is looking at one

        BlockState state = level.getBlockState(hoveredNode.sourcePos());

        if (!(state.getBlock() instanceof DeviceBlock db))
            return;
        hoveredPos = db.getNodePosition(level, pos, state, hoveredNode.id());
        if (hoveredPos == null)
            return;

        hoveredPos = hoveredNode.toGlobalPos(hoveredPos);

        Outliner.getInstance().showAABB("electroenergetics_node_selection", AABB.ofSize(hoveredPos, 5/16f, 5/16f, 5/16f), 3)
                .colored(FontHelper.Palette.STANDARD_CREATE.highlight().getColor().getValue())
                .withFaceTexture(AllSpecialTextures.SELECTION);

        if (selectedNode == null)
            return;

        boolean canConnect = true;

        boolean isCatenary = db instanceof CatenaryHolderBlock && (level.getBlockState(hoveredNode.sourcePos()).getBlock() instanceof CatenaryHolderBlock) &&
                (heldItem.getItem() instanceof WireSpoolItem wsi) && wsi.wireType.get() == CEEWireTypes.COPPER.get();

        // Wire too long
        if (Math.sqrt(selectedNode.sourcePos().distSqr(pos)) > (isCatenary ? CEEConfigs.server().maxCatenaryLength.get() :
                (heldItem.getItem() instanceof WireSpoolItem wsi ? wsi.wireType.get().getMaxLength() : CEEConfigs.server().maxWireLength.get()))) {
            mc.gui.setOverlayMessage(
                    Lang.builder(CreateElecrtoEnergetics.ID)
                            .translate("wire_spool.too_far_away")
                            .style(ChatFormatting.RED)
                            .component()
                    , false);
            canConnect = false;
        }

        // Don't 'self-connect' a block's nodes

        if ((canConnect &&
                hoveredNode.sourcePos().equals(selectedNode.sourcePos()) &&
                (!db.canSelfConnect(level, pos, state, selectedNode.id(), hoveredNode.id()) || selectedNode.id() == hoveredNode.id()))) {
            mc.gui.setOverlayMessage(
                    Lang.builder(CreateElecrtoEnergetics.ID)
                            .translate("wire_spool.invalid_connection")
                            .style(ChatFormatting.RED)
                            .component()
                    , false);
            canConnect = false;
        }

        if (canConnect)
            for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.getAllConnections())
                if (new InWorldNodeConnection(selectedNode, hoveredNode).equals(connection.getFirst())) {
                    canConnect = false;
                    break;
                }

        Outliner.getInstance().showAABB("electroenergetics_node_selection", AABB.ofSize(hoveredPos, 5/16f, 5/16f, 5/16f), 3)
                .colored(new Color(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1f))
                .withFaceTexture(AllSpecialTextures.SELECTION);

        if (!toRemove && !mc.isPaused()) {
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
