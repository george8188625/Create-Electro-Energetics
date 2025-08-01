package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.content.wire.attachments.BannerAttachmentType;
import com.george_vi.electroenergetics.content.wire.attachments.HangingSignAttachmentType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEWireAttachments {
    private static final DeferredRegister<WireAttachmentType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_ATTACHMENT_TYPE, CreateElecrtoEnergetics.ID);

    public static final DeferredHolder<WireAttachmentType, WireAttachmentType> OAK_HANGING_SIGN = WIRE_TYPES.register("oak_hanging_sign", () -> new HangingSignAttachmentType(() -> Blocks.OAK_HANGING_SIGN));
    public static final DeferredHolder<WireAttachmentType, WireAttachmentType> BANNER = WIRE_TYPES.register("banner", BannerAttachmentType::new);

    public static void register(IEventBus bus) {
        WIRE_TYPES.register(bus);
    }
}
