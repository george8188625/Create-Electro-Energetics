package com.george_vi.electroenergetics;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEECreativeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = REGISTER.register("base",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.electroenergetics"))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                    .icon(CEEBlocks.CONNECTOR::asStack)
                    .displayItems(((parameters, output) -> {
                        output.accept(CEEBlocks.CONNECTOR.asStack());
                        output.accept(CEEBlocks.DOUBLE_CONNECTOR.asStack());
                        output.accept(CEEBlocks.TRIPLE_CONNECTOR.asStack());
                        output.accept(CEEBlocks.QUAD_CONNECTOR.asStack());
                        output.accept(CEEItems.WIRE_SPOOL.asStack());
                        output.accept(CEEItems.EMPTY_SPOOL.asStack());
                        output.accept(CEEItems.INSULATED_WIRE.asStack());
                        output.accept(CEEItems.COPPER_WIRE.asStack());
                        output.accept(CEEItems.CLAMP_METER.asStack());
                        output.accept(CEEBlocks.CREATIVE_BATTERY.asStack());
                        output.accept(CEEBlocks.BULB.asStack());
                        output.accept(CEEBlocks.CUT_OFF_SWITCH.asStack());
                        output.accept(CEEBlocks.DOUBLE_SWITCH.asStack());
                        output.accept(CEEBlocks.HV_SWITCH.asStack());
                        output.accept(CEEBlocks.REDSTONE_RELAY.asStack());
                        output.accept(CEEBlocks.ENERGY_METER.asStack());
                        output.accept(CEEBlocks.TRI_POLAR_ENERGY_METER.asStack());
                        output.accept(CEEBlocks.AMMETER.asStack());
                        output.accept(CEEBlocks.VOLTMETER.asStack());
                        output.accept(CEEBlocks.FUSE.asStack());
                        output.accept(CEEBlocks.GROUND_ROD.asStack());
                        output.accept(CEEBlocks.TRANSFORMER.asStack());
                        output.accept(CEEBlocks.VOLTAGE_REGULATOR.asStack());
                        output.accept(CEEBlocks.ELECTRIC_MOTOR.asStack());
                        output.accept(CEEBlocks.ELECTRIC_PUMP.asStack());
                        output.accept(CEEBlocks.ALTERNATOR_ROTOR.asStack());
                        output.accept(CEEBlocks.ALTERNATOR_BRUSHES.asStack());
                        output.accept(CEEBlocks.MAGNET_BLOCK.asStack());
                        output.accept(CEEBlocks.ACCUMULATOR.asStack());
                        output.accept(CEEBlocks.CONVERTER.asStack());
                        output.accept(CEEBlocks.CONCRETE_POLE.asStack());
                        output.accept(CEEBlocks.POLE_MOUNT.asStack());
                        output.accept(CEEBlocks.CATENARY_HOLDER.asStack());
                        output.accept(CEEBlocks.PANTOGRAPH.asStack());
                    }))
                    .build());

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }

}
