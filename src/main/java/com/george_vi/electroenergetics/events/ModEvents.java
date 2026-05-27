package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterRenderer;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorBlock;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickRenderer;
import com.george_vi.electroenergetics.events.datagen.*;
import com.george_vi.electroenergetics.ponder.CEEPonderPlugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import com.simibubi.create.content.equipment.wrench.WrenchItemRenderer;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.simibubi.create.Create.asResource;

@EventBusSubscriber(modid = CreateElectroEnergetics.ID)
public class ModEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void gatherDataHighPriority(GatherDataEvent event) {
        if (!event.getMods().contains(CreateElectroEnergetics.ID))
            return;

        CreateElectroEnergetics.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {

            JsonElement jsonElement = FilesHelper.loadJsonResource("assets/electroenergetics/lang/default.json");
            if (jsonElement == null)
                throw new IllegalStateException(String.format("Could not find default lang file"));

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                provider.add(entry.getKey(), entry.getValue().getAsString());
            PonderIndex.addPlugin(new CEEPonderPlugin());
            PonderIndex.getLangAccess().provideLang(CreateElectroEnergetics.ID, provider::add);
            CEEAdvancements.provideLang(provider::add);

        });
    }

    public static final AtomicBoolean changedConfigs = new AtomicBoolean(false);

    @SubscribeEvent
    public static void reloadModConfig(ModConfigEvent.Reloading event) {
        changedConfigs.set(true);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (!event.getMods().contains(CreateElectroEnergetics.ID))
            return;

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        CEEGeneratedEntriesProvider generatedEntriesProvider = new CEEGeneratedEntriesProvider(packOutput, lookupProvider);
        generator.addProvider(event.includeServer(), generatedEntriesProvider);
        generator.addProvider(event.includeServer(), new CEERecipeGen(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new CEEMixingRecipeGen(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new CEECompactingRecipeGen(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new CEEMechanicalCraftingRecipeGen(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new CEEAdvancements(packOutput, lookupProvider));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CEEPonderPlugin());
        ItemBlockRenderTypes.setRenderLayer(CEEFluids.TRANSFORMER_OIL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CEEFluids.TRANSFORMER_OIL.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CEEFluids.PLANT_OIL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CEEFluids.PLANT_OIL.getSource(), RenderType.translucent());
        RadialWrenchMenu.registerRotationProperty(DoubleConnectorBlock.ROLL, "Roll");
        RadialWrenchMenu.registerRotationProperty(DoubleConnectorBlock.STYLE, "Style");
        RadialWrenchMenu.registerRotationProperty(ConnectorBlock.STYLE, "Style");
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(SimpleCustomRenderer.create(CEEItems.LINEMANS_STICK.get(), new LinemansStickRenderer()), CEEItems.LINEMANS_STICK);
        event.registerItem(SimpleCustomRenderer.create(CEEItems.CLAMP_METER.get(), new ClampMeterRenderer()), CEEItems.CLAMP_METER);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, CreateElectroEnergetics.rl("electric_properties_overlay"), ElectricPropertiesOverlay.INSTANCE);
    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        event.register(CEERegistries.WIRE_TYPE);
        event.register(CEERegistries.WIRE_ATTACHMENT_TYPE);
        event.register(CEERegistries.WIRE_INTERACTION_BEHAVIOUR);
        event.register(CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE);
        event.register(CEERegistries.PANTOGRAPH_TYPE);
        event.register(CEERegistries.SIMULATED_DEVICE_FEATURE_TYPE);
        event.register(CEERegistries.SIMULATED_DEVICE_TYPE);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
            CEEAdvancements.registerTrigger();
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ElectricGaugeBlockEntity.registerCapabilities(event);
    }
}
