package com.george_vi.electroenergetics.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This is a type class for devices.
 * Devices are data structures for blocks with electric behaviour that allows them to tick, even tho they are unloaded.
 * Devices use a separate object for storing data.
 * @param <T> the data holder class type
 */
public abstract class SimulatedDevice<T> {
    final ResourceLocation id;
    public SimulatedDevice(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getID() {
        return id;
    }

    /**
     * This method called once per device before the simulation. Each device can now 'bridge' its nodes, with a resistor or a voltage source, by that nodes are internally connected.
     * @param pos in-world position of the devices block (this position will not always be loaded, before accessing check with {@link Level#isLoaded(BlockPos position)})
     * @param level level that the device is in
     * @param bridges used to 'bridge' nodes or create internal nodes
     * @param extraData saved data local to the device
     */
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, T extraData) {}

    /**
     * This method is called once per device after the simulation. Each device can now process the voltages, currents and show results in the world.
     * @param pos in-world position of the devices block (this position will not always be loaded, before accessing check with {@link Level#isLoaded(BlockPos position)})
     * @param level level that the device is in
     * @param results a holder for the resulting voltages, also containing methods useful for calculating currents etc.
     * @param extraData saved data local to the device
     */
    public void postTick(BlockPos pos, Level level, SimulationResults results, T extraData) {}

    /**
     * Reads the data for this device from an nbt format.
     * @param tag the serialized data
     * @return the data holder object for this device
     */
    public abstract T read(CompoundTag tag);


    /**
     * Writes the data for this device into an nbt format.
     * @param extraData the data holder object for this device
     * @return the serialized data
     */
    public abstract CompoundTag write(T extraData);

    protected void showOverheatingParticles(Level level, BlockPos pos) {
        if (!level.isLoaded(pos))
            return;
        Vec3 pPos = pos.getCenter();

        if (level.random.nextFloat() > 0.5f)
            ((ServerLevel)level).sendParticles(ParticleTypes.SMOKE, pPos.x, pPos.y, pPos.z, 5, 0.2, 0.2, 0.2, 0);
    }


    /**
     * When heat > 0, the temperature rises, until it settles into a value. That value depends on the heat value.
     * This is the formula for the value, the temperature settles into, where h - heat
     * max(0,30 * (h - 3.3))
     * This is similar to the temperature mentioned in WireType, but the values are different.
     * Temperature here isn't based on just the current, but power (for instance heat loss calculated using the P=I²R formula).
     *
     * @param temp temp value in abstract units
     * @param heat heat (energy loss) in Watts
     * @return new temp value in abstract units
     */
    protected float updateTemp(float temp, float heat) {
        if (Float.isNaN(temp))
            temp = 0;

        float newTemp = heat + 30f;
        newTemp *= Math.min(temp < 0 ? 0 : 1 / (1 + (temp / 1000)), 1);
        newTemp = Math.max(temp - 33.3f + newTemp, 0);

        return newTemp;
    }


}
