package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEWireTypes {
    private static final DeferredRegister<WireType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireType, WireType> STANDARD = WIRE_TYPES.register("standard", () -> new WireType(CEEConfigs.server().resistanceValues.wireResistance::get, CEEPartialModels.WIRE_SEGMENT, CEEItems.INSULATED_WIRE, CEEItems.WIRE_SPOOL::get, () -> 3540));
    public static final DeferredHolder<WireType, WireType> CREATIVE = WIRE_TYPES.register("creative", () -> new WireType(() -> 0.00001d, CEEPartialModels.CREATIVE_WIRE_SEGMENT, () -> Items.AIR, CEEItems.CREATIVE_WIRE_SPOOL::get, () -> 9999999));

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }
}
