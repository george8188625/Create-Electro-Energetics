package com.george_vi.electroenergetics.config;

import net.createmod.catnip.config.ConfigBase;

public class CClient extends ConfigBase {
    public final ConfigBool wireLOD = b(true, "wireLOD", "Use Level-Of-Detail when rendering wires.");
    public final ConfigInt wireRenderDistance = i(512, "wireRenderDistance", "Maximum distance for wire rendering.");

    @Override
    public String getName() {
        return "client";
    }
}
