package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHeldFuse;
import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHeldIndicatorBulb;
import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHeldSwitch;
import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHoldable;

public class FuseHoldables {
    public static final FuseHoldable COPPER_CONDUCTOR = FuseHoldable.register(new FuseHoldable.CopperConductor(), CreateElectroEnergetics.rl("copper_conductor"));
    public static final FuseHoldable FUSE = FuseHoldable.register(new FuseHeldFuse(), CreateElectroEnergetics.rl("fuse"));
    public static final FuseHoldable CUT_OFF_SWITCH = FuseHoldable.register(new FuseHeldSwitch(), CreateElectroEnergetics.rl("cut_off_switch"));
    public static final FuseHoldable INDICATOR_BULB = FuseHoldable.register(new FuseHeldIndicatorBulb(), CreateElectroEnergetics.rl("indicator_bulb"));

    public static void register() {

    }
}
