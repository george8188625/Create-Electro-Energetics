package com.george_vi.electroenergetics.compat.computercraft.peripherals;

import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;

public class EnergyMeterPeripheral extends SyncedPeripheral<EnergyMeterBlockEntity> {
	public EnergyMeterPeripheral(EnergyMeterBlockEntity blockEntity) {
		super(blockEntity);
	}

	@Override
	public String getType() {
		return "ElectroEnergetics_EnergyMeter";
	}

	@LuaFunction
	public final boolean isConnected() {
		return !blockEntity.disconnected;
	}

	@LuaFunction
	public final double getTotalEnergy() {
		return blockEntity.totalEnergy * blockEntity.scale.getScale();
	}
}
