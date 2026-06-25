package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface HoldInteractionBehavior {
    void release();

    @OnlyIn(Dist.CLIENT)
    void tick();

    void onMouseMove(double x, double y);

    @OnlyIn(Dist.CLIENT)
    boolean isStillActive();
}
