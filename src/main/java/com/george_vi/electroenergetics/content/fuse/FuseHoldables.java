package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FuseHoldables {
    public static final FuseHoldable COPPER_CONDUCTOR = FuseHoldable.register(new FuseHoldable.CopperConductor(), CreateElecrtoEnergetics.rl("copper_conductor"));
    public static final FuseHoldable FUSE = FuseHoldable.register(new FuseHoldable.Fuse(), CreateElecrtoEnergetics.rl("fuse"));

    public static void register() {

    }
}
