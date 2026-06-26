package com.george_vi.electroenergetics.foundation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface HoldInteractionBehavior {
    void release();

    @OnlyIn(Dist.CLIENT)
    void tick();

    void onMouseMove(double y, double x);

    @OnlyIn(Dist.CLIENT)
    boolean isStillActive();
}
