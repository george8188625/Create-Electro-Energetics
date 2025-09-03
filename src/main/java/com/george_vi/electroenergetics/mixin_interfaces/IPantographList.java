package com.george_vi.electroenergetics.mixin_interfaces;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;

import java.util.List;

public interface IPantographList {
    void setPantographList(List<TrainPantographEntry> newPantographList);

    List<TrainPantographEntry> getPantographList();

    default void changePantographState(BlockPos localPos, boolean active) {
        List<TrainPantographEntry> pantographList = getPantographList();

        for (int i = 0; i < pantographList.size(); i++) {
            TrainPantographEntry e = pantographList.get(i);
            if (e.originalPos().equals(localPos)) {
                pantographList.set(i, new TrainPantographEntry(e.originalPos(), e.rotatedPos(), active, e.facingForward()));
                break;
            }
        }
    }

    default TrainPantographEntry getPantographState(BlockPos localPos) {
        return getPantographList().stream().filter(e -> e.originalPos().equals(localPos)).findFirst().orElse(null);
    }
}
