package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CEELang extends Lang {
    public static LangBuilder builder() {
        return new LangBuilder(CreateElectroEnergetics.ID);
    }

    public static LangBuilder formatVoltage(double voltage) {
        if (Math.abs(voltage) < 0.01)
            voltage = 0;
        return builder()
                .text(String.format(Math.abs(voltage) >= 1000 || Math.abs(voltage) < 1  ? "%.2f" : "%.1f", Math.abs(voltage) >= 1000 ? voltage / 1000 : voltage))
                .translate(Math.abs(voltage) >= 1000 ? "generic.kilovolts" : "generic.volts");
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
                .text(LangNumberFormat.format(Math.abs(power) > 1000 ? power / 1000 : power))
                .translate(Math.abs(power) > 1000 ? "generic.kilowatts" : "generic.watts");
    }

    public static LangBuilder formatFEPerTick(double fet) {
        return builder()
                .text(LangNumberFormat.format(Math.abs(fet) > 1000 ? fet / 1000 : fet))
                .translate(Math.abs(fet) > 1000 ? "generic.kilofe_per_tick" : "generic.fe_per_tick");
    }

    public static LangBuilder formatEnergy(double energy) {
        if (Math.abs(energy) >= 1000000)
            return builder()
                    .text(String.format("%.1f", energy / 1000000))
                    .translate("generic.megawatthours");

        if (Math.abs(energy) >= 1000)
            return builder()
                    .text(String.format("%.1f", energy / 1000))
                    .translate("generic.kilowatthours");

        return builder()
                .text(String.format("%.1f", energy))
                .translate("generic.watthours");
    }

    public static LangBuilder formatResistance(double resistance) {
        if (Math.abs(resistance) >= 1000000)
            return builder()
                    .text(String.format("%.1f", resistance / 1000000))
                    .translate("generic.megaohms");

        if (Math.abs(resistance) >= 1000)
            return builder()
                    .text(String.format("%.1f", resistance / 1000))
                    .translate("generic.kiloohms");

        if (Math.abs(resistance) >= 1)
            return builder()
                    .text(String.format("%.1f", resistance))
                    .translate("generic.ohms");

        return builder()
                .text(String.format("%.1f", resistance * 1000))
                .translate("generic.milliohms");
    }

    public static LangBuilder formatResistancePerMeter(double resistance) {
        if (Math.abs(resistance) >= 1000000)
            return builder()
                    .text(String.format("%.1f", resistance / 1000000))
                    .translate("generic.megaohms_per_meter");

        if (Math.abs(resistance) >= 1000)
            return builder()
                    .text(String.format("%.1f", resistance / 1000))
                    .translate("generic.kiloohms_per_meter");

        if (Math.abs(resistance) >= 1)
            return builder()
                    .text(String.format("%.1f", resistance))
                    .translate("generic.ohms_per_meter");

        return builder()
                .text(String.format("%.1f", resistance * 1000))
                .translate("generic.milliohms_per_meter");
    }

    public static LangBuilder formatCapacitance(double capacitance) {
        if (Math.abs(capacitance) >= 1)
            return builder()
                    .text(String.format("%.1f", capacitance))
                    .translate("generic.farads");

        if (Math.abs(capacitance) >= 1e-3d)
            return builder()
                    .text(String.format("%.2f", capacitance * 1_000))
                    .translate("generic.millifarads");

        if (Math.abs(capacitance) >= 1e-6d)
            return builder()
                    .text(String.format("%.2f", capacitance * 1_000_000))
                    .translate("generic.microfarads");

        return builder()
                .text(String.format("%.1f", capacitance * 1_000_000_000))
                .translate("generic.nanofarads");
    }

    public static LangBuilder formatInductance(double capacitance) {
        if (Math.abs(capacitance) >= 1)
            return builder()
                    .text(String.format("%.1f", capacitance))
                    .translate("generic.henry");

        if (Math.abs(capacitance) >= 1e-3d)
            return builder()
                    .text(String.format("%.2f", capacitance * 1_000))
                    .translate("generic.millihenry");

        if (Math.abs(capacitance) >= 1e-6d)
            return builder()
                    .text(String.format("%.2f", capacitance * 1_000_000))
                    .translate("generic.microhenry");

        return builder()
                .text(String.format("%.1f", capacitance * 1_000_000_000))
                .translate("generic.nanohenry");
    }

    public static LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    public static MutableComponent nodeLabel(String key, Object... args) {
        return Component.translatable("electroenergetics.nodes." + key, args);
    }
}
