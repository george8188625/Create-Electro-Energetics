package com.george_vi.electroenergetics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CEESoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CreateElectroEnergetics.ID);
    public static final Supplier<SoundEvent> HUM = sound("hum");
    public static final Supplier<SoundEvent> SHORT_ARC = sound("short_arc");
    public static final Supplier<SoundEvent> ARC = sound("arc");
    public static final Supplier<SoundEvent> VOLTAGE_REGULATOR = sound("voltage_regulator");
    public static final Supplier<SoundEvent> ELECTRIC_TRAIN = sound("electric_train");
    public static final Supplier<SoundEvent> ELECTRIC_TRAIN_BACKGROUND = sound("electric_train_background");
    public static final Supplier<SoundEvent> TRAIN_RELAY = sound("train_relay");
    public static final Supplier<SoundEvent> DC_TRAIN = sound("dc_train");
    public static final Supplier<SoundEvent> DC_TRAIN_START = sound("dc_train_start");
    public static final Supplier<SoundEvent> BUZZER = sound("buzzer");

    public static final Supplier<SoundEvent> TRAIN_WIND_RISE = sound("train/background/wind_rise");
    public static final Supplier<SoundEvent> TRAIN_WIND_STATIC = sound("train/background/wind_static");
    public static final Supplier<SoundEvent> TRAIN_GTO_ASYNC_DECAY = sound("train/gto/async_decay");
    public static final Supplier<SoundEvent> TRAIN_GTO_ASYNC_RISE = sound("train/gto/async_rise");
    public static final Supplier<SoundEvent> TRAIN_GTO_ASYNC = sound("train/gto/async");
    public static final Supplier<SoundEvent> TRAIN_GTO_P1 = sound("train/gto/p1");
    public static final Supplier<SoundEvent> TRAIN_GTO_P3 = sound("train/gto/p3");
    public static final Supplier<SoundEvent> TRAIN_GTO_P5 = sound("train/gto/p5");
    public static final Supplier<SoundEvent> TRAIN_GTO_P7 = sound("train/gto/p7");
    public static final Supplier<SoundEvent> TRAIN_GTO_P9 = sound("train/gto/p9");
    public static final Supplier<SoundEvent> TRAIN_GTO_P11 = sound("train/gto/p11");
    public static final Supplier<SoundEvent> TRAIN_GTO_P15 = sound("train/gto/p15");

    private static @NotNull DeferredHolder<SoundEvent, SoundEvent> sound(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(CreateElectroEnergetics.rl(path)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
