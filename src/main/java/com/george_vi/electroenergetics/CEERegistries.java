package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class CEERegistries {

    public static final Registry<WireType> WIRE_TYPE = new RegistryBuilder<WireType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("wire_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("standard"))
            .create();

    public static final Registry<WireAttachmentType> WIRE_ATTACHMENT_TYPE = new RegistryBuilder<WireAttachmentType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("wire_attachment_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("empty"))
            .create();

    public static final Registry<WireInteractionBehaviour> WIRE_INTERACTION_BEHAVIOUR = new RegistryBuilder<WireInteractionBehaviour>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("wire_interaction_behaviour")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("empty"))
            .create();

    public static final Registry<ElectricTrainSoundType> ELECTRIC_TRAIN_SOUND_TYPE = new RegistryBuilder<ElectricTrainSoundType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("electric_train_sound_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("modern"))
            .create();

    public static final Registry<PantographType> PANTOGRAPH_TYPE = new RegistryBuilder<PantographType>(ResourceKey.createRegistryKey(CreateElecrtoEnergetics.rl("pantograph_type")))
            .sync(true)
            .defaultKey(CreateElecrtoEnergetics.rl("standard"))
            .create();

}
