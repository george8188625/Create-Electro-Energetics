package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllKeys;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChangeLengthWireInteractionBehaviour extends WireInteractionBehaviour {

    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InWorldNodeConnection connection = point.connection();
        WireData connectionData = sd.getConnectionData(connection);

        if (connectionData == null)
            return;

        TagKey<Item> droppedTag = connectionData.wireType().getDroppedTag();
        if (droppedTag == null && connectionData.wireType().getDrops() != stack.getItem())
           return;
        if (droppedTag != null && !stack.is(droppedTag))
            return;

        Vec3 pos1 = connection.node1().getPosition(level);
        Vec3 pos2 = connection.node2().getPosition(level);

        if (pos1 == null)
            pos1 = connection.node1().sableSourcePos(level).getCenter();

        if (pos2 == null)
            pos2 = connection.node1().sableSourcePos(level).getCenter();

        double distance = pos1.distanceTo(pos2);

        if (player.isShiftKeyDown()) {
            double newLength = connectionData.length - 0.25;
            double lengthDiff = distance - newLength;
            double lengthRatio = newLength == 0 ? 1 : distance / newLength;

            boolean shouldBreakWire = (lengthDiff > 1) && (connectionData.getSag() == 0 ? Math.abs(1 - lengthRatio) > 0.1 : lengthRatio > 1.4f);

            if (shouldBreakWire || newLength < 0.5) {
                ((ServerPlayer)player).connection.send(new ClientboundSetActionBarTextPacket(
                        CEELang.translateDirect("action_bar.wire_too_short").withStyle(ChatFormatting.RED)));
                return;
            }
            connectionData.length = newLength;
        } else {
            if (connectionData.length >= connectionData.wireType().getMaxLength()) {
                ((ServerPlayer)player).connection.send(new ClientboundSetActionBarTextPacket(
                        CEELang.translateDirect("action_bar.wire_too_long").withStyle(ChatFormatting.RED)));
                return;
            }
            connectionData.length += 0.25f;
        }

        if (sd.wireSimulationState.relocateConnection(connection, connectionData)) {
            Vec3 pos = VecHelper.lerp(0.5f, connection.node1().sableSourcePos(level).getCenter(), connection.node2().sableSourcePos(level).getCenter());
            Containers.dropItemStack(level, pos.x, pos.y, pos.z, new ItemStack(sd.removeConnection(connection).wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
        } else
            sd.wireSync.handleWireAdded(connection, connectionData);
    }

    @Override
    public boolean isActiveFor(ItemStack stack, Player player) {
        if (stack.isEmpty())
            return false;
        for (WireType wireType : CEERegistries.WIRE_TYPE) {
                TagKey<Item> droppedTag = wireType.getDroppedTag();
            if (droppedTag == null && wireType.getDrops() != stack.getItem())
                continue;
            if (droppedTag != null && !stack.is(droppedTag))
                continue;
            return true;
        }
        return false;
    }

    @Override
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!AllKeys.altDown())
            return DisplayType.NONE;

        InWorldNodeConnection connection = point.connection();

        WireData wireData = WireRenderer.getConnectionData(connection);

        if (wireData == null)
            return DisplayType.NONE;

        TagKey<Item> droppedTag = wireData.wireType().getDroppedTag();
        if (droppedTag == null && wireData.wireType().getDrops() != stack.getItem())
            return DisplayType.NONE;
        if (droppedTag != null && !stack.is(droppedTag))
            return DisplayType.NONE;

        return DisplayType.LINE;
    }

    public static boolean mouseScrolled(double delta) {
        if (!AllKeys.altDown() )
            return false;

        Minecraft mc = Minecraft.getInstance();

        NodeConnectionPoint point = WireInteractionHandler.targetedPoint;
        if (point == null || mc.player == null)
            return false;

        InWorldNodeConnection connection = point.connection();

        WireData wireData = WireRenderer.getConnectionData(connection);

        if (wireData == null)
            return false;

        ItemStack stack = mc.player.getMainHandItem();
        TagKey<Item> droppedTag = wireData.wireType().getDroppedTag();
        if (!stack.is(wireData.wireType().getSpooledItem())) {
            if (droppedTag == null && wireData.wireType().getDrops() != stack.getItem())
                return false;
            if (droppedTag != null && !stack.is(droppedTag))
                return false;
        }

        CatnipServices.NETWORK.sendToServer(new ChangeLengthWirePacket(point, (byte) delta));

        return true;
    }
}
