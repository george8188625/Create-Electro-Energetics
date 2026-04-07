package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.TickingElectricalDevice;
import com.george_vi.simulateddevices.SDRegistries;
import com.george_vi.simulateddevices.device.DeviceFeatureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEESimulatedDeviceFeatureTypes {

    private static final DeferredRegister<DeviceFeatureType> DEVICE_FEATURE_TYPES =
            DeferredRegister.create(SDRegistries.SIMULATED_DEVICE_FEATURE_TYPE, CreateElectroEnergetics.ID);


    public static final DeferredHolder<DeviceFeatureType, DeviceFeatureType> ELECTRICAL = DEVICE_FEATURE_TYPES.register("electrical",
            () -> new DeviceFeatureType(ElectricalDevice.class));

    public static final DeferredHolder<DeviceFeatureType, DeviceFeatureType> TICKING_ELECTRICAL = DEVICE_FEATURE_TYPES.register("ticking_electrical",
            () -> new DeviceFeatureType(TickingElectricalDevice.class));

    public static void register(IEventBus bus) {
        DEVICE_FEATURE_TYPES.register(bus);
    }

}
