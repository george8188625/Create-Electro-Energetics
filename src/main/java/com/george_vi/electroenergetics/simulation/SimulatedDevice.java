package com.george_vi.electroenergetics.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

public class SimulatedDevice {
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
    public void preTick(BlockPos pos, Level level, BridgeCollector bridges, CompoundTag extraData) {}

    /**
     * This method is called once per device after the simulation. Each device can now process the voltages, currents and show results in the world.
     * @param pos in-world position of the devices block (this position will not always be loaded, before accessing check with {@link Level#isLoaded(BlockPos position)})
     * @param level level that the device is in
     * @param voltages solved voltages of all nodes
     * @param sourceAmps current through voltage sources (only voltage sources)
     * @param extraData saved data local to the device
     */
    public void postTick(BlockPos pos, Level level, Map<Node, Double> voltages, Map<NodeConnection, Double> sourceAmps, CompoundTag extraData) {}

    /**
     * Returns the maximum distance at which the server will send node voltages to clients.
     * @return maximum distance for node voltage transmission
     */
    public int sendVoltagesDistance() {
        return 20;
    }
}
