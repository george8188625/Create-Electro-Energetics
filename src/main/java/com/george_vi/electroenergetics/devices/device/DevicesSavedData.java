package com.george_vi.electroenergetics.devices.device;

import com.george_vi.electroenergetics.CEERegistries;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class DevicesSavedData extends SavedData {
    Long2ObjectMap<SimulatedDevice> DEVICES_BY_POS = new Long2ObjectOpenHashMap<>();
    List<SimulatedDevice>[] DEVICES_BY_FEATURE_TYPE;


    public final ServerLevel level;
    public static final Logger LOGGER = LogUtils.getLogger();

    private DevicesSavedData(ServerLevel level) {
        this.level = level;
        this.DEVICES_BY_FEATURE_TYPE = new ArrayList[DeviceFeatureType.globalID];
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        ListTag deviceList = new ListTag();

        for (Long2ObjectMap.Entry<SimulatedDevice> e : DEVICES_BY_POS.long2ObjectEntrySet()) {
            BlockPos pos = BlockPos.of(e.getLongKey());
            SimulatedDevice device = e.getValue();
            CompoundTag deviceTag = new CompoundTag();

            deviceTag.put("Pos", NbtUtils.writeBlockPos(pos));
            deviceTag.putString("ID", device.type.id().toString());
            CompoundTag deviceDataTag = new CompoundTag();
            device.write(deviceDataTag);
            deviceTag.put("Data", deviceDataTag);
            deviceList.add(deviceTag);
        }

        tag.put("Devices", deviceList);
        return tag;
    }

    private static DevicesSavedData load(ServerLevel level, CompoundTag tag, HolderLookup.Provider provider) {
        DevicesSavedData sd = new DevicesSavedData(level);
        sd.DEVICES_BY_POS.clear();
        sd.DEVICES_BY_FEATURE_TYPE = new ArrayList[DeviceFeatureType.globalID];

        tag.getList("Devices", Tag.TAG_COMPOUND).forEach(tg -> {
            CompoundTag deviceTag = (CompoundTag) tg;
            BlockPos pos = readBlockPos(deviceTag, "Pos");
            ResourceLocation id = null;
            try {
                id = ResourceLocation.parse(deviceTag.getString("ID"));

                SimulatedDeviceType<?> deviceType = CEERegistries.SIMULATED_DEVICE_TYPE.get(id);
                if (deviceType == null)
                    throw new IllegalStateException("No device with id: " + id);
                if (sd.DEVICES_BY_POS.containsKey(pos.asLong()))
                    throw new IllegalStateException("A device already exists at pos ");

                SimulatedDevice device = deviceType.create(level, pos, sd);

                device.read(deviceTag.getCompound("Data"));
                sd.DEVICES_BY_POS.put(pos.asLong(), device);

                for (DeviceFeatureType featureType : getFeatureTypesOf(device)) {
                    if (sd.DEVICES_BY_FEATURE_TYPE[featureType.id] == null)
                        sd.DEVICES_BY_FEATURE_TYPE[featureType.id] = new ArrayList<>();
                    sd.DEVICES_BY_FEATURE_TYPE[featureType.id].add(device);
                }

            } catch (Throwable err) {
                if (id == null)
                    LOGGER.warn("Could not load device at position {}, removing...", pos.toShortString(), err);
                else
                    LOGGER.warn("Could not load device at position {} with id {}, removing...", pos.toShortString(), id, err);
            }
        });

        return sd;
    }

    public static DevicesSavedData load(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(new Factory<>(() -> new DevicesSavedData(level), (tag, provider) -> DevicesSavedData.load(level, tag, provider)), "simulated_devices");
    }

    @SuppressWarnings("unchecked")
    public <T extends SimulatedDevice> T addDevice(SimulatedDeviceType<T> deviceType, BlockPos pos, CompoundTag tag) {
        SimulatedDevice oldDevice = DEVICES_BY_POS.get(pos.asLong());

        if (oldDevice != null && oldDevice.type == deviceType) {
            oldDevice.update();
            setDirty();
            return (T) oldDevice;
        }

        if (oldDevice != null) {
            for (DeviceFeatureType featureType : getFeatureTypesOf(oldDevice)) {
                if (DEVICES_BY_FEATURE_TYPE[featureType.id] != null)
                    DEVICES_BY_FEATURE_TYPE[featureType.id].remove(oldDevice);
            }
        }
        T device = deviceType.create(level, pos, this);
        device.read(tag);
        DEVICES_BY_POS.put(pos.asLong(), device);
        for (DeviceFeatureType featureType : getFeatureTypesOf(device)) {
            if (DEVICES_BY_FEATURE_TYPE[featureType.id] == null)
                DEVICES_BY_FEATURE_TYPE[featureType.id] = new ArrayList<>();
            DEVICES_BY_FEATURE_TYPE[featureType.id].add(device);
        }
        setDirty();
        return device;
    }

    public static Collection<DeviceFeatureType> getFeatureTypesOf(SimulatedDevice device) {
        List<DeviceFeatureType> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<DeviceFeatureType>, DeviceFeatureType> e : CEERegistries.SIMULATED_DEVICE_FEATURE_TYPE.entrySet()) {
            DeviceFeatureType type = e.getValue();
            if (type.featureClazz.isInstance(device))
                out.add(type);
        }
        return out;
    }

    public @Nullable SimulatedDevice removeDevice(BlockPos pos) {
        SimulatedDevice removed = DEVICES_BY_POS.remove(pos.asLong());
        if (removed != null) {
            removed.onDestroy();
            removed.invalidate();
            for (DeviceFeatureType featureType : getFeatureTypesOf(removed)) {
                if (DEVICES_BY_FEATURE_TYPE[featureType.id] != null)
                    DEVICES_BY_FEATURE_TYPE[featureType.id].remove(removed);
            }
        }
        setDirty();
        return removed;
    }

    public @Nullable SimulatedDevice getDevice(BlockPos pos) {
        return DEVICES_BY_POS.get(pos.asLong());
    }

    /**
     * @return null if the device is not of this type or does not exist, otherwise the device.
     */
    @SuppressWarnings("unchecked")
    public <T extends SimulatedDevice> @Nullable T getDevice(BlockPos pos, Class<T> clazz) {
        SimulatedDevice device = DEVICES_BY_POS.get(pos.asLong());
        return clazz.isInstance(device) ? (T) device : null;
    }


    /**
     * @return All devices
     */
    public Collection<SimulatedDevice> getDevices() {
        return new ArrayList<>(DEVICES_BY_POS.values());
    }

    /**
     * @return All devices of the specified feature type
     */
    public Collection<SimulatedDevice> getDevices(DeviceFeatureType deviceFeatureType) {
        Collection<SimulatedDevice> collection = DEVICES_BY_FEATURE_TYPE[deviceFeatureType.id];
        if (collection == null)
            return Collections.emptyList();
        return new ArrayList<>(collection);
    }

    // Backwards compatible with 1.20
    // Credit: Taken from Create
    public static BlockPos readBlockPos(CompoundTag nbt, String key) {
        Optional<BlockPos> pos = NbtUtils.readBlockPos(nbt, key);
        if (pos.isPresent())
            return pos.get();
        CompoundTag oldTag = nbt.getCompound(key);
        return new BlockPos(oldTag.getInt("X"), oldTag.getInt("Y"), oldTag.getInt("Z"));
    }
}
