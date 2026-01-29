package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnection;
import com.george_vi.electroenergetics.simulation.WireType;
import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;

public class WireEffect implements Effect {
    private final LevelAccessor level;
    private NodeConnection connection;
    private CatenaryConnection catenaryConnection;
    private final WireType wireType;
    private final boolean catenary;

    public WireEffect(LevelAccessor level, NodeConnection connection, WireType wireType) {
        this.level = level;
        this.connection = connection;
        this.wireType = wireType;
        catenary = false;
    }

    public WireEffect(ClientLevel level, CatenaryConnection connection, WireType wireType) {
        this.level = level;
        this.catenaryConnection = connection;
        this.wireType = wireType;
        catenary = true;
    }

    @Override
    public LevelAccessor level() {
        return level;
    }

    @Override
    public EffectVisual<?> visualize(VisualizationContext ctx, float partialTick) {
        return catenary ? new CatenaryVisual(ctx, catenaryConnection, wireType) : new WireVisual(ctx, connection, wireType);
    }

}
