package com.george_vi.electroenergetics;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

@Mod(value = CreateElectroEnergetics.ID, dist = Dist.CLIENT)
public class CEEClient {
    public CEEClient(IEventBus modEventBus) {
        modEventBus.addListener(CEEClient::onLoadComplete);
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        ModContainer container = ModList.get()
                .getModContainerById(CreateElectroEnergetics.ID)
                .orElseThrow(() -> new IllegalStateException("Create: Electro Energetics mod container missing on LoadComplete"));
        Supplier<IConfigScreenFactory> configScreen = () -> (mc, previousScreen) -> new BaseConfigScreen(previousScreen, CreateElectroEnergetics.ID);
        container.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
    }
}
