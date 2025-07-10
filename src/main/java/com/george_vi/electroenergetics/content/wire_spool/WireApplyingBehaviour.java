package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.Node;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Objects;

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

        if (!(CEEItems.WIRE_SPOOL.isIn(heldItem) || CEEItems.EMPTY_SPOOL.isIn(heldItem)))
            return;

        boolean toRemove = CEEItems.EMPTY_SPOOL.isIn(heldItem);

        Vec3 alreadySelectedNodePos = null;
        BlockPos alreadySelectedPos = null;
        Integer alreadySelectedNodeID = null;
        if (heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED_POS) && heldItem.getComponents().has(CEEDataComponents.CONNECTOR_CLICKED)) {
            alreadySelectedPos = heldItem.getComponents().get(CEEDataComponents.CONNECTOR_CLICKED_POS);
            alreadySelectedNodeID = heldItem.getComponents().get(CEEDataComponents.CONNECTOR_CLICKED);
            BlockState selectedState = level.getBlockState(alreadySelectedPos);

            if (!(selectedState.getBlock() instanceof DeviceBlock db))
                return;

            for (Map.Entry<Vec3, Integer> n : db.getNodePositions(level, alreadySelectedPos, selectedState).entrySet()) {
                if (Objects.equals(n.getValue(), alreadySelectedNodeID)) {
                    alreadySelectedNodePos = n.getKey().add(alreadySelectedPos.getX(), alreadySelectedPos.getY(), alreadySelectedPos.getZ());
                    break;
                }
            }

            if (alreadySelectedNodePos == null)
                alreadySelectedNodePos = Vec3.ZERO;
            Outliner.getInstance().showAABB("electroenergetics_selected_node", AABB.ofSize(alreadySelectedNodePos, 4/16f, 4/16f, 4/16f), 3)
                    .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                    .withFaceTexture(AllSpecialTextures.SELECTION);
        }

        BlockState state = level.getBlockState(pos);

        // Get hovered Node

        int closestNodeID = 0;
        double closestDistance = 100;
        Vec3 closestPos = Vec3.ZERO;
        Vec3 lookingPos = result.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        if (state.getBlock() instanceof DeviceBlock db) {

            for (Map.Entry<Vec3, Integer> n : db.getNodePositions(level, pos, state).entrySet()) {
                if (lookingPos.distanceTo(n.getKey()) < closestDistance) {
                    closestDistance = lookingPos.distanceTo(n.getKey());
                    closestPos = n.getKey().add(pos.getX(), pos.getY(), pos.getZ());
                    closestNodeID = n.getValue();
                }
                Outliner.getInstance().showAABB("electroenergetics_node_" + n.getValue(), AABB.ofSize(n.getKey().add(pos.getX(), pos.getY(), pos.getZ()), 4/16f, 4/16f, 4/16f), 3)
                        .colored(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue())
                        .withFaceTexture(AllSpecialTextures.SELECTION);
            }

            if (closestDistance < 0.4)
                if (WireRenderer.getAllVoltages().containsKey(new Node(closestNodeID, pos)))
                    mc.gui.setOverlayMessage(db.getNodeLabel(level, pos, state, closestNodeID).withStyle(ChatFormatting.GOLD).append(Component.literal(" ")).append(Component.literal(String.format("%.1f", WireRenderer.getAllVoltages().get(new Node(closestNodeID, pos)))).withStyle(ChatFormatting.GRAY)).append(Component.literal("V").withStyle(ChatFormatting.GRAY)), false);
                else
                    mc.gui.setOverlayMessage(db.getNodeLabel(level, pos, state, closestNodeID).withStyle(ChatFormatting.GOLD), false);
        } else {
            closestDistance = 0;
            closestPos = result.getLocation();
            closestNodeID = -1;
        }


        if (closestDistance < 0.4) {


            if (alreadySelectedNodeID == null) {
                if (closestNodeID != -1)
                    Outliner.getInstance().showAABB("electroenergetics_node_selection", AABB.ofSize(closestPos, 5/16f, 5/16f, 5/16f), 3)
                            .colored(FontHelper.Palette.STANDARD_CREATE.highlight().getColor().getValue())
                            .withFaceTexture(AllSpecialTextures.SELECTION);
                return;
            }

            boolean canConnect = closestNodeID != -1;
            if (alreadySelectedPos.distManhattan(pos) > CEEConfigs.server().maxWireLength.get()) {
                mc.gui.setOverlayMessage(
                        Lang.builder(CreateElecrtoEnergetics.ID)
                                .translate("wire_spool.too_far_away")
                                .style(ChatFormatting.RED)
                                .component()
                        , false);
                canConnect = false;
            }

            if (canConnect && pos.equals(alreadySelectedPos) && state.getBlock() instanceof DeviceBlock db && (!db.canSelfConnect(level, pos, state, alreadySelectedNodeID, closestNodeID) || alreadySelectedNodeID == closestNodeID)) {
                mc.gui.setOverlayMessage(
                        Lang.builder(CreateElecrtoEnergetics.ID)
                                .translate("wire_spool.invalid_connection")
                                .style(ChatFormatting.RED)
                                .component()
                        , false);
                canConnect = false;
            }

            if (canConnect)
                for (NodeConnection connection : WireRenderer.getAllConnections())
                    if (new NodeConnection(new Node(alreadySelectedNodeID, alreadySelectedPos), new Node(closestNodeID, pos)).equals(connection)) {
                        canConnect = false;
                        break;
                    }
            if (closestNodeID != -1)
                Outliner.getInstance().showAABB("electroenergetics_node_selection", AABB.ofSize(closestPos, 5/16f, 5/16f, 5/16f), 3)
                        .colored(new Color(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1f))
                        .withFaceTexture(AllSpecialTextures.SELECTION);

            if (!toRemove && !mc.isPaused() && alreadySelectedNodePos != null) {
                for (Vec3 point : WireRenderer.cablePoints(alreadySelectedNodePos, closestPos, 1)) {
                    if (level.random.nextInt(7) != 0)
                        continue;
                    level.addParticle(new DustParticleOptions(new Vector3f(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f), 1),
                            point.x(), point.y(), point.z(),
                            0, 0, 0);
                }
            }
        }
    }

}
