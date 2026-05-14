package com.george_vi.electroenergetics.content.accumulator;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.foundation.electrical_properties.AccumulatorProperties;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AccumulatorDevice extends SimpleElectricalDevice {

    public boolean isDoubleCell;

    public double cell1Charge;
    public double cell2Charge;

    public float temp;
    public AccumulatorProperties properties1 = new AccumulatorProperties();
    public AccumulatorProperties properties2 = new AccumulatorProperties();
    public AccumulatorBlockEntity be;

    public AccumulatorDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        properties1.storedCharge = cell1Charge;
        properties2.storedCharge = cell2Charge;

        BridgeCollector.Builder builder = bridges.builder(pos);

        builder.connect(0, 1, properties1);
        if (isDoubleCell)
            builder.connect(2, 3, properties2);
    }

    @Override
    public void postTick(SimulationResults results) {
        cell1Charge = properties1.storedCharge;
        cell2Charge = properties2.storedCharge;

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof AccumulatorBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                this.be.cell1Charge = cell1Charge;
                this.be.cell2Charge = cell2Charge;
                this.be.isDoubleCell = isDoubleCell;
            }
        }


//        float loss = (float) Math.abs(results.getCurrentThrough(pos, 1, 2) * totalVoltage * totalVoltage) / 10000;
//
//        this.temp = updateTemp(this.temp, loss);
//        if (!CEEConfigs.server().componentDamage.get())
//            return;
//
//        if (this.temp > 150000) {
//            if (level.isLoaded(pos)) {
//                CatnipServices.NETWORK.sendToClientsAround((ServerLevel) level, pos.getCenter(), 40, new SendSparkPacket(pos.getCenter(), SendSparkPacket.SparkSize.SMALL));
//                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0, 0, 0,0, 0);
//            }
//            deviceSD.removeDevice(pos);
//            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
//        } else if (this.temp > 120000)
//            showOverheatingParticles(level, pos);
    }

    @Override
    public void update() {
        super.update();

        BlockState state = level.getBlockState(pos);
        if (!CEEBlocks.ACCUMULATOR.has(state))
            return;

        isDoubleCell = state.getValue(AccumulatorBlock.STACK).isDouble();
    }

    @Override
    public void read(CompoundTag tag) {
        this.cell1Charge = tag.getDouble("Cell1Charge");
        this.cell2Charge = tag.getDouble("Cell2Charge");
        this.isDoubleCell = tag.getBoolean("IsDoubleCell");
        this.temp = tag.getFloat("Temp");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putDouble("Cell1Charge", this.cell1Charge);
        tag.putDouble("Cell2Charge", this.cell2Charge);
        if (isDoubleCell)
            tag.putBoolean("IsDoubleCell", true);
        tag.putFloat("Temp", this.temp);
    }
}
