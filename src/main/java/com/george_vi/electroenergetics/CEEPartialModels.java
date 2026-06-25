package com.george_vi.electroenergetics;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.item.DyeColor;

public class CEEPartialModels {
    public static final PartialModel WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/wire_segment"));
    public static final PartialModel HANGING_INSULATOR = PartialModel.of(CreateElectroEnergetics.rl("block/hanging_insulator/base"));
    public static final PartialModel HANGING_INSULATOR_ENDPOINT = PartialModel.of(CreateElectroEnergetics.rl("block/hanging_insulator/chain"));
    public static final PartialModel DUPLEX_WIRE_SEGMENT = PartialModel.of(CreateElectroEnergetics.rl("block/duplex_wire_segment"));
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
    public static final PartialModel POTENTIOMETER_SHAFT = PartialModel.of(CreateElectroEnergetics.rl("block/potentiometer/shaft"));
    public static final PartialModel REDSTONE_POTENTIOMETER_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/redstone_potentiometer/dial"));
    public static final PartialModel REDSTONE_POTENTIOMETER_REDSTONE = PartialModel.of(CreateElectroEnergetics.rl("block/redstone_potentiometer/redstone"));
    public static final PartialModel VARIAC_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/variac/dial"));
    public static final PartialModel VARIAC_SHAFT = PartialModel.of(CreateElectroEnergetics.rl("block/variac/shaft"));
    public static final PartialModel REDSTONE_VARIAC_REDSTONE = PartialModel.of(CreateElectroEnergetics.rl("block/redstone_variac/redstone"));
    public static final PartialModel RESISTIVE_HEATER_HEATING_ELEMENT = PartialModel.of(CreateElectroEnergetics.rl("block/resistive_heater/heating_element"));
    public static final PartialModel RESISTIVE_HEATER_HEATING_ELEMENT_GLOW = PartialModel.of(CreateElectroEnergetics.rl("block/resistive_heater/heating_element_glow"));
    public static final PartialModel SYNCHROSCOPE_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/synchroscope/dial"));
    public static final PartialModel ELECTRIC_MOTOR_SHAFT = PartialModel.of(CreateElectroEnergetics.rl("block/electric_motor/shaft"));
    public static final PartialModel ALTERNATOR_BRUSHES_SHAFT = PartialModel.of(CreateElectroEnergetics.rl("block/alternator_brushes/shaft"));
    public static final PartialModel BIRB = PartialModel.of(CreateElectroEnergetics.rl("block/birb"));
    public static final PartialModel WIRE_TIE = PartialModel.of(CreateElectroEnergetics.rl("block/wire_tie"));
    public static final PartialModel PANEL_COVER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/cover"));
    public static final PartialModel PANEL_ATTACHMENT_AMMETER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/ammeter"));
    public static final PartialModel PANEL_ATTACHMENT_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/dial"));
    public static final PartialModel PANEL_ATTACHMENT_VOLTMETER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/voltmeter"));
    public static final PartialModel PANEL_ATTACHMENT_ESTOP_CLOSED = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/estop"));
    public static final PartialModel PANEL_ATTACHMENT_ESTOP_OPEN = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/estop_pressed"));
    public static final PartialModel PANEL_ATTACHMENT_MCB_CLOSED = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_circuit_breaker"));
    public static final PartialModel PANEL_ATTACHMENT_MCB_OPEN = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_circuit_breaker_tripped"));
    public static final PartialModel PANEL_ATTACHMENT_ENERGY_METER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/energy_meter"));
    public static final PartialModel PANEL_ATTACHMENT_ENERGY_METER_INVERTED = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/energy_meter_inverted"));
    public static final PartialModel PANEL_ATTACHMENT_TRI_ENERGY_METER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/tri_polar_energy_meter"));
    public static final PartialModel PANEL_ATTACHMENT_TRI_ENERGY_METER_INVERTED = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/tri_polar_energy_meter_inverted"));
    public static final PartialModel PANEL_ATTACHMENT_MOMENTARY_SWITCH = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/momentary_switch"));
    public static final PartialModel PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/momentary_switch_button"));
    public static final PartialModel PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/momentary_switch_button_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_momentary_switch"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_CUT_OFF_SWITCH = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_cut_off_switch"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_momentary_switch_button"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_momentary_switch_button_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_AMMETER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_ammeter"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_VOLTMETER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_voltmeter"));
    public static final PartialModel PANEL_ATTACHMENT_SMOL_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_dial"));

