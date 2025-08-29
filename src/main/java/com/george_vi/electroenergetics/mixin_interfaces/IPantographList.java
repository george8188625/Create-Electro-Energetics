package com.george_vi.electroenergetics.mixin_interfaces;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;

import java.util.List;

public interface IPantographList {
    void setPantographList(List<Pair<BlockPos, Boolean>> newPantographList);
    List<Pair<BlockPos, Boolean>> getPantographList();
}
