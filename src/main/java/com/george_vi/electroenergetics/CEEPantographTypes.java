package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEPantographTypes {
    private static final DeferredRegister<PantographType> PANTOGRAPH_TYPE =
            DeferredRegister.create(CEERegistries.PANTOGRAPH_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<PantographType, PantographType> STANDARD = PANTOGRAPH_TYPE.register("standard", () -> new PantographType(3.25f, 0.25f, 0.5f, 2f));
    public static final DeferredHolder<PantographType, PantographType> DOUBLE = PANTOGRAPH_TYPE.register("double", () -> new PantographType(3.25f, 0.5f, 0.5f, 2f));
    public static final DeferredHolder<PantographType, PantographType> RAIL_CONTACT_SHOE = PANTOGRAPH_TYPE.register("rail_contact_shoe", () -> new PantographType(1f, 0, -0.75f, 0.5f));

    public static void register(IEventBus bus) {
        PANTOGRAPH_TYPE.register(bus);
    }
}
