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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class MomentarySwitchPanelAttachment extends PanelAttachment implements ElectricalPanelLink {

    public SwitchingBehaviour behaviour;
    public int closedTicks;
    public DyeColor color;

    public float offsetState = 0;
    public float prevOffsetState = 0;

    public MomentarySwitchPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);
        renderLinkAntenna(be, ms, buffer, light);

        CachedBuffers.partial(miniature() ? (slot.isSixth() ? CEEPartialModels.PANEL_ATTACHMENT_TINY_INDICATOR_BULB : CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH) : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        float offset = Mth.lerp(partialTicks, prevOffsetState, offsetState);
        if (color == null)
            CachedBuffers.partial(miniature() ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON, be.getBlockState())
                    .translate(0, 0, offset)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
        else
            CachedBuffers.partial(miniature() ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON_DYEABLE : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE, be.getBlockState())
                    .translate(0, 0, offset)
                    .color(color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevOffsetState = offsetState;
        offsetState = closedTicks > 0 ? (miniature() ? 0.99f : 1.99f)/16f : 0f;
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
        this.behaviour.isClosed = this.closedTicks > 0;
        this.behaviour.postTick(voltage, getCenter(), level);
        if (this.closedTicks == 0) {
            CEESoundEvents.playOnServer(level, pos, CEESoundEvents.CONTACT_OPEN.get(), 1, 1);
            sendData();
            updateLinkState();
        }
        this.closedTicks = Math.max(-1, this.closedTicks - 1);
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

        if (stack.getItem() instanceof DyeItem di) {
            color = di.getDyeColor();
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return ItemInteractionResult.SUCCESS;
        }

        boolean swing = closedTicks == -1;
        if (!level.isClientSide()) {
            if (closedTicks == -1)
                CEESoundEvents.playOnServer(level, pos, CEESoundEvents.CONTACT_CLOSE.get(), 1f, 1f);
            closedTicks = 4;
            updateLinkState();
            sendData();
        }
        return swing ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
    }


    @Override
    public void initialize() {
        updateLinkState();
    }

    @Override
    public int getTransmittedStrength() {
        return closedTicks > 0 && isAlive() ? 15 : 0;
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
        closedTicks = tag.contains("ClosedTicks") ? tag.getInt("ClosedTicks") : -1;
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (closedTicks != -1)
            tag.putInt("ClosedTicks", closedTicks);
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
}
