package com.george_vi.electroenergetics.compat.computercraft.peripherals;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.foundation.electrical_properties.AccumulatorProperties;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;

public class AccumulatorPeripheral extends SyncedPeripheral<AccumulatorBlockEntity> {
	public AccumulatorPeripheral(AccumulatorBlockEntity blockEntity) {
		super(blockEntity);
	}

	@Override
	public String getType() {
		return "ElectroEnergetics_Accumulator";
	}

	@LuaFunction
	public final boolean hasTwoCells() {
		return blockEntity.isDoubleCell;
	}

	@LuaFunction
	public final double getCellWattHours(int index) {
		return switch (index) {
			case 0 -> blockEntity.cell1Charge;
			case 1 -> {
				if (!blockEntity.isDoubleCell)
					throw new IllegalArgumentException("No second cell");
				yield blockEntity.cell2Charge;
			}
			default -> throw new IllegalArgumentException("Invalid cell index");
		};
	}

	@LuaFunction
	public final double getWattHours() {
		return blockEntity.cell1Charge + blockEntity.cell2Charge;
	}

	@LuaFunction
	public final double getCellChargePercentage(int index) {
		return switch(index) {
			case 0 -> blockEntity.cell1Charge / AccumulatorProperties.getNominalCharge() * 100d;
			case 1 -> {
				if (!blockEntity.isDoubleCell)
					throw new IllegalArgumentException("No second cell");
				yield blockEntity.cell2Charge / AccumulatorProperties.getNominalCharge() * 100d;
			}
			default -> throw new IllegalArgumentException("Invalid cell index");
		};
	}

	@LuaFunction
	public final double getChargePercentage() {
		return (blockEntity.cell1Charge + blockEntity.cell2Charge)
				/ (AccumulatorProperties.getNominalCharge()
				* (blockEntity.isDoubleCell ? 2 : 1)) * 100d;
	}

	@LuaFunction
	public final double getCapacity() {
		return AccumulatorProperties.getNominalCharge() * (blockEntity.isDoubleCell ? 2 : 1);
	}

	@LuaFunction
	public final double getCellCapacity() {
		return AccumulatorProperties.getNominalCharge();
	}
}
