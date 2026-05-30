package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class MomentarySwitchPanelAttachment extends PanelAttachment {

    public final boolean miniature;
    public SwitchingBehaviour behaviour;
    public int closedTicks;
    public DyeColor color;

    public float offsetState = 0;
    public float prevOffsetState = 0;

    private MomentarySwitchPanelAttachment(PanelAttachmentType type, boolean miniature) {
        super(type);
        this.miniature = miniature;
    }

    public static MomentarySwitchPanelAttachment normal(PanelAttachmentType type) {
        return new MomentarySwitchPanelAttachment(type, false);
    }

    public static MomentarySwitchPanelAttachment miniature(PanelAttachmentType type) {
        return new MomentarySwitchPanelAttachment(type, true);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        CachedBuffers.partial(miniature ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.SOLID));

        float offset = Mth.lerp(partialTicks, prevOffsetState, offsetState);
        if (color == null)
            CachedBuffers.partial(miniature ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON, be.getBlockState())
                    .translate(0, 0, offset)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
        else
            CachedBuffers.partial(miniature ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON_DYEABLE : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE, be.getBlockState())
                    .translate(0, 0, offset)
                    .color(color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID));
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevOffsetState = offsetState;
        offsetState = closedTicks > 0 ? (miniature ? 0.99f : 1.99f)/16f : 0f;
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
        }
        this.closedTicks = Math.max(-1, this.closedTicks - 1);
    }

    @Override
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                AllItems.WRENCH.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

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
            sendData();
        }
        return swing ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        if (!clientPacket)
            behaviour = new SwitchingBehaviour(tag.getCompound("Behaviour"));
        color = DyeColor.byName(tag.getString("Color"), null);
        closedTicks = tag.contains("ClosedTicks") ? tag.getInt("ClosedTicks") : -1;
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (closedTicks != -1)
            tag.putInt("ClosedTicks", closedTicks);
        if (color != null)
            tag.putString("Color", color.getSerializedName());
    }
}
