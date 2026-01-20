package com.george_vi.electroenergetics;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class CEEPartialModels {
    public static final PartialModel WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/wire_segment"));
    public static final PartialModel COPPER_WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/copper_wire_segment"));
    public static final PartialModel CREATIVE_WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/creative_wire_segment"));
    public static final PartialModel IRON_WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/iron_wire_segment"));
    public static final PartialModel IRON_BUS_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/iron_bus_segment"));
    public static final PartialModel HEAVILY_INSULATED_WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/heavily_insulated_wire_segment"));
    public static final PartialModel ROTOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/alternator_rotor/block"));
    public static final PartialModel INSULATOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/connector/insulator"));
    public static final PartialModel ATTACHMENT_CHAIN = PartialModel.of(CreateElecrtoEnergetics.rl("block/wire_attachments/attachment_chain"));
    public static final PartialModel HV_SWITCH_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/high_voltage_switch/arm"));
    public static final PartialModel HV_SWITCH_PIVOT = PartialModel.of(CreateElecrtoEnergetics.rl("block/high_voltage_switch/pivot"));
    public static final PartialModel PANTOGRAPH_UPPER_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/upper_arm"));
    public static final PartialModel PANTOGRAPH_UPPER_ARM_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/upper_arm_arm"));
    public static final PartialModel PANTOGRAPH_LOWER_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/lower_arm"));
    public static final PartialModel PANTOGRAPH_UPPER_ARMS_DOUBLE = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/upper_arms_double"));
    public static final PartialModel PANTOGRAPH_LOWER_ARMS_DOUBLE = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/lower_arms_double"));
    public static final PartialModel PANTOGRAPH_CONNECTING_SURFACE = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/connecting_surface"));
    public static final PartialModel PANTOGRAPH_CONNECTING_SURFACE_DOUBLE = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/connecting_surface_double"));
    public static final PartialModel PANTOGRAPH_CONNECTING_ROD = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/connecting_rod"));
    public static final PartialModel PANTOGRAPH_SPRINGS = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/springs"));
    public static final PartialModel PANTOGRAPH_SPRINGS_DOUBLE = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/springs_double"));
    public static final PartialModel CATENARY_HOLDER_INSULATOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/insulator"));
    public static final PartialModel CATENARY_HOLDER_LONG_ROD = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/long_rod"));
    public static final PartialModel CATENARY_HOLDER_SHORT_ROD = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/short_rod"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_4 = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/mount_4"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_6 = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/mount_6"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_8 = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/mount_8"));
    public static final PartialModel CATENARY_HOLDER_MOUNT_10 = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/mount_10"));
    public static final PartialModel CATENARY_HOLDER_CONNECTOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/catenary_holder/connector"));
    public static final PartialModel BULB_FILAMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/bulb/filament"));
    public static final PartialModel BULB_FILAMENT_BRIGHT = PartialModel.of(CreateElecrtoEnergetics.rl("block/bulb/filament_bright"));
    public static final PartialModel BULB_BROKEN_FILAMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/bulb/filament_broken"));
    public static final PartialModel BULB_GLASS = PartialModel.of(CreateElecrtoEnergetics.rl("block/bulb/glass"));
    public static final PartialModel RESISTOR_STRIP = PartialModel.of(CreateElecrtoEnergetics.rl("block/electronics/resistor_strip"));
    public static final PartialModel FUSE_HOLDER_COPPER_CONDUCTOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/fuse_holder/copper_conductor"));
    public static final PartialModel FUSE_HOLDER_FUSE = PartialModel.of(CreateElecrtoEnergetics.rl("block/fuse_holder/fuse"));
    public static final PartialModel FUSE_HOLDER_BROKEN_FUSE = PartialModel.of(CreateElecrtoEnergetics.rl("block/fuse_holder/broken_fuse"));
    public static final PartialModel INDICATOR_BULB_GLOW = PartialModel.of(CreateElecrtoEnergetics.rl("block/indicator_bulb/glow"));
    public static final PartialModel INDICATOR_BULB_TUBE = PartialModel.of(CreateElecrtoEnergetics.rl("block/indicator_bulb/tube"));
    public static final PartialModel INDICATOR_BULB_CUBE = PartialModel.of(CreateElecrtoEnergetics.rl("block/indicator_bulb/cube"));
    public static final PartialModel WIRE_DAMPER_ATTACHMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/wire_damper_attachment"));
    public static final PartialModel POTENTIOMETER_DIAL = PartialModel.of(CreateElecrtoEnergetics.rl("block/potentiometer/dial"));

    public static final Map<DyeColor, PartialModel> COLORED_WIRE_SEGMENTS = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_WIRE_SEGMENTS.put(color,
                    PartialModel.of(CreateElecrtoEnergetics.rl("block/colored_wire/" + color.getName())));
        }
    }

    public static void register() {

    }
}
