package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.DCElectricTrainSoundBehaviour;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.IGBTElectricTrainSoundBehaviour;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ModernElectricTrainSoundBehaviour;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class CEEElectricTrainSoundTypes {
    private static final DeferredRegister<ElectricTrainSoundType> ELECTRIC_TRAIN_SOUND_TYPE =
            DeferredRegister.create(CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE, CreateElectroEnergetics.ID);

    public static final DeferredHolder<ElectricTrainSoundType, ElectricTrainSoundType> MODERN = ELECTRIC_TRAIN_SOUND_TYPE.register("modern", () -> new ElectricTrainSoundType(() -> ModernElectricTrainSoundBehaviour::new, CreateElectroEnergetics.rl("modern"), Object2IntMaps::emptyMap));
    public static final DeferredHolder<ElectricTrainSoundType, ElectricTrainSoundType> DC = ELECTRIC_TRAIN_SOUND_TYPE.register("dc", () -> new ElectricTrainSoundType(() -> DCElectricTrainSoundBehaviour::new, CreateElectroEnergetics.rl("dc"), Suppliers.memoize(() -> Object2IntMaps.singleton(CEEBlocks.ALTERNATOR_BRUSHES.get(), 10))));
    public static final DeferredHolder<ElectricTrainSoundType, ElectricTrainSoundType> IGBT = ELECTRIC_TRAIN_SOUND_TYPE.register("igbt", () -> new ElectricTrainSoundType(() -> IGBTElectricTrainSoundBehaviour::new, CreateElectroEnergetics.rl("igbt"), Suppliers.memoize(() -> Object2IntMaps.singleton(CEEBlocks.MAGNET_BLOCK.get(), 5))));

    public static void register(IEventBus bus) {
        ELECTRIC_TRAIN_SOUND_TYPE.register(bus);
    }

}
