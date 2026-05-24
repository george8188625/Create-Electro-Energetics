package com.george_vi.electroenergetics.content.linemans_stick;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LinemansStickWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel sl))
            return;
        ItemStack offHandItemStack = player.getOffhandItem();

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InWorldNodeConnection connection = new InWorldNodeConnection(point.node1(), point.node2());
        WireData data = sd.getConnectionData(connection);
        List<Pair<Float, WireAttachment>> attachments = data.attachments();

        Vec3 pos1 = connection.node1().getPosition(level);
        Vec3 pos2 = connection.node2().getPosition(level);

        if (pos1 == null || pos2 == null) {
            pos1 = connection.node1().sourcePos().getCenter();
            pos2 = connection.node2().sourcePos().getCenter();
        }

        double distance = pos1.distanceTo(pos2);

        // Attachment installation:
        if (offHandItemStack.is(CEETags.WIRE_ATTACHMENT) && !player.isShiftKeyDown()) {
            // Search for the wire interaction behavior responsible for the item

            WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                    .filter(h -> h.isActiveFor(offHandItemStack))
                    .findFirst().orElse(null);

            if (behaviour == null)
                return;

            behaviour.interactWire(point, level, player, offHandItemStack);
        }

        // Attachment removal:
        if (offHandItemStack.isEmpty() && player.isShiftKeyDown())
            for (int i = 0; i < attachments.size(); i++) {
                Pair<Float, WireAttachment> attachment = attachments.get(i);
                if (Math.abs(point.point() - attachment.getFirst()) * distance < attachment.getSecond().getWidth() / 2 + 0.25) {
                    AllSoundEvents.WRENCH_REMOVE.playOnServer(level, BlockPos.containing(point.posAt(pos1, pos2, data.getSag(distance))));
                    Vec3 pos = QuadraticWireHelper.posAt(pos1, pos2, attachment.getFirst(), data.getSag());

                    for (ItemStack drop : attachment.getSecond().getDrops(level)) {
                        if (player.getOffhandItem().isEmpty())
                            player.getInventory().setItem(Inventory.SLOT_OFFHAND, drop);
                        else
                            Containers.dropItemStack(level, pos.x, pos.y, pos.z, drop);
                    }

                    attachments.remove(i);
                    sd.setConnectionData(connection, new WireData(data.wireType(), data.temperature(), attachments, data.length));
                    return;
                }
            }
    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        return stack.getItem() == CEEItems.LINEMANS_STICK.get();
    }

    @Override
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (LinemansStickClientHandler.currentMode == LinemansStickMode.NONE ||
                LinemansStickClientHandler.currentMode == LinemansStickMode.WIRE_MOVE)
            return DisplayType.NONE;
        return DisplayType.DOT;
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (LinemansStickClientHandler.currentMode == LinemansStickMode.ATTACHMENT_REMOVAL) {
            if (point == null)
                return 0;
            InWorldNodeConnection connection = new InWorldNodeConnection(point.node1(), point.node2());
            WireData data = WireRenderer.getConnectionData(connection);
            if (data == null)
                return 0x9ede73;

            Vec3 pos1 = connection.node1().getPosition(level);
            Vec3 pos2 = connection.node2().getPosition(level);

            if (pos1 == null || pos2 == null) {
                pos1 = connection.node1().sourcePos().getCenter();
                pos2 = connection.node2().sourcePos().getCenter();
            }

            double distance = pos1.distanceTo(pos2);

            for (Pair<Float, WireAttachment> attachment : data.attachments()) {
                if (Math.abs(point.point() - attachment.getFirst()) * distance < attachment.getSecond().getWidth() / 2 + 0.25)
                    return 0xff7171;
            }

            return 0xffffff;
        }
        return super.getWireDisplayColor(point, level, player, stack);
    }
}
