package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.attachments.AttachmentRemovalWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.attachments.BannerWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.attachments.BuntingWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.attachments.DamperWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.DyeWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolWireInteractionBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEWireInteractionBehaviours {
    private static final DeferredRegister<WireInteractionBehaviour> WIRE_INTERACTION_BEHAVIOURS =
            DeferredRegister.create(CEERegistries.WIRE_INTERACTION_BEHAVIOUR, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> BANNER = WIRE_INTERACTION_BEHAVIOURS.register("banner", BannerWireInteractionBehaviour::new);
    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> BUNTING = WIRE_INTERACTION_BEHAVIOURS.register("bunting", BuntingWireInteractionBehaviour::new);
    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> EMPTY_SPOOL = WIRE_INTERACTION_BEHAVIOURS.register("empty_spool", EmptySpoolWireInteractionBehaviour::new);
    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> CLAMP_METER = WIRE_INTERACTION_BEHAVIOURS.register("clamp_meter", ClampMeterWireInteractionBehaviour::new);
    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> DYE = WIRE_INTERACTION_BEHAVIOURS.register("dye", DyeWireInteractionBehaviour::new);
    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> DAMPER = WIRE_INTERACTION_BEHAVIOURS.register("damper", DamperWireInteractionBehaviour::new);
    public static final DeferredHolder<WireInteractionBehaviour, WireInteractionBehaviour> ATTACHMENT_REMOVAL = WIRE_INTERACTION_BEHAVIOURS.register("attachment_removal", AttachmentRemovalWireInteractionBehaviour::new);
    public static void register(IEventBus bus) {
        WIRE_INTERACTION_BEHAVIOURS.register(bus);
    }
}
