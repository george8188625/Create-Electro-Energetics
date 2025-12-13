package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.WireData;
import net.createmod.catnip.data.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DyeWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        DyeColor color = DyeColor.getColor(stack);
        if (!(player.level() instanceof ServerLevel sl) || color == null)
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        WireData wireConnectionData = sd.getConnectionData(point.connection());
        if (wireConnectionData.wireType() != CEEWireTypes.STANDARD.get() && !CEEWireTypes.COLORED_WIRES.values().stream().anyMatch(w -> w.get() == wireConnectionData.wireType()))
            return;
        sd.setConnectionData(point.connection(), new WireData(CEEWireTypes.COLORED_WIRES.get(color).get(), wireConnectionData.temperature(), wireConnectionData.attachments()));

    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        return stack.getItem() instanceof DyeItem;
    }

    @Override
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        Pair<NodeConnection, WireData> wireData = WireRenderer.getAllConnections().stream().filter(p -> p.getFirst().equals(point.connection())).findFirst().orElse(null);
        if (wireData == null ||
            (wireData.getSecond().wireType() != CEEWireTypes.STANDARD.get() &&
                    !CEEWireTypes.COLORED_WIRES.values().stream().anyMatch(w -> w.get() == wireData.getSecond().wireType())))
            return null;

        return DisplayType.LINE;
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        DyeColor color = DyeColor.getColor(stack);
        if (color == null)
            return super.getWireDisplayColor(point, level, player, stack);
        return color.getTextColor();
    }
}
