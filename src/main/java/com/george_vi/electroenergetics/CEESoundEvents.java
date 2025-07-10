package com.george_vi.electroenergetics;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CEESoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CreateElecrtoEnergetics.ID);
    public static final Supplier<SoundEvent> HUM = SOUND_EVENTS.register("hum", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("hum")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
