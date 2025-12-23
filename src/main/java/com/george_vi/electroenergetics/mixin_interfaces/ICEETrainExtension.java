package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;

public interface ICEETrainExtension {
    ElectricTrainSoundType getSoundType();

    void setSoundType(ElectricTrainSoundType type);

}
