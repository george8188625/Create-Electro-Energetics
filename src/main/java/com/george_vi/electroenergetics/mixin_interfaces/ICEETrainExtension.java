package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public interface ICEETrainExtension {
    ElectricTrainSoundType getSoundType();

    void setSoundType(ElectricTrainSoundType type);

    Set<TrainSoundModifier> getSoundModifyingBlocks();

    int getAccumulators();

    void setAccumulators(int value);

    double getAccumulatorCharge();

    void setAccumulatorCharge(double value);

}
