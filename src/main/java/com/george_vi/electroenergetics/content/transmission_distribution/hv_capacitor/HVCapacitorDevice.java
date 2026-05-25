package com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorDevice;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.electrical_properties.CapacitorProperties;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.DoubleSupplier;

public class HVCapacitorDevice extends SimpleElectricalDevice {
    public final DoubleSupplier maxVoltage;
    public double lastVoltage;
    public double capacitance;
    public float temp;
    public Direction facing;
    public CapacitorProperties properties;

    public HVCapacitorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type, DoubleSupplier maxVoltage) {
        super(level, pos, deviceSD, type);
        this.maxVoltage = maxVoltage;
    }
    
    @Override
    public void preTick(BridgeCollector bridges) {
        properties.capacitance = capacitance;
        BlockPos connectorPos = facing == null ? null : pos.relative(facing);
        DoubleConnectorDevice connectorDevice = connectorPos == null ? null : deviceSD.getDevice(connectorPos, DoubleConnectorDevice.class);

        bridges.builder(pos)
                .node(2)
                .connect(new InWorldNode(0, connectorDevice == null ? pos : connectorPos), new InWorldNode(2, pos), properties)
                .resistor(new InWorldNode(1, connectorDevice == null ? pos : connectorPos), new InWorldNode(2, pos), 0.1);
    }

    @Override
    public void postTick(SimulationResults results) {
        lastVoltage = properties.lastVoltage;

        double voltage = results.getVoltageAt(pos, 0, 2);

        temp = ElectricalDevice.updateTemp(temp, (float) ((Math.abs(voltage) * 500) / maxVoltage.getAsDouble()));

        if (!CEEConfigs.server().componentDamage.get())
            return;

        if (temp > 17000) {
            if (level.isLoaded(pos)) {
                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
            }
            deviceSD.removeDevice(pos);
            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        } else if (temp > 14000)
            ElectricalDevice.showOverheatingParticles(level, pos);
    }

    @Override
    public void read(CompoundTag tag) {
        lastVoltage = tag.getDouble("LastVoltage");
        capacitance = tag.getDouble("Capacitance");
        temp = tag.getFloat("Temp");
        properties = new CapacitorProperties();
        facing = tag.contains("Facing") ? Direction.byName(tag.getString("Facing")) : null;
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("LastVoltage", lastVoltage);
        tag.putDouble("Capacitance", capacitance);
        tag.putFloat("Temp", temp);
        if (facing != null)
            tag.putString("Facing", facing.getSerializedName());
    }

}
