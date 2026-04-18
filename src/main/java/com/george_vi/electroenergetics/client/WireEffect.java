package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;

public class WireEffect implements Effect {
    private final LevelAccessor level;
    private InWorldNodeConnection connection;
    private CatenaryConnection catenaryConnection;
    private final WireType wireType;
    private final WireData wireData;
    private final boolean catenary;

    public WireEffect(LevelAccessor level, InWorldNodeConnection connection, WireType wireType, WireData wireData) {
        this.level = level;
        this.connection = connection;
        this.wireType = wireType;
        this.wireData = wireData;
        catenary = false;
    }

    public WireEffect(ClientLevel level, CatenaryConnection connection, WireType wireType, WireData wireData) {
        this.level = level;
        this.catenaryConnection = connection;
        this.wireType = wireType;
        this.wireData = wireData;
        catenary = true;
    }

    @Override
    public LevelAccessor level() {
        return level;
    }

    @Override
    public EffectVisual<?> visualize(VisualizationContext ctx, float partialTick) {
        return catenary ? new CatenaryVisual(ctx, catenaryConnection, wireType) : new WireVisual(ctx, connection, wireType, wireData);
    }

}
