package com.george_vi.electroenergetics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CEESoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CreateElecrtoEnergetics.ID);
    public static final Supplier<SoundEvent> HUM = SOUND_EVENTS.register("hum", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("hum")));
    public static final Supplier<SoundEvent> SHORT_ARC = SOUND_EVENTS.register("short_arc", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("short_arc")));
    public static final Supplier<SoundEvent> ARC = SOUND_EVENTS.register("arc", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("arc")));
    public static final Supplier<SoundEvent> ELECTRIC_TRAIN = SOUND_EVENTS.register("electric_train", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("electric_train")));
    public static final Supplier<SoundEvent> ELECTRIC_TRAIN_BACKGROUND = SOUND_EVENTS.register("electric_train_background", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("electric_train_background")));
    public static final Supplier<SoundEvent> BUZZER = SOUND_EVENTS.register("buzzer", () -> SoundEvent.createVariableRangeEvent(CreateElecrtoEnergetics.rl("buzzer")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
