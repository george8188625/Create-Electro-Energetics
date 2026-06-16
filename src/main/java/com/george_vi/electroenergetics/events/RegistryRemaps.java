package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber
public class RegistryRemaps {

    @SubscribeEvent
    public static void remap(RegisterEvent event) {
        Registry<?> registry = event.getRegistry();

        if (registry == BuiltInRegistries.BLOCK || registry == BuiltInRegistries.ITEM) {
            registry.addAlias(CreateElectroEnergetics.rl("electric_motor"), CreateElectroEnergetics.rl("red_electric_motor"));
        }

        if (registry == CEERegistries.PANEL_ATTACHMENT_TYPE) {
            registry.addAlias(CreateElectroEnergetics.rl("miniature_ammeter"), CreateElectroEnergetics.rl("ammeter"));
            registry.addAlias(CreateElectroEnergetics.rl("miniature_voltmeter"), CreateElectroEnergetics.rl("voltmeter"));
            registry.addAlias(CreateElectroEnergetics.rl("miniature_indicator_bulb"), CreateElectroEnergetics.rl("indicator_bulb"));
            registry.addAlias(CreateElectroEnergetics.rl("miniature_momentary_switch"), CreateElectroEnergetics.rl("momentary_switch"));
        }

    }
}
