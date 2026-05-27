package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class VoltageSync {

    public static void finishSimulation(InfrastructureSavedData sd, ServerLevel level, SimulationResults results) {

        for (ServerPlayer player : level.getPlayers(p -> true)) {
            if (!player.isUsingItem())
                continue;

            ItemStack usedStack = player.getItemInHand(player.getUsedItemHand());
            if (!CEEItems.CLAMP_METER.isIn(usedStack) && !(CEEItems.LINEMANS_STICK.isIn(usedStack) &&
                    CEEItems.CLAMP_METER.isIn(player.getOffhandItem())))
                continue;

            if (usedStack.has(CEEDataComponents.NODE_CONNECTION)) {
                InWorldNodeConnection connection = usedStack.getOrDefault(CEEDataComponents.NODE_CONNECTION, new InWorldNodeConnection(BlockPos.ZERO, 0, 0));
                SendVoltageDataPacket packet = new SendVoltageDataPacket();
                packet.nodes = new InWorldNode[2];
                packet.voltages = new double[2 << results.microTickBits];
                packet.frequencies = new float[2];
                packet.microTickBits = (byte) results.microTickBits;

                packet.nodes[0] = connection.node1();
                packet.nodes[1] = connection.node2();

                int id1 = results.circuitBuilder.nodeIndexes.getInt(connection.node1()) << results.microTickBits;
                int id2 = results.circuitBuilder.nodeIndexes.getInt(connection.node2()) << results.microTickBits;
                for (int j = 0; j < results.microTicks; j++) {
                    packet.voltages[j] = results.voltages[id1 | j];
                    packet.voltages[results.microTicks + j] = results.voltages[id2 | j];
                }

                CatnipServices.NETWORK.sendToClient(player, packet);
            }
        }
    }
}
