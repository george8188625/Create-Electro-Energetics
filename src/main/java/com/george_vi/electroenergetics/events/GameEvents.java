package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.content.wire_spool.ClearWireConnectionsPacket;
import com.george_vi.electroenergetics.content.wire_spool.SendWireConnectionsPacket;
import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.WireRenderer;
import com.george_vi.electroenergetics.simulation.NodeConnection;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.Node;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.simibubi.create.infrastructure.command.AllCommands;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = CreateElecrtoEnergetics.ID)
public class GameEvents {
    @SubscribeEvent
    public static void tickClient(ClientTickEvent.Pre event) {
        WireApplyingBehaviour.tick();
    }

    @SubscribeEvent
    public static void tickLevel(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level)
            SimulationTicker.tick(level);
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            WireRenderer.render(event.getLevelRenderer(), event.getPoseStack(), event.getCamera());
    }

    @SubscribeEvent
    public static void enterDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        CatnipServices.NETWORK.sendToClient(player, ClearWireConnectionsPacket.clearAll());

        if (!(player.level() instanceof ServerLevel sl))
            return;
        ServerLevel level = sl.getServer().getLevel(event.getTo());

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        List<Node> newNodes = sd.getNodes().stream().filter(n -> Math.sqrt(n.sourcePos().atY(0).distManhattan(event.getEntity().getOnPos())) < 200).toList();

        Set<NodeConnection> connections = new HashSet<>();
        for (Node node : newNodes)
            connections.addAll(sd.getConnections(node));

        for (NodeConnection connection : connections)
            CatnipServices.NETWORK.sendToClient(player, new SendWireConnectionsPacket(connection.node1().sourcePos(), connection.node1().id(), connection.node2().sourcePos(), connection.node2().id()));

    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (!(player.level() instanceof ServerLevel level))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        List<Node> newNodes = sd.getNodes().stream().filter(n -> Math.sqrt(n.sourcePos().atY(0).distManhattan(event.getEntity().getOnPos())) < 250).toList();

        Set<NodeConnection> connections = new HashSet<>();
        for (Node node : newNodes)
            connections.addAll(sd.getConnections(node));

        for (NodeConnection connection : connections)
            CatnipServices.NETWORK.sendToClient(player, new SendWireConnectionsPacket(connection.node1().sourcePos(), connection.node1().id(), connection.node2().sourcePos(), connection.node2().id()));
    }

    @SubscribeEvent
    public static void enterSection(EntityEvent.EnteringSection event) {
        BlockPos oldPos = event.getOldPos().center().atY(0);
        BlockPos newPos = event.getNewPos().center().atY(0);

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            if (!(event.getEntity() instanceof Player))
                return;
            List<NodeConnection> connections = WireRenderer.getAllConnections();
            List<NodeConnection> toRemove = new ArrayList<>();
            for (NodeConnection connection : connections)
                if (Math.sqrt(connection.node1().sourcePos().atY(0).distManhattan(newPos)) > 250 &&
                        Math.sqrt(connection.node2().sourcePos().atY(0).distManhattan(newPos)) > 250)
                    toRemove.add(connection);
            WireRenderer.removeConnections(toRemove);

            return;
        }

        if (!(player.level() instanceof ServerLevel level))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        List<Node> newNodes = sd.getNodes().stream().filter(n ->
                Math.sqrt(n.sourcePos().atY(0).distManhattan(newPos)) < 200 &&
                Math.sqrt(n.sourcePos().atY(0).distManhattan(oldPos)) >= 200
        ).toList();

        Set<NodeConnection> connections = new HashSet<>();
        for (Node node : newNodes)
             connections.addAll(sd.getConnections(node));

        for (NodeConnection connection : connections)
            CatnipServices.NETWORK.sendToClient(player, new SendWireConnectionsPacket(connection.node1().sourcePos(), connection.node1().id(), connection.node2().sourcePos(), connection.node2().id()));

    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CEECommands.register(event.getDispatcher());
    }
}
