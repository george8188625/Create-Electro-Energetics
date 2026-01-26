package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEPantographTypes {
    private static final DeferredRegister<PantographType> PANTOGRAPH_TYPE =
            DeferredRegister.create(CEERegistries.PANTOGRAPH_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<PantographType, PantographType> STANDARD = PANTOGRAPH_TYPE.register("standard", () -> new PantographType(3f));
    public static final DeferredHolder<PantographType, PantographType> DOUBLE = PANTOGRAPH_TYPE.register("double", () -> new PantographType(3.5f));

    public static void register(IEventBus bus) {
        PANTOGRAPH_TYPE.register(bus);
    }
}
