package com.george_vi.electroenergetics.compat.computercraft.peripherals;

import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;

public class ElectricGaugePeripheral extends SyncedPeripheral<ElectricGaugeBlockEntity> {
    public ElectricGaugePeripheral(ElectricGaugeBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "ElectroEnergetics_ElectricGauge";
    }

    @LuaFunction
    public final float getValue() {
        return (float) (blockEntity.voltmeter ? blockEntity.voltage : blockEntity.voltage / 0.01);
    }
}
