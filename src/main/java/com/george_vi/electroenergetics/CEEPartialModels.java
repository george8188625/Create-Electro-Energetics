package com.george_vi.electroenergetics;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class CEEPartialModels {
    public static final PartialModel WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/wire_segment"));
    public static final PartialModel COPPER_WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/copper_wire_segment"));
    public static final PartialModel ELECTRUM_WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/electrum_wire_segment"));
    public static final PartialModel CREATIVE_WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/creative_wire_segment"));
    public static final PartialModel IRON_WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/iron_wire_segment"));
    public static final PartialModel IRON_BUS_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/iron_bus_segment"));
    public static final PartialModel IRON_RAIL_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/iron_rail_segment"));
    public static final PartialModel HEAVILY_INSULATED_WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/heavily_insulated_wire_segment"));
    public static final PartialModel ROTOR = PartialModel.of(CreateElectroEnergetics.rl("block/alternator_rotor/block"));
    public static final PartialModel INSULATOR = PartialModel.of(CreateElectroEnergetics.rl("block/connector/insulator"));
    public static final PartialModel ATTACHMENT_CHAIN = PartialModel.of(CreateElectroEnergetics.rl("block/wire_attachments/attachment_chain"));
    public static final PartialModel HV_SWITCH_ARM = PartialModel.of(CreateElectroEnergetics.rl("block/high_voltage_switch/arm"));
    public static final PartialModel HV_SWITCH_PIVOT = PartialModel.of(CreateElectroEnergetics.rl("block/high_voltage_switch/pivot"));
    public static final PartialModel PANTOGRAPH_BASE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/base"));
    public static final PartialModel PANTOGRAPH_UPPER_ARM = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/upper_arm"));
    public static final PartialModel PANTOGRAPH_UPPER_ARM_ARM = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/upper_arm_arm"));
    public static final PartialModel PANTOGRAPH_LOWER_ARM = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/lower_arm"));
    public static final PartialModel PANTOGRAPH_BASE_DOUBLE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/base_double"));
    public static final PartialModel PANTOGRAPH_UPPER_ARMS_DOUBLE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/upper_arms_double"));
    public static final PartialModel PANTOGRAPH_LOWER_ARMS_DOUBLE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/lower_arms_double"));
    public static final PartialModel PANTOGRAPH_CONNECTING_SURFACE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/connecting_surface"));
    public static final PartialModel PANTOGRAPH_CONNECTING_SURFACE_DOUBLE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/connecting_surface_double"));
    public static final PartialModel PANTOGRAPH_CONNECTING_ROD = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/connecting_rod"));
    public static final PartialModel PANTOGRAPH_SPRINGS = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/springs"));
    public static final PartialModel PANTOGRAPH_SPRINGS_DOUBLE = PartialModel.of(CreateElectroEnergetics.rl("block/pantograph/springs_double"));
    public static final PartialModel RAIL_CONTACT_SHOE_CONTACT = PartialModel.of(CreateElectroEnergetics.rl("block/rail_contact_shoe/contact_shoe"));
    public static final PartialModel RAIL_CONTACT_SHOE_HINGES = PartialModel.of(CreateElectroEnergetics.rl("block/rail_contact_shoe/hinges"));
    public static final PartialModel CATENARY_HOLDER_INSULATOR = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/insulator"));
    public static final PartialModel CATENARY_HOLDER_LONG_ROD = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/long_rod"));
    public static final PartialModel CATENARY_HOLDER_SHORT_ROD = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/short_rod"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_4 = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/mount_4"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_6 = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/mount_6"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_8 = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/mount_8"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_10 = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/mount_10"));
    public static final PartialModel CATENARY_HOLDER_CONNECTOR = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/connector"));
    public static final PartialModel CATENARY_HOLDER_INSULATOR_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_insulator"));
    public static final PartialModel CATENARY_HOLDER_LONG_ROD_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_long_rod"));
    public static final PartialModel CATENARY_HOLDER_SHORT_ROD_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_short_rod"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_4_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_mount_4"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_6_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_mount_6"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_8_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_mount_8"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_10_WEATHERED = PartialModel.of(CreateElectroEnergetics.rl("block/catenary_holder/weathered_mount_10"));
    public static final PartialModel BULB_FILAMENT = PartialModel.of(CreateElectroEnergetics.rl("block/bulb/filament"));
    public static final PartialModel BULB_FILAMENT_BRIGHT = PartialModel.of(CreateElectroEnergetics.rl("block/bulb/filament_bright"));
    public static final PartialModel BULB_BROKEN_FILAMENT = PartialModel.of(CreateElectroEnergetics.rl("block/bulb/filament_broken"));
    public static final PartialModel BULB_GLASS = PartialModel.of(CreateElectroEnergetics.rl("block/bulb/glass"));
    public static final PartialModel RESISTOR_STRIP = PartialModel.of(CreateElectroEnergetics.rl("block/electronics/resistor_strip"));
    public static final PartialModel FUSE_HOLDER_COPPER_CONDUCTOR = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/copper_conductor"));
    public static final PartialModel FUSE_HOLDER_FUSE = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/fuse"));
    public static final PartialModel FUSE_HOLDER_BROKEN_FUSE = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/broken_fuse"));
    public static final PartialModel FUSE_HOLDER_SWITCH = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/switch"));
    public static final PartialModel FUSE_HOLDER_SWITCH_CLOSED = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/switch_closed"));
    public static final PartialModel FUSE_HOLDER_INDICATOR_BULB = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/indicator_bulb"));
    public static final PartialModel INDICATOR_BULB_GLOW = PartialModel.of(CreateElectroEnergetics.rl("block/indicator_bulb/glow"));
    public static final PartialModel INDICATOR_BULB_TUBE = PartialModel.of(CreateElectroEnergetics.rl("block/indicator_bulb/tube"));
    public static final PartialModel INDICATOR_BULB_CUBE = PartialModel.of(CreateElectroEnergetics.rl("block/indicator_bulb/cube"));
    public static final PartialModel WIRE_DAMPER_ATTACHMENT = PartialModel.of(CreateElectroEnergetics.rl("block/wire_damper_attachment"));
    public static final PartialModel POTENTIOMETER_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/potentiometer/dial"));
    public static final PartialModel RESISTIVE_HEATER_HEATING_ELEMENT = PartialModel.of(CreateElectroEnergetics.rl("block/resistive_heater/heating_element"));
    public static final PartialModel RESISTIVE_HEATER_HEATING_ELEMENT_GLOW = PartialModel.of(CreateElectroEnergetics.rl("block/resistive_heater/heating_element_glow"));
    public static final PartialModel SYNCHROSCOPE_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/synchroscope/dial"));
    public static final PartialModel VARISTOR = PartialModel.of(CreateElectroEnergetics.rl("block/fuse_holder/varistor"));

    public static final Map<DyeColor, PartialModel> COLORED_WIRE_SEGMENTS = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_WIRE_SEGMENTS.put(color,
                    PartialModel.of(CreateElectroEnergetics.rl("block/colored_wire/" + color.getName())));
        }
    }

    public static void register() {

    }
}
