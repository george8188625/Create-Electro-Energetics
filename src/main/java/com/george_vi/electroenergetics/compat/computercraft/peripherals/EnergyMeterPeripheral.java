package com.george_vi.electroenergetics.compat.computercraft.peripherals;

import com.george_vi.electroenergetics.content.energy_meter.ChangeEnergyMeterStatePacket;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.createmod.catnip.platform.CatnipServices;

public class EnergyMeterPeripheral extends SyncedPeripheral<EnergyMeterBlockEntity> {
	public EnergyMeterPeripheral(EnergyMeterBlockEntity blockEntity) {
		super(blockEntity);
	}

	@Override
	public String getType() {
		return "ElectroEnergetics_EnergyMeter";
	}

	@LuaFunction
	public final void resetCounter() {
		CatnipServices.NETWORK.sendToServer(new ChangeEnergyMeterStatePacket(true, blockEntity.disconnected, blockEntity.getBlockPos()));
	}

	@LuaFunction
	public final void disconnect() {
		CatnipServices.NETWORK.sendToServer(new ChangeEnergyMeterStatePacket(false, true, blockEntity.getBlockPos()));
	}

	@LuaFunction
	public final void connect() {
		CatnipServices.NETWORK.sendToServer(new ChangeEnergyMeterStatePacket(false, false, blockEntity.getBlockPos()));
	}

	@LuaFunction
	public final boolean isConnected() {
		return !blockEntity.disconnected;
	}

	@LuaFunction
	public final double getTotalEnergy() {
		return blockEntity.totalEnergy;
	}
}
