package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.foundation.DielectricMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CreateElectroEnergetics.ID);

    public static final Holder<MobEffect> DIELECTRIC = MOB_EFFECTS.register("dielectric", () -> new DielectricMobEffect(
            MobEffectCategory.BENEFICIAL,
            0xF5D933
    ));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
