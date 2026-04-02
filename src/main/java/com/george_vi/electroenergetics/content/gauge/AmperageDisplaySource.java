package com.george_vi.electroenergetics.content.gauge;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.List;

public class AmperageDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof ElectricGaugeBlockEntity be) || be.voltmeter)
            return ZERO.copy();
        return CEELang.formatAmperage(Math.abs(be.voltage / 0.01d) * be.scaling.getScale()).component();
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
