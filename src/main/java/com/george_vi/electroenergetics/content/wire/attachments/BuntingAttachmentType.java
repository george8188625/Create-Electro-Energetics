package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.BuntingBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.util.Collections;
import java.util.List;

public class BuntingAttachmentType extends WireAttachmentType {
    @Override
    public float getWidth(WireAttachment attachment) {
        return 0.5f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 0.5f;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (!ModList.get().isLoaded("supplementaries"))
            return;
        PoseTransformStack msr = TransformStack.of(pose);
        msr.rotateXDegrees(180);
        msr.rotateZDegrees(pitch);
        msr.translate(0.25, -0.19, 0);

        DyeColor color = DyeColor.byName(attachment.data.getString("BaseColor"), DyeColor.WHITE);
        BuntingBlockTileRenderer.renderBunting(color, Direction.WEST, 0, pose, null, buffer, light, OverlayTexture.NO_OVERLAY, BlockPos.containing(pos), mc.level.getGameTime());
    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        if (!ModList.get().isLoaded("supplementaries"))
            return Collections.emptyList();
        DyeColor color = DyeColor.byName(attachment.data.getString("BaseColor"), DyeColor.WHITE);
        return List.of(ModRegistry.BUNTING_BLOCKS.get(color).get().asItem().getDefaultInstance());
    }

    @Override
    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        if (!ModList.get().isLoaded("supplementaries"))
            return Collections.emptyList();
        DyeColor color = DyeColor.byName(attachment.data.getString("BaseColor"), DyeColor.WHITE);
        return List.of(ModRegistry.BUNTING_BLOCKS.get(color).get().asItem().getDefaultInstance());
    }
}
