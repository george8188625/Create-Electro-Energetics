package com.george_vi.electroenergetics.content.railway_electrification.gauges;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side cache for train voltage and current values.
 * Used to sync data from server to gauges on train contraptions.
 * Entries are automatically cleaned up after 5 seconds of no updates.
 */
@OnlyIn(Dist.CLIENT)
public class ClientTrainGaugeData {
    private static final Map<UUID, CachedTrainData> TRAIN_DATA = new HashMap<>();
    private static final long EXPIRE_TIME_MS = 5000;

    /**
     * Updates the cached voltage and current values for a train.
     * Called when receiving {@link SyncTrainGaugeDataPacket} from the server.
     *
     * @param trainId The UUID of the train
     * @param voltage The train's current voltage in volts
     * @param current The train's total current draw in amps
     */
    public static void update(UUID trainId, double voltage, double current) {
        TRAIN_DATA.put(trainId, new CachedTrainData(voltage, current, System.currentTimeMillis()));
    }

    /**
     * Retrieves the cached voltage and current values for a train.
     * Returns {@link TrainGaugeValues#ZERO} if no data exists for the given train.
     *
     * @param trainId The UUID of the train
     * @return The cached gauge values, or zero values if not found
     */
    public static TrainGaugeValues get(UUID trainId) {
        CachedTrainData cached = TRAIN_DATA.get(trainId);
        if (cached == null)
            return TrainGaugeValues.ZERO;
        return new TrainGaugeValues(cached.voltage, cached.current);
    }

    /**
     * Called periodically to remove stale entries.
     * Should be called from a client tick event.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, CachedTrainData>> iterator = TRAIN_DATA.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, CachedTrainData> entry = iterator.next();
            if (now - entry.getValue().timestamp > EXPIRE_TIME_MS) {
                iterator.remove();
            }
        }
    }

    /**
     * Internal cached data with timestamp for expiry tracking.
     */
    private record CachedTrainData(double voltage, double current, long timestamp) {}

    /**
     * Public-facing gauge values without the internal timestamp.
     */
    public record TrainGaugeValues(double voltage, double current) {
        public static final TrainGaugeValues ZERO = new TrainGaugeValues(0, 0);
    }
}
