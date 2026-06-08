package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.client.ClientNodeData;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.client.WireEffects;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.bundled_wire.BundledWireApplyingBehaviour;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelClientTicker;
import com.george_vi.electroenergetics.content.fuse.BlownFuseTracker;
import com.george_vi.electroenergetics.content.fuse.FuseBlockItem;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickClientHandler;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickItem;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.ClientTrainGaugeData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.content.wire.interaction.InteractDetachedNodePacket;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.WireSparkEffectTicker;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.simibubi.create.AllSoundEvents;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = CreateElectroEnergetics.ID)
public class GameEvents {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void tickClient(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;
        WireApplyingBehaviour.tick();
        BundledWireApplyingBehaviour.tick();
        WireInteractionHandler.tick();
        WireEffects.tick();
        ElectricTrainSounds.tick();
        ClientTrainGaugeData.tick();
        LinemansStickClientHandler.tick();
        FuseBlockItem.tickClient();
        ElectricalPanelClientTicker.tick();

        ElectricPropertiesOverlay.INSTANCE.ticks++;

        // Safety?
        WireInteractionHandler.preventUseOnBlockPacket = false;

        for (ClientNodeData nodeData : WireRenderer.NODE_DATA.values()) {
            nodeData.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void mouseScrolled(InputEvent.MouseScrollingEvent event) {
        double delta = event.getScrollDeltaY();
        event.setCanceled(FuseBlockItem.mouseScrolled(delta));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderHighlightBlock(RenderHighlightEvent.Block event) {
        BlockPos pos = event.getTarget().getBlockPos();
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null)
            return;

        if (WireInteractionHandler.targetedPoint != null) {
            event.setCanceled(true);
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (CEEBlocks.ACCUMULATOR.has(state))
            AccumulatorBlock.renderHighlightBlock(event, state);
        else if (CEEBlocks.ELECTRICAL_PANEL.has(state))
            ElectricalPanelClientTicker.renderHighlightBlock(event, state);
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

            BlownFuseTracker.tick();
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
    public static void tick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        LinemansStickItem.tickPlayerRange(player);
    }


    @SubscribeEvent
    public static void playerInteractItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack stack = event.getItemStack();

        // Detached Node Interactions:
        if (WireApplyingBehaviour.targetingDetachedNode != null) {
            CatnipServices.NETWORK.sendToServer(new InteractDetachedNodePacket(WireApplyingBehaviour.targetingDetachedNode));

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
        }

        // Wire interactions:
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stack, event.getEntity()))
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
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack stack = event.getItemStack();
        if (event.getLevel() instanceof ServerLevel level) {
            BlockPos pos = event.getPos();
            // Node label renaming functionality:
            if (!stack.is(CEETags.NODE_RENAME_ITEM))
                return;

            InWorldNode hoveredNode = InWorldNode.closestNode(level, event.getHitVec().getLocation(), 1.5f);
            BlockState hoveredBlockState = level.getBlockState(pos);

            if (hoveredNode == null)
                hoveredNode = InWorldNode.closestNode(level, pos, hoveredBlockState, 1.5f, event.getHitVec().getLocation());

            if (hoveredNode == null)
                return;

            InfrastructureSavedData sd = InfrastructureSavedData.load(level);

            InWorldNodeData nodeData = sd.getNodeData(hoveredNode);
            if (nodeData == null)
                return;

            String prevLabel = nodeData.label;
            nodeData.label = Optional.ofNullable(stack.get(DataComponents.CUSTOM_NAME))
                    .map(Component::getString)
                    .orElse(null);

            sd.wireSync.handleNodeLabelRename(nodeData);

            if (!Objects.equals(prevLabel, nodeData.label))
                if (nodeData.label == null)
                    AllSoundEvents.CLIPBOARD_ERASE.playOnServer(level, hoveredNode.sourcePos());
                else
                    AllSoundEvents.CLIPBOARD_CHECKMARK.playOnServer(level, hoveredNode.sourcePos());

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
            return;
        }

        // Detached Node Interactions:
        if (WireApplyingBehaviour.targetingDetachedNode != null) {
            CatnipServices.NETWORK.sendToServer(new InteractDetachedNodePacket(WireApplyingBehaviour.targetingDetachedNode));

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
        }

        // Wire interactions:
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stack, event.getEntity()))
                .findFirst().orElse(null);
        if (behaviour == null)
            return;

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
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
