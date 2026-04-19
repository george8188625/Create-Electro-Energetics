package com.george_vi.electroenergetics.content.wire.interaction;

import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public abstract class WireInteractionBehaviour {
    public abstract void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack);

    public abstract boolean isActiveFor(ItemStack stack);

    @OnlyIn(Dist.CLIENT)
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return DisplayType.DOT;
    }

    @OnlyIn(Dist.CLIENT)
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return 0xFFFFFFFF;
    }

    public boolean tryUseOnWire(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) {
            NodeConnectionPoint point = WireInteractionHandler.targetedPoint;
            if (point == null || usedHand != InteractionHand.MAIN_HAND)
                return false;
            CatnipServices.NETWORK.sendToServer(new InteractWirePacket(point));
            return true;
        }
        return true;
    }

    public void attachToWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack, WireAttachment toAttach) {
        if (!(level instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        WireData data = sd.getConnectionData(new InWorldNodeConnection(point.node1(), point.node2()));
        for (Pair<Float, WireAttachment> attachment : data.attachments()) {
            if (Math.abs(point.point() - attachment.getFirst()) * Math.sqrt(point.node1().sourcePos().distSqr(point.node2().sourcePos())) < attachment.getSecond().getWidth() / 2 + 0.25)
                return;
        }

        List<Pair<Float, WireAttachment>> attachments = new ArrayList<>(data.attachments());
        attachments.add(Pair.of(point.point(), toAttach));
        sd.setConnectionData(new InWorldNodeConnection(point.node1(), point.node2()), new WireData(data.wireType(), data.temperature(), attachments, data.length));

        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, BlockPos.containing(point.posAt(Vec3.atCenterOf(point.node1().sourcePos()), Vec3.atCenterOf(point.node2().sourcePos()), data.wireType().getSag())));

        if (!player.isCreative())
            stack.shrink(1);
    }

    public int attachmentColor() {
        NodeConnectionPoint point = WireInteractionHandler.targetedPoint;
        if (point == null)
            return 0;
        WireData data = WireRenderer.getConnectionData(new InWorldNodeConnection(point.node1(), point.node2()));
        if (data == null)
            return 0x9ede73;

        for (Pair<Float, WireAttachment> attachment : data.attachments()) {
            if (Math.abs(point.point() - attachment.getFirst()) * Math.sqrt(point.node1().sourcePos().distSqr(point.node2().sourcePos())) < attachment.getSecond().getWidth() / 2 + 0.25)
                return 0xff7171;
        }

        return 0x9ede73;
    }

    public enum DisplayType {
        DOT, LINE, NONE;
    }

    public static class Empty extends WireInteractionBehaviour {
        @Override
        public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {

        }

        @Override
        public boolean isActiveFor(ItemStack stack) {
            return false;
        }
    }
}
