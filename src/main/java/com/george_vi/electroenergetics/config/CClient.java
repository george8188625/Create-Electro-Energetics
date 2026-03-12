package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;

public class CClient extends ConfigBase {
    public final ConfigBool wireLOD = b(true, "wireLOD", "Use Level-Of-Detail when rendering wires without flywheel.");
    public final ConfigInt wireRenderDistance = i(512, "wireRenderDistance", "Maximum distance for wire rendering without flywheel.");
    public final ConfigBool debugPantographRange = b(false, "debugPantographRange", "Show the pantograph range box.");
    public final ConfigBool debugNodeID = b(false, "debugNodeID", "Display nodeID when hovering.");

    @Override
    public String getName() {
        return "client";
    }
}
