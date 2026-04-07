package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Set;

public interface IPantographList {
    void electroEnergetics$setPantographList(List<TrainPantographEntry> newPantographList);

    List<TrainPantographEntry> electroEnergetics$getPantographList();

    Set<TrainSoundModifier> electroEnergetics$getSoundModifyingBlocks();

    int electroEnergetics$getAccumulators();

    void electroEnergetics$setAccumulators(int value);

    boolean electroEnergetics$hasCreativeElectricalSource();


    default void changePantographState(BlockPos localPos, boolean active) {
        List<TrainPantographEntry> pantographList = electroEnergetics$getPantographList();

        for (int i = 0; i < pantographList.size(); i++) {
            TrainPantographEntry e = pantographList.get(i);
            if (e.originalPos.equals(localPos)) {
                e.active = active;
                break;
            }
        }
    }

    default TrainPantographEntry getPantographState(BlockPos localPos) {
        return electroEnergetics$getPantographList().stream().filter(e -> e.originalPos.equals(localPos)).findFirst().orElse(null);
    }

    boolean electroEnergetics$hasElectricMotor();

    void electroEnergetics$setElectricMotor(boolean v);

}
