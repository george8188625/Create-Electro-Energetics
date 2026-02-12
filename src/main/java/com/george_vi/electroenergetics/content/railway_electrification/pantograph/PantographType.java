package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

public final class PantographType {
    public final float reach;
    public final float backOffset;
    public final float topOffset;
    public final float sidewaysReach;

    public PantographType(float reach, float backOffset, float topOffset, float sidewaysReach) {
        this.reach = reach;
        this.backOffset = backOffset;
        this.topOffset = topOffset;
        this.sidewaysReach = sidewaysReach;
    }
}
