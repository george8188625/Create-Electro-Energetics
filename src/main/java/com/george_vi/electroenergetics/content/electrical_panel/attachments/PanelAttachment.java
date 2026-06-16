package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.content.electrical_panel.*;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PanelAttachment {
    public InWorldNode[] nodes;
    public BlockPos pos;
    public Level level;
    public ElectricalPanelSlot slot;
    public Direction panelFacing;
    public final PanelAttachmentType type;
    public String label;

    public PanelAttachment(PanelAttachmentType type) {
        this.type = type;
    }

    /**
     * Called on the logical client only when the position is loaded. Ticked by the BE.
     */
    @OnlyIn(Dist.CLIENT)
    public void tickClient(ElectricalPanelBlockEntity be) {

    }

    @OnlyIn(Dist.CLIENT)
    public abstract void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                                MultiBufferSource buffer, int light, int overlay);

    @OnlyIn(Dist.CLIENT)
    public void transformPose(PoseStack ms, ElectricalPanelBlockEntity be) {
        Direction facing = be.getBlockState().getValue(ElectricalPanelBlock.FACING);
        TransformStack.of(ms)
                .rotateYCenteredDegrees(-facing.toYRot() + 180)
                .translateY(
                        slot == ElectricalPanelSlot.HALF_LOWER ? -6/16f :
                        slot == ElectricalPanelSlot.HALF_UPPER ? 6/16f : 0)
                .rotateZCenteredDegrees(slot.layoutType() == ElectricalPanelLayoutType.HALF_HORIZONTAL ? 90 : 0)
                .translateX((float) slot.leftOffset);
    }

    /**
     * Called on the logical server only. Ticked by the device.
     */
    public abstract void preTick(BridgeCollector bridges);

    /**
     * Called on the logical server only. Ticked by the device.
     */
    public abstract void postTick(SimulationResults results);

    public void read(CompoundTag tag, boolean clientPacket) {

    }

    public void write(CompoundTag tag, boolean clientPacket) {

    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public boolean addToTooltip(ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (label != null) {
            CEELang.builder()
                    .text(label)
                    .forGoggles(tooltip);
            return true;
        }
        return false;
    }

    /**
     * Called both on the logical client and server.
     */
    public void onInserted(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {

    }

    /**
     * Called both on the logical client and server.
     * @return null if nothing changed (doesn't send BE data), otherwise the interaction result.
     */
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    /**
     * @return the center position of this attachment
     */
    public @NotNull Vec3 getCenter() {
        return VecHelper.rotateCentered(slot.center, -panelFacing.toYRot() + 180, Direction.Axis.Y).add(Vec3.atLowerCornerOf(pos));
    }

    public void sendData() {
        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be)
                be.sendData();
    }

    @SuppressWarnings("unused")
    public void onRemoved(Player player) {

    }

    public List<ItemStack> getDrops() {
        return List.of(defaultDroppedStack());
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public void renderLabel(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                            MultiBufferSource buffer, int light, int overlay) {
        Direction facing = be.getBlockState().getValue(ElectricalPanelBlock.FACING);
        boolean isHorizontal = be.getLayoutType() == ElectricalPanelLayoutType.HALF_HORIZONTAL;
        int fullWidth = slot.layoutType() == ElectricalPanelLayoutType.THIRD ? 14 : be.getLayoutType() == ElectricalPanelLayoutType.FULL ? 48 : 24;
        if (isHorizontal)
            fullWidth = 48;
        float scale = 1 / 128f;
        float y = be.getLayoutType() == ElectricalPanelLayoutType.FULL ? 2/16f : 4/16f;
        Minecraft mc = Minecraft.getInstance();
        int width = mc.font.width(label);
        float leftOffset = 2 / 16f;
        if (width > fullWidth * 2) {
            scale /= 2;
            leftOffset = 3.2f / 16f;
        }
        TransformStack.of(ms)
                .rotateYCenteredDegrees(-facing.toYRot() + 180)
                .translate(
                        (isHorizontal ? 0 : (float) slot.leftOffset) + leftOffset,
                        (isHorizontal ? -slot.leftOffset + 9/16f : y),
                        isHorizontal ? 9/16f : 11/16f
                )
                .scale(-scale, -scale, -scale);

        mc.font.drawInBatch(label, -fullWidth - width / 2f, 0, 0xffffff, true, ms.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }

    public @NotNull ItemStack defaultDroppedStack() {
        ItemStack stack = new ItemStack(type.item.asItem());
        if (label != null)
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(label));
        return stack;
    }

    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return CEELang.nodeLabel("node");
    }

    @SuppressWarnings("unused")
    public float getNodeSize(Level level, BlockPos pos, BlockState state, int id) {
        return slot.nodeSize;
    }
}
