package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.energy_meter.ActivePowerDisplaySource;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterDisplaySource;
import com.george_vi.electroenergetics.content.gauge.AmperageDisplaySource;
import com.george_vi.electroenergetics.content.gauge.VoltageDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

import static com.george_vi.electroenergetics.CreateElecrtoEnergetics.REGISTRATE;

public class CEEDisplaySources {
    public static final RegistryEntry<DisplaySource, EnergyMeterDisplaySource> ENERGY_METER = simple("energy_meter", EnergyMeterDisplaySource::new);
    public static final RegistryEntry<DisplaySource, ActivePowerDisplaySource> WATTMETER = simple("wattmeter", ActivePowerDisplaySource::new);
    public static final RegistryEntry<DisplaySource, VoltageDisplaySource> VOLTAGE = simple("voltage", VoltageDisplaySource::new);
    public static final RegistryEntry<DisplaySource, AmperageDisplaySource> AMPERAGE = simple("amperage", AmperageDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(String name, Supplier<T> supplier) {
        return REGISTRATE.displaySource(name, supplier).register();
    }

    public static void register() {

    }
}
