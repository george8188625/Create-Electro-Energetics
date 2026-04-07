package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AttachmentRemovalWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        WireData data = sd.getConnectionData(new InWorldNodeConnection(point.node1(), point.node2()));
        List<Pair<Float, WireAttachment>> attachments = data.attachments();
        for (int i = 0; i < attachments.size(); i++) {
            Pair<Float, WireAttachment> attachment = attachments.get(i);
            if (Math.abs(point.point() - attachment.getFirst()) * Math.sqrt(point.node1().sourcePos().distSqr(point.node2().sourcePos())) < attachment.getSecond().getWidth() / 2 + 0.25) {
                AllSoundEvents.WRENCH_REMOVE.playOnServer(level, BlockPos.containing(point.posAt(Vec3.atCenterOf(point.node1().sourcePos()), Vec3.atCenterOf(point.node2().sourcePos()), data.wireType().getSag())));
                Vec3 pos = QuadraticWireHelper.posAt(Vec3.atCenterOf(point.node1().sourcePos()), Vec3.atCenterOf(point.node2().sourcePos()), 1.0f - attachment.getFirst(), data.wireType().getSag());
                for (ItemStack drop : attachment.getSecond().getDrops(level))
                    Containers.dropItemStack(level, pos.x, pos.y, pos.z, drop);
                attachments.remove(i);
                sd.setConnectionData(new InWorldNodeConnection(point.node1(), point.node2()), new WireData(data.wireType(), data.temperature(), attachments));
                return;
            }
        }
    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        return AllItems.WRENCH.isIn(stack);
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (point == null)
            return 0;
        WireData data = WireRenderer.getConnectionData(new InWorldNodeConnection(point.node1(), point.node2()));
        if (data == null)
            return 0x9ede73;

        for (Pair<Float, WireAttachment> attachment : data.attachments()) {
            if (Math.abs(point.point() - attachment.getFirst()) * Math.sqrt(point.node1().sourcePos().distSqr(point.node2().sourcePos())) < attachment.getSecond().getWidth() / 2 + 0.25)
                return 0xff7171;
        }

        return 0xffffff;
    }
}
