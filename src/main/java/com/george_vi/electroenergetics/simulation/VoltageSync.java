package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.foundation.AttachedNode;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.foundation.NodeConnection;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class VoltageSync {
    static Map<Node, Double> oldVoltages = new HashMap<>();
    public static void finishSimulation(InfrastructureSavedData sd, ServerLevel level) {
        Map<Node, Double> voltages = Map.copyOf(sd.VOLTAGES);
        Map<Player, NodeConnection> clampMeteringPlayers = new HashMap<>();
        for (Player player : level.getPlayers(p -> true)) {
            if (player.isUsingItem()) {
                ItemStack usedStack = player.getItemInHand(player.getUsedItemHand());
                if (CEEItems.CLAMP_METER.isIn(usedStack)) {
                    if (usedStack.has(CEEDataComponents.NODE_CONNECTION)) {
                        clampMeteringPlayers.put(player, usedStack.getOrDefault(CEEDataComponents.NODE_CONNECTION, new NodeConnection(BlockPos.ZERO, 0, 0)));
                    }
                }
            }
        }

        for (Map.Entry<Node, Double> e : voltages.entrySet()) {
            Node node = e.getKey();
            Double voltage = e.getValue();
            InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(node.sourcePos());
            if (node instanceof AttachedNode || deviceInstance == null)
                continue;


            CatnipServices.NETWORK.sendToClients(level.getPlayers(p -> p.position().distanceTo(node.sourcePos().getCenter()) <= deviceInstance.simulatedDevice().sendVoltagesDistance() || (clampMeteringPlayers.containsKey(p) && clampMeteringPlayers.get(p).isAny(node))),
                    new SendVoltageDataPacket(node.sourcePos(), node.id(), voltage));
        }
    }
}
