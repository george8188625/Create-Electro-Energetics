package com.george_vi.electroenergetics.content.linemans_stick;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class LinemansStickClientHandler {

    @OnlyIn(Dist.CLIENT)
    public static Vec3 linemansStickTarget = Vec3.ZERO;

    @OnlyIn(Dist.CLIENT)
    public static Vec3 prevLinemansStickTarget = Vec3.ZERO;

    @OnlyIn(Dist.CLIENT)
    public static LinemansStickMode currentMode = LinemansStickMode.NONE;

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.hitResult == null)
            return;

        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;

        ItemStack heldItemStack = player.getMainHandItem();
        if (heldItemStack.getItem() != CEEItems.LINEMANS_STICK.get())
            return;

        ItemStack offHandItemStack = player.getOffhandItem();

        boolean isShiftKeyDown = player.isShiftKeyDown();

        if (currentMode == LinemansStickMode.NONE) {
            if (offHandItemStack.getItem() instanceof ClampMeterItem)
                currentMode = LinemansStickMode.CLAMP_METER;
            else if (isShiftKeyDown && offHandItemStack.isEmpty())
                currentMode = LinemansStickMode.ATTACHMENT_REMOVAL;
            else if (!isShiftKeyDown && offHandItemStack.is(CEETags.WIRE_ATTACHMENT))
                currentMode = LinemansStickMode.ATTACHMENT_INSTALLATION;
        }

        else if (currentMode == LinemansStickMode.CLAMP_METER &&
                !(offHandItemStack.getItem() instanceof ClampMeterItem))
            currentMode = LinemansStickMode.NONE;
        else if (currentMode == LinemansStickMode.ATTACHMENT_REMOVAL &&
                !(isShiftKeyDown && offHandItemStack.isEmpty()))
            currentMode = LinemansStickMode.NONE;
        else if (currentMode == LinemansStickMode.ATTACHMENT_INSTALLATION &&
                (isShiftKeyDown || !offHandItemStack.is(CEETags.WIRE_ATTACHMENT)))
            currentMode = LinemansStickMode.NONE;

        if (mc.hitResult instanceof BlockHitResult result) {

            BlockPos pos = result.getBlockPos();
            BlockState state = level.getBlockState(pos);

            InWorldNode node = InWorldNode.closestNode(level, pos, state, 0.4f, result.getLocation());

            if (currentMode == LinemansStickMode.NONE && node != null) {
                Vec3 nodeLocation = node.getPosition(level);
                if (nodeLocation != null) {
                    prevLinemansStickTarget = linemansStickTarget;
                    linemansStickTarget = nodeLocation;
                    return;
                }
            }

            if (WireInteractionHandler.targetedPoint != null) {
                InWorldNode node1 = WireInteractionHandler.targetedPoint.node1();
                InWorldNode node2 = WireInteractionHandler.targetedPoint.node2();
                Vec3 pos1 = node1.getPosition(level);
                Vec3 pos2 = node2.getPosition(level);
                Vec3 target = WireInteractionHandler.targetedPos;
                prevLinemansStickTarget = linemansStickTarget;
                linemansStickTarget = target;
                return;
            }
        } else if (mc.hitResult instanceof EntityHitResult result) {

            if (WireInteractionHandler.targetedPoint != null) {
                InWorldNode node1 = WireInteractionHandler.targetedPoint.node1();
                InWorldNode node2 = WireInteractionHandler.targetedPoint.node2();
                Vec3 pos1 = node1.getPosition(level);
                Vec3 pos2 = node2.getPosition(level);
                Vec3 target = WireInteractionHandler.targetedPos;
                prevLinemansStickTarget = linemansStickTarget;
                linemansStickTarget = target;
                return;
            }
        }

        prevLinemansStickTarget = linemansStickTarget;
        linemansStickTarget = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position)mc.hitResult.getLocation());

    }
}
