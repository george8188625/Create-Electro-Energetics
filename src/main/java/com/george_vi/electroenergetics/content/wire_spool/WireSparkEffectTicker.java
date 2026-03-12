package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class WireSparkEffectTicker {
    // There are two maps so that if a player places a connection mid-tick, it schedules it for the next tick.
    static Map<Level, Stack<Pair<InWorldNodeConnection, Pair<Vec3, Player>>>> placedConnections = new HashMap<>();
    static Map<Level, Stack<Pair<InWorldNodeConnection, Pair<Vec3, Player>>>> connectionsToCheck = new HashMap<>();

    public static void preTick(Level level) {
        Stack<Pair<InWorldNodeConnection, Pair<Vec3, Player>>> connections = placedConnections.get(level);
        if (connections == null)
            return;
        while (!connections.empty())
            connectionsToCheck.computeIfAbsent(level, l -> new Stack<>()).add(connections.pop());
        placedConnections.remove(level);
    }

    public static void postTick(ServerLevel level, SimulationResults results) {
        Stack<Pair<InWorldNodeConnection, Pair<Vec3, Player>>> connections = connectionsToCheck.get(level);
        if (connections == null)
            return;
        while (!connections.empty()) {
            Pair<InWorldNodeConnection, Pair<Vec3, Player>> e = connections.pop();
            InWorldNodeConnection connection = e.getFirst();
            Player player = e.getSecond().getSecond();
            Vec3 pos = e.getSecond().getFirst();
            double current = Math.abs(results.getCurrentThrough(connection.node1(), connection.node2()));
            if (current > 900)
                CatnipServices.NETWORK.sendToClientsAround(level, pos, 40, new SendSparkPacket(pos, SendSparkPacket.SparkSize.LARGE));
            else  if (current > 35)
                CatnipServices.NETWORK.sendToClientsAround(level, pos, 40, new SendSparkPacket(pos, SendSparkPacket.SparkSize.MEDIUM));

        }
        connectionsToCheck.remove(level);
    }
}
