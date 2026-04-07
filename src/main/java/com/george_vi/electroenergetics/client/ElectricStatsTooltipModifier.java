package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

public class ElectricStatsTooltipModifier implements TooltipModifier {
    public static final SimpleRegistry<Item, ElectricStatSet> ALL_ENTRIES = SimpleRegistry.create();

    protected final Item item;

    public ElectricStatsTooltipModifier(Item item) {
        this.item = item;
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        List<Component> tooltip = context.getToolTip();

        ElectricStatSet entries = ALL_ENTRIES.get(item);
        if (entries == null)
            return;
        tooltip.add(CommonComponents.EMPTY);
        for (ElectricStatEntry e : entries.entries) {
            CEELang.translate("tooltip." + e.langKey)
                    .style(ChatFormatting.GRAY)
                    .addTo(tooltip);
            LangBuilder builder = CEELang.builder();
            int progress = 1;
            if (e.statType == ElectricStatType.VOLTAGE) {
                progress = Mth.floor(e.value.getAsDouble() / 200);
            } else if (e.statType == ElectricStatType.RESISTANCE) {
                progress = Mth.floor(e.value.getAsDouble() / 300);
            } else if (e.statType == ElectricStatType.RESISTANCE_PER_METER) {
                progress = Mth.floor(e.value.getAsDouble() * 150);
            } else if (e.statType == ElectricStatType.POWER) {
                double val = e.value.getAsDouble();
                progress = val < 500 ? 0 : val < 1000 ? 1 : val < 7500 ? 2 : 3;
            } else if (e.statType == ElectricStatType.MAX_CURRENT) {
                progress = Mth.floor(e.value.getAsDouble() / 100);
            }

            progress = Mth.clamp(progress, 0, 3);

            ChatFormatting style = progress == 0 ? ChatFormatting.AQUA : progress == 1 ? ChatFormatting.YELLOW : progress == 2 ? ChatFormatting.GOLD : ChatFormatting.RED;

            builder.add(CreateLang.text(TooltipHelper.makeProgressBar(3, progress)).style(style));
            if (e.statType == ElectricStatType.VOLTAGE) {
                builder.add(CEELang.formatVoltage(e.value.getAsDouble()));
            } else if (e.statType == ElectricStatType.RESISTANCE) {
                builder.add(CEELang.formatResistance(e.value.getAsDouble()));
            } else if (e.statType == ElectricStatType.RESISTANCE_PER_METER) {
                builder.add(CEELang.formatResistancePerMeter(e.value.getAsDouble()));
            } else if (e.statType == ElectricStatType.POWER) {
                builder.add(CEELang.formatPower(e.value.getAsDouble()));
            } else if (e.statType == ElectricStatType.MAX_CURRENT) {
                builder.add(CEELang.formatAmperage(e.value.getAsDouble()));
            }

            builder.style(style)
                    .addTo(tooltip);

        }
    }

    public enum ElectricStatType {
        VOLTAGE,
        RESISTANCE,
        RESISTANCE_PER_METER,
        MAX_CURRENT,
        POWER,
    }

    public record ElectricStatEntry(ElectricStatType statType, String langKey, DoubleSupplier value) {
        public static ElectricStatEntry resistance(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.RESISTANCE, "resistance", value);
        }

        public static ElectricStatEntry resistancePerMeter(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.RESISTANCE_PER_METER, "resistance", value);
        }

        public static ElectricStatEntry voltage(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.VOLTAGE, "voltage", value);
        }

        public static ElectricStatEntry maxCurrent(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.MAX_CURRENT, "max_current", value);
        }

        public static ElectricStatEntry power(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.POWER, "power", value);
        }

        public static ElectricStatEntry maxPower(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.POWER, "max_power", value);
        }

        public static ElectricStatEntry maxVoltage(DoubleSupplier value) {
            return new ElectricStatEntry(ElectricStatType.VOLTAGE, "max_voltage", value);
        }
    }

    public static class ElectricStatSet {
        List<ElectricStatEntry> entries = new ArrayList<>();
        public ElectricStatSet add(ElectricStatEntry e) {
            entries.add(e);
            return this;
        }

        public ElectricStatSet addResistance(DoubleSupplier v) {
            entries.add(ElectricStatEntry.resistance(v));
            return this;
        }

        public ElectricStatSet addResistancePerMeter(DoubleSupplier v) {
            entries.add(ElectricStatEntry.resistancePerMeter(v));
            return this;
        }

        public ElectricStatSet addVoltage(DoubleSupplier v) {
            entries.add(ElectricStatEntry.voltage(v));
            return this;
        }

        public ElectricStatSet addMaxVoltage(DoubleSupplier v) {
            entries.add(ElectricStatEntry.maxVoltage(v));
            return this;
        }

        public ElectricStatSet addMaxCurrent(DoubleSupplier v) {
            entries.add(ElectricStatEntry.maxCurrent(v));
            return this;
        }

        public ElectricStatSet addPower(DoubleSupplier v) {
            entries.add(ElectricStatEntry.power(v));
            return this;
        }

        public ElectricStatSet addMaxPower(DoubleSupplier v) {
            entries.add(ElectricStatEntry.maxPower(v));
            return this;
        }

        public ElectricStatSet addMaxVoltageOnMaxTemp(DoubleSupplier resistance, double tempThreshold, float factor) {
            entries.add(ElectricStatEntry.voltage(() -> factor * Math.sqrt(tempThreshold * resistance.getAsDouble() / 30 + 3.3 * resistance.getAsDouble())));
            return this;
        }
    }
}
