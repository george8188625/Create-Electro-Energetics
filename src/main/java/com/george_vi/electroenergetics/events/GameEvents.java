package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHandler;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.wire.LoadedWireManager;
import com.george_vi.electroenergetics.content.wire.WireEffects;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.*;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = CreateElecrtoEnergetics.ID)
public class GameEvents {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void tickClient(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;
        WireApplyingBehaviour.tick();
        WireInteractionHandler.tick();
        WireEffects.tick();
        ElectricTrainSounds.tick();
    }

    @SubscribeEvent
    public static void tickLevel(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level)
            SimulationTicker.tick(level);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            WireRenderer.render(event.getLevelRenderer(), event.getPoseStack(), event.getCamera());
    }

    @SubscribeEvent
    public static void enterDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        LoadedWireManager.unloadForPlayer(player);
        LoadedWireManager.handlePlayerEnterNewSection(player, new ChunkPos(player.blockPosition()));
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        LoadedWireManager.unloadForPlayer(player);
        LoadedWireManager.handlePlayerEnterNewSection(player, new ChunkPos(player.blockPosition()));
    }

    @SubscribeEvent
    public static void enterSection(EntityEvent.EnteringSection event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (event.getNewPos().getX() == event.getOldPos().getX() && event.getNewPos().getZ() == event.getOldPos().getZ())
            return;

        LoadedWireManager.handlePlayerEnterNewSection(player, event.getNewPos().chunk());
    }

    @SubscribeEvent
    public static void playerInteractItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND)
            return;
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(event.getItemStack()))
                .findFirst().orElse(null);
        if (behaviour == null)
            return;

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand()))
            event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void playerInteractOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND)
            return;
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(event.getItemStack()))
                .findFirst().orElse(null);
        if (behaviour == null)
            return;

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand()))
            event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CEECommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void addToElectricGraph(AddToElectricGraphEvent event) {
        CatenaryHandler.addToGraph(event);
    }

    @SubscribeEvent
    public static void finishElectricSimulation(FinishElectricSimulationEvent event) {
        CatenaryHandler.finishSimulation(event);
    }

}
