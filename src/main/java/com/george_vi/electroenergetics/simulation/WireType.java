package com.george_vi.electroenergetics.simulation;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.item.Item;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class WireType {

    final DoubleSupplier resistance;
    final PartialModel model;
    final Supplier<Item> droppedItem;
    final Supplier<Item> spoolItem;
    final double insulationResistance;
    final double maxInsulationVoltage;
    final Supplier<WireType> onOverheated;

    /**
     * This is the temperature at which the wire burns. It is in abstract units.
     * When a current is going through a wire, it raises temperature. It eventually settles into a temperature,
     * the temperature that it stops heating up is dependent on the current.
     * these are temperature values, that the wire settles into, and the currents needed for that.
     * The current is capped to 1000A. Here are a few samples from the function.
     * 14000 -> ~ 500A
     * 10000 -> ~ 366A
     * 7500  -> ~ 280A
     * 5000  -> ~ 200A
     * 2000  -> ~ 100A
     * 1000  -> ~ 66.6A
     * 500   -> ~ 50A
     * 0     -> I <= 33.3A
     */
    final DoubleSupplier maxTemperature;
    final float sag;
    final IntSupplier maxLength;

    public WireType(DoubleSupplier resistance, PartialModel model, Supplier<Item> droppedItem, Supplier<Item> spoolItem, double insulationResistance, double maxInsulationVoltage, Supplier<WireType> onOverheated, DoubleSupplier maxTemperature, float sag, IntSupplier maxLength) {
        this.resistance = resistance;
        this.model = model;
        this.droppedItem = droppedItem;
        this.spoolItem = spoolItem;
        this.insulationResistance = insulationResistance;
        this.maxInsulationVoltage = maxInsulationVoltage;
        this.onOverheated = onOverheated;
        this.maxTemperature = maxTemperature;
        this.sag = sag;
        this.maxLength = maxLength;
    }

    public Item getDrops() {
        return droppedItem.get();
    }

    public Item getSpooledItem() {
        return spoolItem.get();
    }

    public double getMaxTemperature() {
        return maxTemperature.getAsDouble();
    }

    public WireType replaceOverheatedWith() {
        return onOverheated.get();
    }

    public double getResistance() {
        return resistance.getAsDouble();
    }

    public double insulationResistance() {
        return insulationResistance;
    };

    public double maxInsulationVoltage() {
        return maxInsulationVoltage;
    };

    public PartialModel getModel() {
        return model;
    }

    public float getSag() {
        return sag;
    }

    public float getMaxLength() {
        return maxLength.getAsInt();
    }

    public boolean insulated() {
        return insulationResistance > 1;
    }
}
