package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.simulation.CableType;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class CEERegistries {

    public static final Registry<WireType> WIRE_TYPE = new RegistryBuilder<WireType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("wire_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("empty"))
            .create();

    public static final Registry<CableType> CABLE_TYPE = new RegistryBuilder<CableType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("cable_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("empty"))
            .create();

    public static final Registry<WireAttachmentType> WIRE_ATTACHMENT_TYPE = new RegistryBuilder<WireAttachmentType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("wire_attachment_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("empty"))
            .create();

    public static final Registry<WireInteractionBehaviour> WIRE_INTERACTION_BEHAVIOUR = new RegistryBuilder<WireInteractionBehaviour>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("wire_interaction_behaviour")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("empty"))
            .create();
}
