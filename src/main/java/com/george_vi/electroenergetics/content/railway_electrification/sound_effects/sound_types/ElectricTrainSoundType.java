package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class ElectricTrainSoundType {
    @OnlyIn(Dist.CLIENT)
    public Supplier<ElectricTrainSoundBehaviour> soundBehaviour;

    public ElectricTrainSoundType(Supplier<Supplier<ElectricTrainSoundBehaviour>> soundBehaviour) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.soundBehaviour = soundBehaviour.get());
    }

}
