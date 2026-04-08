package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.devices.device.DeviceFeatureType;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.simulation.WireType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class CEERegistries {

    public static final Registry<WireType> WIRE_TYPE = new RegistryBuilder<WireType>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("wire_type")))
            .sync(true)
            .defaultKey(CreateElectroEnergetics.rl("standard"))
            .create();

    public static final Registry<WireAttachmentType> WIRE_ATTACHMENT_TYPE = new RegistryBuilder<WireAttachmentType>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("wire_attachment_type")))
            .sync(true)
            .defaultKey(CreateElectroEnergetics.rl("empty"))
            .create();

    public static final Registry<WireInteractionBehaviour> WIRE_INTERACTION_BEHAVIOUR = new RegistryBuilder<WireInteractionBehaviour>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("wire_interaction_behaviour")))
            .sync(true)
            .defaultKey(CreateElectroEnergetics.rl("empty"))
            .create();

    public static final Registry<ElectricTrainSoundType> ELECTRIC_TRAIN_SOUND_TYPE = new RegistryBuilder<ElectricTrainSoundType>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("electric_train_sound_type")))
            .sync(true)
            .defaultKey(CreateElectroEnergetics.rl("modern"))
            .create();

    public static final Registry<PantographType> PANTOGRAPH_TYPE = new RegistryBuilder<PantographType>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("pantograph_type")))
            .sync(true)
            .defaultKey(CreateElectroEnergetics.rl("standard"))
            .create();

    public static final Registry<SimulatedDeviceType<?>> SIMULATED_DEVICE_TYPE =
            new RegistryBuilder<SimulatedDeviceType<?>>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("simulated_device_type")))
                    .sync(false)
                    .onAdd(((registry, id,
                             key, value) -> {
                        for (Block block : value.validBlocks())
                            SimulatedDeviceType.BY_BLOCK.put(block, value);
                    }))
                    .create();

    public static final Registry<DeviceFeatureType> SIMULATED_DEVICE_FEATURE_TYPE =
            new RegistryBuilder<DeviceFeatureType>(ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("simulated_device_feature_type")))
                    .sync(false)
                    .onBake((registry) -> DeviceFeatureType.freeze())
                    .create();

}
