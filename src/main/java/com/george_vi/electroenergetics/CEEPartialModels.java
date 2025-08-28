package com.george_vi.electroenergetics;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class CEEPartialModels {
    public static final PartialModel WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/wire_segment"));
    public static final PartialModel ROTOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/alternator_rotor/block"));
    public static final PartialModel INSULATOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/connector/insulator"));
    public static final PartialModel ATTACHMENT_CHAIN = PartialModel.of(CreateElecrtoEnergetics.rl("block/wire_attachments/attachment_chain"));
    public static final PartialModel HV_SWITCH_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/high_voltage_switch/arm"));
    public static final PartialModel HV_SWITCH_PIVOT = PartialModel.of(CreateElecrtoEnergetics.rl("block/high_voltage_switch/pivot"));
    public static final PartialModel PANTOGRAPH_UPPER_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/upper_arm"));
    public static final PartialModel PANTOGRAPH_LOWER_ARM = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/lower_arm"));
    public static final PartialModel PANTOGRAPH_CONNECTING_SURFACE = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/connecting_surface"));
    public static final PartialModel PANTOGRAPH_CONNECTING_ROD = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/connecting_rod"));
    public static final PartialModel PANTOGRAPH_SPRINGS = PartialModel.of(CreateElecrtoEnergetics.rl("block/pantograph/springs"));

    public static void register() {

    }
}
