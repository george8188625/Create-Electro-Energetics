package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnalogLeverPanelAttachment extends PanelAttachment implements ElectricalPanelLink {
    public int prevRedstoneSignal = -1;
    public int redstoneSignal;

    public DyeColor color;
    Style style = Style.LEVER;

    // Rendering
    public float leverAngle = 0;
    public float prevLeverAngle = 0;

    public AnalogLeverPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        renderLinkAntenna(be, ms, buffer, light);

        CachedBuffers.partial(miniature() || slot.isSixth() ?
                        CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_SMOL :
                        CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        float angle = Mth.lerp(partialTicks, prevLeverAngle, leverAngle);

        if (style.lever != null) {
            CachedBuffers.partial(style.lever, be.getBlockState())
                    .translate(5 / 16f, 8 / 16f, 10 / 16f)
                    .rotateXDegrees(angle)
                    .translate(miniature() ? -6 / 16f : -5 / 16f, -8 / 16f, -10 / 16f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
        }

        if (color == null)
            CachedBuffers.partial(style.leverHead, be.getBlockState())
                    .translate(5 / 16f, 8 / 16f, 10 / 16f)
                    .rotateXDegrees(angle)
                    .translate(miniature() ? -6 / 16f : -5 / 16f, -8 / 16f, -10 / 16f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
        else
            CachedBuffers.partial(style.leverHeadDyeable, be.getBlockState())
                    .translate(5 / 16f, 8 / 16f, 10 / 16f)
                    .rotateXDegrees(angle)
                    .translate(miniature() ? -6 / 16f : -5 / 16f, -8 / 16f, -10 / 16f)
                    .color(color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevLeverAngle = leverAngle;
        float target = Math.clamp(redstoneSignal / 15f, 0f, 1f) * 90 - 45;

        leverAngle = Mth.lerp(0.5f, leverAngle, target);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (prevRedstoneSignal != redstoneSignal) {
            prevRedstoneSignal = redstoneSignal;
            updateLinkState();
        }
    }

    @Override
    public void postTick(SimulationResults results) {
    }

    @Override
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                AllItems.WRENCH.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (AllBlocks.REDSTONE_LINK.isIn(stack)) {
            if (!level.isClientSide && player instanceof ServerPlayer && player.mayBuild())
                player.openMenu(this, buf -> {
                    for (int i = 0; i < 2; i++)
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, getLinkFrequencies()[i]);
                });
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem() == type.item.asItem()) {
            style = Style.values()[(style.ordinal() + 1) % Style.values().length];
            prevLeverAngle = 0;
            leverAngle = 0;
            sendData();
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof DyeItem di) {
            color = di.getDyeColor();
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return ItemInteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            if (!CEEHoldInteractionHandler.isInteracting())
                CEEHoldInteractionHandler.startInteraction(new AnalogLeverHoldInteraction(pos, slot.ordinal(),
                        redstoneSignal));
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void initialize() {
        updateLinkState();
    }

    @Override
    public int getTransmittedStrength() {
        if (!isAlive())
            return 0;
        return Mth.clamp(redstoneSignal, 0, 15);
    }

    @Override
    public BlockPos getLocation() {
        return pos;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public void onRemoved(Player player) {
        super.onRemoved(player);
        if (!level.isClientSide)
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(getLevel(), this);
    }

    @Override
    public void updateLinkState() {
        ElectricalPanelLink.super.updateLinkState();
        sendData();
    }

    @Override
    public void removeLinkState() {
        ElectricalPanelLink.super.removeLinkState();
        sendData();
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        color = DyeColor.byName(tag.getString("Color"), null);
        redstoneSignal = tag.getByte("RedstoneSignal");
        style = Style.byId(tag.getString("Style"));
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (redstoneSignal != 0)
            tag.putByte("RedstoneSignal", (byte) redstoneSignal);
        if (style != Style.LEVER)
            tag.putString("Style", style.getSerializedName());
        if (color != null)
            tag.putString("Color", color.getSerializedName());
        writeLinkFrequencies(tag, registries);
    }

    private boolean miniature() {
        return slot.fullWidth == 14;
    }

    ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    private enum Style implements StringRepresentable {
        THROTTLE(CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_METAL_THINGY,
                CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_THROTTLE_HEAD,
                CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_THROTTLE_HEAD_DYEABLE),
        LEVER(null,
                CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_LEVER,
                CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_LEVER_DYEABLE),
        BALL(CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_METAL_THINGY,
                CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_BALL_HEAD,
                CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_BALL_HEAD_DYEABLE);

        @Nullable public final PartialModel lever;
        public final PartialModel leverHead;
        public final PartialModel leverHeadDyeable;

        Style(@Nullable PartialModel lever, PartialModel leverHead, PartialModel leverHeadDyeable) {
            this.lever = lever;
            this.leverHead = leverHead;
            this.leverHeadDyeable = leverHeadDyeable;
        }


        @Override
        public @NotNull String getSerializedName() {
            return Lang.asId(name());
        }

        public static Style byId(String id) {
            for (Style type : values())
                if (type.getSerializedName().equals(id))
                    return type;
            return LEVER;
        }
    }
}
