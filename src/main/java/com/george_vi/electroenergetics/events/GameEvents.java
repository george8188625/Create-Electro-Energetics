package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.client.WireEffects;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

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

        WireSync.unloadForPlayer(player);
        WireSync.handlePlayerEnterNewSection(player, new ChunkPos(player.blockPosition()));
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        WireSync.unloadForPlayer(player);
        WireSync.handlePlayerEnterNewSection(player, new ChunkPos(player.blockPosition()));
    }

    @SubscribeEvent
    public static void enterSection(EntityEvent.EnteringSection event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (event.getNewPos().getX() == event.getOldPos().getX() && event.getNewPos().getZ() == event.getOldPos().getZ())
            return;

        WireSync.handlePlayerEnterNewSection(player, event.getNewPos().chunk());
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

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
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

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CEECommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void addToElectricGraph(AddToElectricGraphEvent event) {
        InfrastructureSavedData sd = event.sd;
        if (sd.wireSimulationState.rebuild)
            sd.wireSimulationState.rebuild();

        sd.catenaryModule.buildCircuit(event.builder);
        sd.wireElectrocutionModule.buildCircuit(event.builder);
        sd.wireCrossContactModule.buildCircuit(event.builder);
        sd.wireAssemblerModule.buildCircuit(event.builder);
    }

    @SubscribeEvent
    public static void finishElectricSimulation(FinishElectricSimulationEvent event) {
        InfrastructureSavedData sd = event.sd;
        sd.wireElectrocutionModule.finishSimulation(event.results);
        sd.catenaryModule.finishSimulation(event.results);
    }

    @SubscribeEvent
    public static void spawnMob(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL || event.getResult() == MobSpawnEvent.SpawnPlacementCheck.Result.FAIL)
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(event.getLevel().getLevel());
        boolean foundBulb = sd.getDevices().stream().filter(d -> d.simulatedDevice() instanceof BulbDevice && d.pos().getCenter().distanceToSqr(event.getPos().getCenter()) <= 400).anyMatch(d -> true);
        if (foundBulb)
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void reloadLevelRenderer(ReloadLevelRendererEvent event) {
        WireRenderer.recreateVisuals();
    }

}
