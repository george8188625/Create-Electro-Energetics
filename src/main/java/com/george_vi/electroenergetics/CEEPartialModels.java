package com.george_vi.electroenergetics;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class CEEPartialModels {
    public static final PartialModel WIRE_SEGMENT = PartialModel.of(CreateElecrtoEnergetics.rl("block/wire_segment"));
    public static final PartialModel ROTOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/alternator_rotor/block"));
    public static final PartialModel INSULATOR = PartialModel.of(CreateElecrtoEnergetics.rl("block/connector/insulator"));

    public static void register() {

    }
}
