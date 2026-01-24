package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;

import java.util.Set;

public interface ICEETrainExtension {
    ElectricTrainSoundType getSoundType();

    void setSoundType(ElectricTrainSoundType type);

    Set<TrainSoundModifier> getSoundModifyingBlocks();

    ElectricTrainData getElectricTrainData();
}
