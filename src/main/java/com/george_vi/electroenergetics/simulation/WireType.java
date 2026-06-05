package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.config.CEEConfigs;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class WireType {

    final DoubleSupplier resistance;
    final PartialModel model;
    final Supplier<Item> droppedItem;
    final Supplier<Item> spoolItem;
    final double insulationResistance;
    final DoubleSupplier maxInsulationVoltage;
    final Supplier<WireType> overheatedReplacement;
    final TagKey<Item> droppedTag;

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
    final float thickness;
    final boolean isDecorative;
    final boolean isInvulnerable;
    final WireRenderType renderType;
    final PartialModel endPointModel;

    private WireType(DoubleSupplier resistance, PartialModel model, Supplier<Item> droppedItem,
                     Supplier<Item> spoolItem, double insulationResistance, DoubleSupplier maxInsulationVoltage,
                     Supplier<WireType> overheatedReplacement, TagKey<Item> droppedTag, DoubleSupplier maxTemperature,
                     float sag, IntSupplier maxLength, float thickness, boolean isDecorative, boolean isInvulnerable, WireRenderType renderType, PartialModel endPointModel) {
        this.renderType = renderType;
        this.endPointModel = endPointModel;
        if (droppedItem == null && droppedTag == null)
            droppedItem = () -> Items.AIR;

        this.resistance = resistance;
        this.model = model;
        this.droppedItem = droppedItem;
        this.spoolItem = spoolItem;
        this.insulationResistance = insulationResistance;
        this.maxInsulationVoltage = maxInsulationVoltage;
        this.overheatedReplacement = overheatedReplacement;
        this.droppedTag = droppedTag;
        this.maxTemperature = maxTemperature;
        this.sag = sag;
        this.maxLength = maxLength;
        this.thickness = thickness;
        this.isDecorative = isDecorative;
        this.isInvulnerable = isInvulnerable;
    }

    public Item getDrops() {
        return droppedItem == null ? CEETags.itemFromTag(droppedTag) : droppedItem.get();
    }

    public @Nullable TagKey<Item> getDroppedTag() {
        return droppedTag;
    }

    public Item getSpooledItem() {
        return spoolItem.get();
    }

    public double getMaxTemperature() {
        return maxTemperature.getAsDouble();
    }

    public WireType overheatedReplacement() {
        return overheatedReplacement.get();
    }

    public double getResistance() {
        return resistance.getAsDouble();
    }

    public double insulationResistance() {
        return insulationResistance;
    }

    public double maxInsulationVoltage() {
        return maxInsulationVoltage.getAsDouble();
    }

    public PartialModel getModel() {
        return model;
    }

    @Nullable
    public PartialModel getEndPointModel() {
        return endPointModel;
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

    public float getThickness() {
        return thickness;
    }

    @SuppressWarnings("unused")
    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    @SuppressWarnings("unused")
    public boolean isDecorative() {
        return isDecorative;
    }

    @OnlyIn(Dist.CLIENT)
    public RenderType renderType() {
        return switch (renderType) {
            case SOLID, SOLID_NOT_SCALED -> RenderType.solid();
            case CUTOUT, CUTOUT_NOT_SCALED -> RenderType.cutout();
            case TRANSLUCENT, TRANSLUCENT_NOT_SCALED -> RenderType.translucent();
        };
    }

    public boolean shouldScaleLast() {
        return switch (renderType) {
            case SOLID, CUTOUT, TRANSLUCENT -> true;
            default -> false;
        };
    }


    public static class Builder {

        DoubleSupplier resistance = () -> CEEConfigs.server().resistanceValues.wireResistance.get();
        PartialModel model;
        PartialModel endPointModel;
        Supplier<Item> droppedItem = null;
        Supplier<Item> spoolItem = () -> Items.AIR;
        double insulationResistance = 0;
        DoubleSupplier maxInsulationVoltage = () -> 0;
        Supplier<WireType> overheatedReplacement = () -> null;
        DoubleSupplier maxTemperature = () -> 1000;
        float sag = 1;
        IntSupplier maxLength = () -> CEEConfigs.server().maxWireLength.get();
        float thickness = 1/16f;

        Function<DyeColor, WireType> dyeTypeFunction;
        DyeColor dyeColor;
        boolean decorative = false;
        boolean invulnerable = false;
        TagKey<Item> droppedTag = null;

        WireRenderType renderType = WireRenderType.SOLID;

        public WireType build() {
            if (dyeTypeFunction != null)
                return new Dyeable(resistance, model, droppedItem, spoolItem, insulationResistance,
                        maxInsulationVoltage, overheatedReplacement, droppedTag, maxTemperature, sag, maxLength,
                        thickness, dyeTypeFunction, dyeColor, renderType, endPointModel);
            return new WireType(resistance, model, droppedItem, spoolItem, insulationResistance, maxInsulationVoltage,
                    overheatedReplacement, droppedTag, maxTemperature, sag, maxLength, thickness, decorative,
                    invulnerable, renderType, endPointModel);
        }

        public Builder(PartialModel model) {
            this.model = model;
        }

        public Builder resistance(DoubleSupplier v) {
            resistance = v;
            return this;
        }

        public Builder droppedItem(Supplier<Item> v) {
            droppedItem = v;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder endPointItem(PartialModel model) {
            endPointModel = model;
            return this;
        }

        public Builder droppedTag(TagKey<Item> v) {
            droppedTag = v;
            return this;
        }

        public Builder spoolItem(Supplier<Item> v) {
            spoolItem = v;
            return this;
        }

        public Builder insulationResistance(double v) {
            insulationResistance = v;
            return this;
        }

        public Builder sag(float v) {
            sag = v;
            return this;
        }

        public Builder thickness(float v) {
            thickness = v;
            return this;
        }

        public Builder maxTemperature(DoubleSupplier v) {
            maxTemperature = v;
            return this;
        }

        public Builder maxInsulationVoltage(DoubleSupplier v) {
            maxInsulationVoltage = v;
            return this;
        }

        public Builder replaceOnOverheated(Supplier<WireType> v) {
            overheatedReplacement = v;
            return this;
        }

        public Builder maxLength(IntSupplier v) {
            maxLength = v;
            return this;
        }

        public Builder dyeable(Function<DyeColor, WireType> dyeTypeFunction) {
            this.dyeTypeFunction = dyeTypeFunction;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder dyeable(Function<DyeColor, WireType> dyeTypeFunction, DyeColor color) {
            this.dyeTypeFunction = dyeTypeFunction;
            dyeColor = color;
            return this;
        }

        public Builder dyeable(DeferredHolder<WireType, WireType>[] allDyes) {
            return dyeable((dye) -> allDyes[dye.ordinal()].get());
        }

        public Builder dyeable(DeferredHolder<WireType, WireType>[] allDyes, DyeColor color) {
            dyeColor = color;
            return dyeable(allDyes);
        }

        public Builder invulnerable() {
            invulnerable = true;
            return this;
        }

        public Builder decorative() {
            decorative = true;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder renderType(WireRenderType renderType) {
            this.renderType = renderType;
            return this;
        }
    }

    public static class Dyeable extends WireType {
        private final Function<DyeColor, WireType> typeFunction;
        private final DyeColor dyeColor;

        private Dyeable(DoubleSupplier resistance, PartialModel model, Supplier<Item> droppedItem,
                        Supplier<Item> spoolItem, double insulationResistance, DoubleSupplier maxInsulationVoltage,
                        Supplier<WireType> overheatedReplacement, TagKey<Item> droppedTag,
                        DoubleSupplier maxTemperature, float sag, IntSupplier maxLength, float thickness,
                        Function<DyeColor, WireType> typeFunction, @Nullable DyeColor dyeColor,
                        WireRenderType renderType, PartialModel endpointModel) {
            super(resistance, model, droppedItem, spoolItem, insulationResistance, maxInsulationVoltage,
                    overheatedReplacement, droppedTag, maxTemperature, sag, maxLength, thickness, false,
                    false, renderType, endpointModel);

            this.typeFunction = typeFunction;
            this.dyeColor = dyeColor;
        }

        public WireType getDyed(DyeColor color) {
            return typeFunction.apply(color);
        }

        public DyeColor getColor() {
            return dyeColor;
        }

        @SuppressWarnings("unused")
        public boolean isDyed() {
            return dyeColor != null;
        }

    }

    /**
     * For render types, this is used instead of {@link RenderType} to not cause issues with dedicated servers
     * <br>
     * For not scaled wire types, the first and last segment aren't rendered
     */
    public enum WireRenderType {
        SOLID,
        SOLID_NOT_SCALED,
        CUTOUT,
        CUTOUT_NOT_SCALED,
        TRANSLUCENT,
        TRANSLUCENT_NOT_SCALED
    }
}
