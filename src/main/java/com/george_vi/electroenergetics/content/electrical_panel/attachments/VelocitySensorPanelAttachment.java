package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class VelocitySensorPanelAttachment extends PanelAttachment {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public boolean wide;
    SpeedUnit speedUnit = SpeedUnit.METERS_PER_S;

    public double value;

    public VelocitySensorPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) {
            value = 0;
            return;
        }

        Vec3 prevPosition = subLevel.lastPose().transformPosition(pos.getCenter());
        Vec3 position = subLevel.logicalPose().transformPosition(pos.getCenter());

        double distance = position.distanceTo(prevPosition);
        // distance meters / tick
        value = distance * 20;
        // value meters / second

        dialTarget = (float) Mth.clamp(value / 60f, 0, 1);

        prevDialState = dialState;
        dialState += (dialTarget - dialState) * .125f;
        if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
            dialState -= (dialState - 1) * level.random.nextFloat();
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);
        float progress = Mth.lerp(partialTicks, prevDialState, dialState);

        if (wide) {
            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_VELOCITY_SENSOR_WIDE, be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_DIAL, be.getBlockState())
                    .translate(3.5 / 16f, 8 / 16f, 8.75 / 16f)
                    .rotateZ(Mth.PI * -progress + Mth.HALF_PI)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        } else {
            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_VELOCITY_SENSOR, be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_DIAL, be.getBlockState())
                    .translate(3.5 / 16f, 6.5 / 16f, 8.75 / 16f)
                    .rotateZ(Mth.PI / 2 * progress)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (!level.isLoaded(pos))
            return;

        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) {
            value = 0;
            return;
        }

        Vec3 prevPosition = subLevel.lastPose().transformPosition(pos.getCenter());
        Vec3 position = subLevel.logicalPose().transformPosition(pos.getCenter());

        double distance = position.distanceTo(prevPosition);
        // distance meters / tick
        value = distance * 20;
        // value meters / second
    }

    @Override
    public void postTick(SimulationResults results) {
    }

    @Override
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (AllItems.WRENCH.isIn(stack)) {
            speedUnit = SpeedUnit.values()[(speedUnit.ordinal() + 1) % SpeedUnit.values().length];
            sendData();
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem() == type.item.asItem() && slot.isHorizontal) {
            wide ^= true;
            sendData();
            return ItemInteractionResult.SUCCESS;
        }

        return super.onInteract(stack, player, hand, hitResult);
    }

    @Override
    public boolean addToGoggleTooltip(ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (label != null)
            CEELang.builder()
                    .text(label)
                    .forGoggles(tooltip);
        else
            CreateLang.translate("gui.gauge.info_header")
                    .forGoggles(tooltip);

        double v = value;
        Lang.builder(CreateElectroEnergetics.ID)
                .text(TooltipHelper.makeProgressBar(3, dialState < 0.01 ? 0 : dialState < 0.33 ? 1 : dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v * speedUnit.multiplier))
                .text(speedUnit.suffix)
                .style(dialState < 0.01 ? ChatFormatting.DARK_GRAY :
                        dialState < 0.33f ? ChatFormatting.GREEN :
                        dialState < 0.66f ? (ChatFormatting.AQUA) :
                        (ChatFormatting.LIGHT_PURPLE))
                .forGoggles(tooltip);
        return true;
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        value = tag.getDouble("Value");
        wide = tag.getBoolean("Wide");
        speedUnit = SpeedUnit.values()[Mth.clamp(tag.getInt("SpeedUnit"), 0, SpeedUnit.values().length - 1)];
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        tag.putDouble("Value", value);
        if (wide)
            tag.putBoolean("Wide", true);
        if (speedUnit != SpeedUnit.METERS_PER_S)
            tag.putInt("SpeedUnit", speedUnit.ordinal());
    }

    enum SpeedUnit {
        METERS_PER_S(1, " m/s"),
        KM_PER_H(3.6f, " km/h"),

        MILES_PER_H(2.237f, isAprilFools() ? " burgers per bald eagle" : " mph"), // who even uses this unit am I right
        ;

        public final float multiplier;
        public final String suffix;

        SpeedUnit(float multiplier, String suffix) {
            this.multiplier = multiplier;
            this.suffix = suffix;
        }

        private static boolean isAprilFools() {
            LocalDate localdate = LocalDate.now();
            int day = localdate.getDayOfMonth();
            Month month = localdate.getMonth();
            return month == Month.APRIL && day == 1;
        }
    }
}
