package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.simibubi.create.Create;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class CEELang extends Lang {
    public static LangBuilder builder() {
        return new LangBuilder(CreateElecrtoEnergetics.ID);
    }

    public static LangBuilder formatVoltage(double voltage) {
        if (Math.abs(voltage) < 0.01)
            voltage = 0;
        return builder()
                .text(String.format("%.1f", Math.abs(voltage) > 1000 ? voltage / 1000 : voltage))
                .translate(Math.abs(voltage) > 1000 ? "generic.kilovolts" : "generic.volts");
    }

    public static LangBuilder formatAmperage(double amperage) {
        if (Math.abs(amperage) < 0.01)
            amperage = 0;
        return builder()
                .text(String.format("%.1f", amperage))
                .translate("generic.amps");
    }

    public static LangBuilder formatPower(double power) {
        return builder()
                .text(String.format("%.1f", Math.abs(power) > 1000 ? power / 1000 : power))
                .translate(Math.abs(power) > 1000 ? "generic.kilowatts" : "generic.watts");
    }
}
