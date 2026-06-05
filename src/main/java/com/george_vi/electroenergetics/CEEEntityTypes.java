package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeEntity;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.MobCategory;

import static com.george_vi.electroenergetics.CreateElectroEnergetics.REGISTRATE;

@SuppressWarnings("unused")
public class CEEEntityTypes {

    public static final EntityEntry<DetachedNodeEntity> DETACHED_NODE = REGISTRATE.entity("detached_node", DetachedNodeEntity::new, MobCategory.MISC)
            .properties(b -> b
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(2)
                    .setUpdateInterval(3)
                    .fireImmune()
                    .noSummon()
                    .setShouldReceiveVelocityUpdates(false))
            .renderer(() -> NoopRenderer::new)
            .register();

    public static void register() {

    }
}
