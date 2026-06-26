package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
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

public class SteeringWheelPanelAttachment extends PanelAttachment implements ElectricalPanelLink, IAnalogPanelAttachment {
    public int prevRedstoneSignal = -1;
    public int redstoneSignal;

    public DyeColor color;

    // Rendering
    public float leverAngle = 0;
    public float prevLeverAngle = 0;

    public SteeringWheelPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        float angle = Mth.lerp(partialTicks, prevLeverAngle, leverAngle);
        CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL, be.getBlockState())
                .rotateZCenteredDegrees(angle)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        if (color == null)
            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL_HANDLE, be.getBlockState())
                    .rotateZCenteredDegrees(angle)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
        else
            CachedBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL_HANDLE_DYEABLE, be.getBlockState())
                    .rotateZCenteredDegrees(angle)
                    .color(color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        ms.translate(4/16f, -2/16f, 0);
        renderLinkAntenna(be, ms, buffer, light);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevLeverAngle = leverAngle;
        float target = Math.clamp(redstoneSignal / 15f, 0f, 1f) * 180 - 90;

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

        if (stack.getItem() instanceof DyeItem di) {
            color = di.getDyeColor();
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return ItemInteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            if (!CEEHoldInteractionHandler.isInteracting())
                CEEHoldInteractionHandler.startInteraction(new SteeringWheelHoldInteraction(pos, slot.ordinal(),
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
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (redstoneSignal != 0)
            tag.putByte("RedstoneSignal", (byte) redstoneSignal);
        if (color != null)
            tag.putString("Color", color.getSerializedName());
        writeLinkFrequencies(tag, registries);
    }

    ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    @Override
    public int getAnalogState() {
        return redstoneSignal;
    }

    @Override
    public void setAnalogState(int state) {
        redstoneSignal = state;
    }
}
