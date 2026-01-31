package com.george_vi.electroenergetics.content.clamp_meter;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClampMeterWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        player.releaseUsingItem();
        stack.set(CEEDataComponents.NODE_CONNECTION, new InWorldNodeConnection(point.node1(), point.node2()));
        player.startUsingItem(InteractionHand.MAIN_HAND);
    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        return CEEItems.CLAMP_METER.isIn(stack);
    }
}
