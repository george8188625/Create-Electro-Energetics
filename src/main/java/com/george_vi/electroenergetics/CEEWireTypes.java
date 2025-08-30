package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.WireType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEWireTypes {
    private static final DeferredRegister<WireType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireType, WireType> STANDARD = WIRE_TYPES.register("standard", () -> new WireType(CEEConfigs.server().resistanceValues.wireResistance::get, CEEPartialModels.WIRE_SEGMENT));

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }
}
