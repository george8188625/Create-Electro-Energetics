package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorBlock;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.events.datagen.CEEGeneratedEntriesProvider;
import com.george_vi.electroenergetics.events.datagen.CEERecipeGen;
import com.george_vi.electroenergetics.ponder.CEEPonderPlugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        });
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
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CEEPonderPlugin());

        RadialWrenchMenu.registerRotationProperty(DoubleConnectorBlock.ROLL, "Roll");
        RadialWrenchMenu.registerRotationProperty(DoubleConnectorBlock.STYLE, "Style");
        RadialWrenchMenu.registerRotationProperty(ConnectorBlock.STYLE, "Style");
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
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ElectricGaugeBlockEntity.registerCapabilities(event);
    }
}
