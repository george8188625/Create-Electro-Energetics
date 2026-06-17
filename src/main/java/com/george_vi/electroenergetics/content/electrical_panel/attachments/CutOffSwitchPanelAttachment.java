package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelLayoutType;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
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

public class CutOffSwitchPanelAttachment extends PanelAttachment implements ElectricalPanelLink {
    public SwitchingBehaviour behaviour;
    public boolean isClosed;
    Style style = Style.LEVER;
    public DyeColor color;

    public float leverAngle = 0;
    public float prevLeverAngle = 0;

    public CutOffSwitchPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        renderLinkAntenna(be, ms, buffer, light);

        CachedBuffers.partial(miniature() ? style.miniatureBody : style.body, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        float angle = Mth.lerp(partialTicks, prevLeverAngle, leverAngle);
        switch (style) {
            case LEVER -> {
                if (color == null)
                    CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER, be.getBlockState())
                            .translate(5/16f, 8/16f, 10/16f)
                            .rotateXDegrees(angle)
                            .translate(miniature() ? -6/16f : -5/16f, -8/16f, -10/16f)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
                else
                    CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER_DYEABLE, be.getBlockState())
                            .translate(5/16f, 8/16f, 10/16f)
                            .rotateXDegrees(angle)
                            .translate(miniature() ? -6/16f : -5/16f, -8/16f, -10/16f)
                            .color(color.getFireworkColor())
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
            }
            case BUTTON -> {
                if (color == null)
                    CachedBuffers.partial(miniature() ?
                                    CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON :
                                    CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON, be.getBlockState())
                            .translate(0, 0, angle)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
                else
                    CachedBuffers.partial(miniature() ?
                                    CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON_DYEABLE :
                                    CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE, be.getBlockState())
                            .translate(0, 0, angle)
                            .color(color.getFireworkColor())
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
            }
            case DIAL -> {
                if (color == null)
                    CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL, be.getBlockState())
                            .translate(miniature() ? 4/16f : 5/16f, 8/16f, 10/16f)
                            .rotateZDegrees(angle)
                            .translate(-5/16f, -8/16f, -10/16f)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
                else
                    CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL_DYEABLE, be.getBlockState())
                            .translate(miniature() ? 4/16f : 5/16f, 8/16f, 10/16f)
                            .rotateZDegrees(angle)
                            .translate(-5/16f, -8/16f, -10/16f)
                            .color(color.getFireworkColor())
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
            }
        }
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevLeverAngle = leverAngle;
        float target = switch (style) {
            case LEVER, DIAL -> isClosed ? 45 : -45;
            case BUTTON -> isClosed ? (miniature() ? 0.99f : 1.99f)/16f : 0f;
        };

        leverAngle = Mth.lerp(style == Style.DIAL ? 0.5f : 1f, leverAngle, target);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(r));
    }

    @Override
    public void postTick(SimulationResults results) {
        double voltage = results.getVoltageAt(nodes[0], nodes[1]);
        this.behaviour.isClosed = isClosed;
        this.behaviour.postTick(voltage, getCenter(), level);
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

        if (!level.isClientSide()) {
            CEESoundEvents.playOnServer(level, pos, isClosed ? CEESoundEvents.CONTACT_OPEN.get() : CEESoundEvents.CONTACT_CLOSE.get(), 1f, 1f);
            isClosed ^= true;
            updateLinkState();
            sendData();
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void initialize() {
        updateLinkState();
    }

    @Override
    public int getTransmittedStrength() {
        return isClosed && isAlive() ? 15 : 0;
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
        if (!clientPacket)
            behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
        color = DyeColor.byName(tag.getString("Color"), null);
        isClosed = tag.getBoolean("IsClosed");
        style = Style.byId(tag.getString("Style"));
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (isClosed)
            tag.putBoolean("IsClosed", true);
        if (style != Style.LEVER)
            tag.putString("Style", style.getSerializedName());
        if (color != null)
            tag.putString("Color", color.getSerializedName());
        writeLinkFrequencies(tag, registries);
    }

    private boolean miniature() {
        return slot.layoutType() == ElectricalPanelLayoutType.THIRD;
    }

    ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    private enum Style implements StringRepresentable {
        LEVER(CEEPartialModels.PANEL_ATTACHMENT_SMOL_CUT_OFF_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER_DYEABLE),
        BUTTON(CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON,
                CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE),
        DIAL(CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL_DYEABLE);

        public final PartialModel miniatureBody;
        public final PartialModel body;
        public final PartialModel dial;
        public final PartialModel dialDyeable;

        Style(PartialModel miniatureBody, PartialModel body, PartialModel dial, PartialModel dialDyeable) {
            this.miniatureBody = miniatureBody;
            this.body = body;
            this.dial = dial;
            this.dialDyeable = dialDyeable;
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
