package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ModernElectricTrainSoundBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEElectricTrainSoundTypes {
    private static final DeferredRegister<ElectricTrainSoundType> ELECTRIC_TRAIN_SOUND_TYPE =
            DeferredRegister.create(CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<ElectricTrainSoundType, ElectricTrainSoundType> MODERN = ELECTRIC_TRAIN_SOUND_TYPE.register("modern", () -> new ElectricTrainSoundType(() -> ModernElectricTrainSoundBehaviour::new));

    public static void register(IEventBus bus) {
        ELECTRIC_TRAIN_SOUND_TYPE.register(bus);
    }

}