    public static final PartialModel PANEL_ATTACHMENT_SMOL_INDICATOR_BULB = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_indicator_bulb"));
    public static final PartialModel PANEL_ATTACHMENT_TINY_INDICATOR_BULB = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/tiny_indicator_bulb"));
    public static final PartialModel PANEL_ATTACHMENT_INDICATOR_BULB = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/indicator_bulb"));

    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_SMOL = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/miniature_analog_lever"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_LEVER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_lever"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_LEVER_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_lever_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_BALL_HEAD = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_ball_head"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_BALL_HEAD_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_ball_head_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_THROTTLE_HEAD = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_throttle_head"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_THROTTLE_HEAD_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_throttle_head_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_ANALOG_LEVER_METAL_THINGY = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/analog_lever_metal_thingy"));

    public static final PartialModel PANEL_ATTACHMENT_VELOCITY_SENSOR = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/velocity_sensor"));
    public static final PartialModel PANEL_ATTACHMENT_VELOCITY_SENSOR_WIDE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/velocity_sensor_wide"));

    public static final PartialModel PANEL_ATTACHMENT_CUT_OFF_SWITCH = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/cut_off_switch"));
    public static final PartialModel PANEL_ATTACHMENT_TINY_SWITCH = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/tiny_switch"));
    public static final PartialModel PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/cut_off_switch_lever"));
    public static final PartialModel PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/cut_off_switch_lever_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/cut_off_switch_dial"));
    public static final PartialModel PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL_DYEABLE = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/cut_off_switch_dial_dyeable"));
    public static final PartialModel PANEL_ATTACHMENT_SIM_LINEAR_ALTITUDE_SENSOR = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/compat/altitude_sensor_linear"));
    public static final PartialModel PANEL_ATTACHMENT_SIM_RADIAL_ALTITUDE_SENSOR = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/compat/altitude_sensor_radial"));
    public static final PartialModel PANEL_ATTACHMENT_LINK_ANTENNA = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/link_antenna"));
    public static final PartialModel PANEL_ATTACHMENT_LINK_ANTENNA_POWERED = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/attachments/link_antenna_powered"));

    public static final PartialModel[] COLORED_WIRE_SEGMENTS = new PartialModel[DyeColor.values().length];
    public static final PartialModel[] COLORED_HEAVILY_INSULATED_WIRE_SEGMENTS = new PartialModel[DyeColor.values().length];
    public static final PartialModel[] COLORED_HANGING_INSULATOR = new PartialModel[DyeColor.values().length];
    public static final PartialModel[] DYED_ELECTRICAL_PANEL_COVERS = new PartialModel[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_WIRE_SEGMENTS[color.ordinal()] =
                    PartialModel.of(CreateElectroEnergetics.rl("block/colored_wire/" + color.getName()));
            COLORED_HEAVILY_INSULATED_WIRE_SEGMENTS[color.ordinal()] =
                    PartialModel.of(CreateElectroEnergetics.rl("block/colored_heavily_insulated_wire/" + color.getName()));
            COLORED_HANGING_INSULATOR[color.ordinal()] =
                    PartialModel.of(CreateElectroEnergetics.rl("block/hanging_insulator/" + color.getName()));
            DYED_ELECTRICAL_PANEL_COVERS[color.ordinal()] = PartialModel.of(CreateElectroEnergetics.rl("block/electrical_panel/cover/" + color.getSerializedName()));
        }
    }

    public static void register() {

    }
}
