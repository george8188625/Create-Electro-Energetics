package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlock;
import com.simibubi.create.AllCreativeModeTabs;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class CEECreativeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateElectroEnergetics.ID);

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
                        output.accept(CEEItems.HEAVILY_INSULATED_WIRE_SPOOL.asStack());
                        output.accept(CEEItems.COPPER_WIRE_SPOOL.asStack());
                        if (shouldAddElectrum())
                            output.accept(CEEItems.ELECTRUM_WIRE_SPOOL.asStack());
                        output.accept(CEEItems.IRON_WIRE_SPOOL.asStack());
                        output.accept(CEEItems.IRON_BUS_SPOOL.asStack());
                        output.accept(CEEItems.IRON_RAIL_SPOOL.asStack());
                        output.accept(CEEItems.CREATIVE_WIRE_SPOOL.asStack());
                        output.accept(CEEItems.EMPTY_SPOOL.asStack());
                        output.accept(CEEItems.INSULATED_WIRE.asStack());
                        output.accept(CEEItems.HEAVILY_INSULATED_WIRE.asStack());
                        output.accept(CEEItems.COPPER_WIRE.asStack());
                        if (shouldAddElectrum())
                            output.accept(CEEItems.ELECTRUM_WIRE.asStack());
                        output.accept(CEEItems.IRON_WIRE.asStack());
                        output.accept(CEEItems.IRON_WIRE_STRAND.asStack());
                        output.accept(CEEItems.CLAMP_METER.asStack());
                        output.accept(CEEItems.LINEMANS_STICK.asStack());
                        output.accept(CEEBlocks.CREATIVE_BATTERY.asStack());
                        output.accept(CEEBlocks.BULB.asStack());
                        output.accept(CEEBlocks.INDICATOR_BULB.asStack());
                        output.accept(CEEBlocks.MOMENTARY_SWITCH.asStack());
                        output.accept(CEEBlocks.EMERGENCY_STOP_BUTTON.asStack());
                        output.accept(CEEBlocks.CUT_OFF_SWITCH.asStack());
                        output.accept(CEEBlocks.DOUBLE_SWITCH.asStack());
                        output.accept(CEEBlocks.HV_SWITCH.asStack());
                        output.accept(CEEBlocks.REDSTONE_RELAY.asStack());
                        output.accept(CEEBlocks.RELAY.asStack());
                        output.accept(CEEBlocks.BUZZER.asStack());
                        output.accept(CEEBlocks.ENERGY_METER.asStack());
                        output.accept(CEEBlocks.TRI_POLAR_ENERGY_METER.asStack());
                        output.accept(CEEBlocks.AMMETER.asStack());
                        output.accept(CEEBlocks.VOLTMETER.asStack());
                        output.accept(CEEBlocks.FUSE.asStack());
                        output.accept(CEEBlocks.FUSE_HOLDER.asStack());
                        output.accept(CEEBlocks.ELECTRICAL_PANEL.asStack());
                        output.accept(CEEBlocks.SF6_BREAKER.asStack());
                        output.accept(CEEBlocks.GROUND_ROD.asStack());
                        output.accept(CEEBlocks.RADIATOR_PANEL.asStack());
                        output.accept(CEEBlocks.TRANSFORMER_CORE.asStack());
                        output.accept(CEEBlocks.TRANSFORMER.asStack());
                        output.accept(CEEBlocks.VOLTAGE_REGULATOR.asStack());
                        output.accept(CEEBlocks.CURRENT_TRANSFORMER.asStack());
                        output.accept(CEEFluids.TRANSFORMER_OIL.getBucket().orElseThrow());
                        output.accept(CEEFluids.PLANT_OIL.getBucket().orElseThrow());
                        for (BlockEntry<ElectricMotorBlock> e : CEEBlocks.ELECTRIC_MOTORS)
                            output.accept(e.asStack(), e == CEEBlocks.ELECTRIC_MOTORS[DyeColor.RED.ordinal()] ?
                                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS :
                                    CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);

                        output.accept(CEEBlocks.ELECTRIC_PUMP.asStack());
                        output.accept(CEEBlocks.STATOR.asStack());
                        output.accept(CEEBlocks.ALTERNATOR_ROTOR.asStack());
                        output.accept(CEEBlocks.ALTERNATOR_BRUSHES.asStack());
                        output.accept(CEEBlocks.MAGNET_BLOCK.asStack());
                        output.accept(CEEBlocks.ACCUMULATOR.asStack());
                        output.accept(CEEBlocks.HV_CAPACITOR.asStack());
                        output.accept(CEEBlocks.CONVERTER.asStack());
                        output.accept(CEEBlocks.RESISTIVE_HEATER.asStack());
                        output.accept(CEEBlocks.CONCRETE_POLE.asStack());
                        output.accept(CEEBlocks.INSULATOR.asStack());
                        output.accept(CEEBlocks.POLE_MOUNT.asStack());
                        output.accept(CEEBlocks.CATENARY_HOLDER.asStack());
                        output.accept(CEEBlocks.PANTOGRAPH.asStack());
                        output.accept(CEEBlocks.RAIL_CONTACT_SHOE.asStack());
                        output.accept(CEEBlocks.DIODE.asStack());
                        output.accept(CEEBlocks.CAPACITOR.asStack());
                        output.accept(CEEBlocks.INDUCTOR.asStack());
                        output.accept(CEEBlocks.POTENTIOMETER.asStack());
                        output.accept(CEEBlocks.RESISTOR.asStack());
                        output.accept(CEEBlocks.CREATIVE_RESISTOR.asStack());
                        if (FMLLoader.getDist().isDedicatedServer() || CEEConfigs.client().showACContent.get()) {
                            output.accept(CEEBlocks.THREE_PHASE_ALTERNATOR_BRUSHES.asStack());
                            output.accept(CEEBlocks.SYNCHROSCOPE.asStack());
                            output.accept(CEEBlocks.FREQUENCY_METER.asStack());
                        }
                        output.accept(CEEItems.TRANSFORMER_CORE_LAMINATION.asStack());
                        output.accept(CEEItems.COMMUTATOR.asStack());
                        output.accept(CEEItems.WIRE_DAMPER.asStack());
                        output.accept(CEEItems.MINIATURE_CIRCUIT_BREAKER.asStack());
                        output.accept(CEEItems.MINIATURE_MOMENTARY_SWITCH.asStack());
                        output.accept(CEEItems.MINIATURE_INDICATOR_BULB.asStack());
                        output.accept(CEEItems.MINIATURE_VOLTMETER.asStack());
                        output.accept(CEEItems.MINIATURE_AMMETER.asStack());
                        output.accept(CEEBlocks.HIGH_VOLTAGE_SIGN.asStack());
                        output.accept(CEEBlocks.ELECTRIC_SHOCK_SIGN.asStack());
                        output.accept(CEEBlocks.GROUNDING_SIGN.asStack());
                        output.accept(CEEItems.GLASS_INSULATOR_SPOOL.asStack());
                        output.accept(CEEItems.GLASS_INSULATOR_SEGMENT.asStack());
                        output.accept(CEEItems.HANGING_GLASS_INSULATION.asStack());
                    }))
                    .build());

    private static boolean shouldAddElectrum() {
        for (Holder<Item> ignored : BuiltInRegistries.ITEM.getTagOrEmpty(CEETags.ELECTRUM_NUGGET))
            return true;
        return false;
    }

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }

}
