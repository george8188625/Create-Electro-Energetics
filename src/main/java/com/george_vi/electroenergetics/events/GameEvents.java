package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEESimulatedDeviceFeatureTypes;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.client.WireEffects;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.ClientTrainGaugeData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.WireSparkEffectTicker;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackShape;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CreateElectroEnergetics.ID)
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
        ClientTrainGaugeData.tick();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderHighlightBlock(RenderHighlightEvent.Block event) {
        BlockPos pos = event.getTarget().getBlockPos();
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null)
            return;

        BlockState state = level.getBlockState(pos);
        if (CEEBlocks.ACCUMULATOR.has(state))
            AccumulatorBlock.renderHighlightBlock(event, state);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            WireRenderer.render(event.getLevelRenderer(), event.getPoseStack(), event.getCamera());
    }

    @SubscribeEvent
    public static void serverTickEvent(ServerTickEvent.Post event) {
        if (ModEvents.changedConfigs.getAndSet(false))
            for (ServerLevel level : event.getServer().getAllLevels()) {
                InfrastructureSavedData sd = InfrastructureSavedData.load(level);
                sd.wireSimulationState.onReloadConfigs();
                sd.wireSimulationState.reloadLazyConnections();
            }
    }

    @SubscribeEvent
    public static void serverLevelTickEvent(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(level);
            sd.tick();

            // It's not done through EntityEvent.EnteringSection, because other mods may position the entity multiple
            // times a tick.
            // e.g. Sitting in a seat on a sable contraption causes it to teleport twice a tick between the plot
            // location and the normal location, causing WireSync to send a ton of wire update packets and flickering.
            for (ServerPlayer player : level.players()) {
                Vec3 pos = player.position();
                sd.wireSync.handlePlayerEnterNewSection(player,
                        ChunkPos.asLong(Mth.floor(pos.x) >> 4, Mth.floor(pos.z) >> 4));
            }
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ConverterBlockEntity.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void enterDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) player.level());
        WireSync wireSync = sd.wireSync;
        wireSync.unloadForPlayer(player);
        wireSync.handlePlayerEnterNewSection(player, ChunkPos.asLong(player.blockPosition()));
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load((ServerLevel) player.level());
        WireSync wireSync = sd.wireSync;
        wireSync.unloadForPlayer(player);
        wireSync.handlePlayerEnterNewSection(player, ChunkPos.asLong(player.blockPosition()));
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
        WireSparkEffectTicker.preTick(event.level);
    }

    @SubscribeEvent
    public static void finishElectricSimulation(FinishElectricSimulationEvent event) {
        InfrastructureSavedData sd = event.sd;
        sd.wireElectrocutionModule.finishSimulation(event.results);
        sd.catenaryModule.finishSimulation(event.results);
        WireSparkEffectTicker.postTick(event.level, event.results);
    }

    @SubscribeEvent
    public static void spawnMob(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL || event.getResult() == MobSpawnEvent.SpawnPlacementCheck.Result.FAIL)
            return;
        DevicesSavedData sd = DevicesSavedData.load(event.getLevel().getLevel());
        boolean foundBulb = sd.getDevices(CEESimulatedDeviceFeatureTypes.TICKING_ELECTRICAL.get()).stream().filter(d -> d instanceof BulbDevice && d.pos.getCenter().distanceToSqr(event.getPos().getCenter()) <= 400).anyMatch(d -> true);
        if (foundBulb)
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void reloadLevelRenderer(ReloadLevelRendererEvent event) {
        WireRenderer.recreateVisuals();
    }

}
