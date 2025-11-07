package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEERegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class WireAttachment {
    public final WireAttachmentType type;
    public CompoundTag data = new CompoundTag();

    public static StreamCodec<ByteBuf, WireAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, WireAttachment::write,
            WireAttachment::read
    );

    public WireAttachment(WireAttachmentType type) {
        this.type = type;
    }

    public static WireAttachment read(CompoundTag tag) {
        WireAttachment attachment = new WireAttachment(CEERegistries.WIRE_ATTACHMENT_TYPE.get(ResourceLocation.parse(tag.getString("ID"))));
        attachment.data = tag;
        return attachment;
    }

    public CompoundTag write() {
        CompoundTag tag = data.copy();
        tag.putString("ID", CEERegistries.WIRE_ATTACHMENT_TYPE.getKey(type).toString());
        return tag;
    }

    public float getWidth() {
        return type.getWidth(this);
    }

    public List<ItemStack> getDrops(Level level) {
        return type.getDrops(this, level);
    }

    public List<ItemStack> getItemRequirement() {
        return type.getItemRequirements(this);
    }
}
