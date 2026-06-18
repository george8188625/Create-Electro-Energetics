package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElectricalPanelBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation, SpecialBlockEntityItemRequirement {

    // This exists purely to mirror the device's data, or for rendering on the client side.
    // The device actually owns those.
    PanelAttachment[] beAttachments = new PanelAttachment[ElectricalPanelSlot.values().length];

    public ElectricalPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        for (PanelAttachment attachment : beAttachments)
            if (attachment != null)
                attachment.tickClient(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        PanelAttachment[] attachments = getAttachments();
        readAttachments(tag, attachments, worldPosition, level, registries, clientPacket, getBlockState().getValue(ElectricalPanelBlock.FACING));
        if (Arrays.stream(attachments).allMatch(Objects::isNull)) {
            Arrays.fill(attachments, null);
            readLegacyAttachments(tag, attachments, worldPosition, level, registries, getBlockState().getValue(ElectricalPanelBlock.FACING));
        }
    }

    static void readLegacyAttachments(CompoundTag tag, PanelAttachment[] toFill, BlockPos worldPosition, Level level,
                                      HolderLookup.Provider registries, Direction facing) {

        ElectricalPanelLayoutType layoutType = ElectricalPanelLayoutType.byIdOrNone(tag.getString("Layout"));

        for (int i = 0; i < layoutType.slots.length; i++) {
            CompoundTag attachmentTag = tag.getCompound("Attachment" + i);
            if (attachmentTag.isEmpty())
                continue;

            String id = attachmentTag.getString("ID");

            ResourceLocation attachmentId = ResourceLocation.tryParse(id);

            if (attachmentId == null)
                continue;

            PanelAttachmentType type = CEERegistries.PANEL_ATTACHMENT_TYPE.get(attachmentId);

            if (type == null)
                continue;

            if (toFill[layoutType.slots[i].ordinal()] == null) {
                toFill[layoutType.slots[i].ordinal()] = type.createNew(worldPosition, type.mode.getNodesFor(worldPosition, layoutType.slots[i]),
                        level, layoutType.slots[i], facing, registries);
            }
        }
    }


    static void readAttachments(CompoundTag tag, PanelAttachment[] toFill, BlockPos worldPosition, Level level,
                                HolderLookup.Provider registries, boolean clientPacket, Direction facing) {

        for (int i = 0; i < toFill.length; i++) {
            CompoundTag attachmentTag = tag.getCompound("AttachmentSlot" + i);
            if (attachmentTag.isEmpty()) {
                toFill[i] = null;
                continue;
            }

            String id = attachmentTag.getString("ID");

            ResourceLocation attachmentId = ResourceLocation.tryParse(id);
            if (attachmentId == null) {
                toFill[i] = null;
                continue;
            }

            PanelAttachmentType type = CEERegistries.PANEL_ATTACHMENT_TYPE.get(attachmentId);
            if (type == null) {
                toFill[i] = null;
                continue;
            }

            ElectricalPanelSlot slot = ElectricalPanelSlot.values()[i];

            if (toFill[i] == null) {
                toFill[i] = type.createNew(worldPosition, type.mode.getNodesFor(worldPosition, slot),
                        level, slot, facing, registries);
            }

            toFill[i].read(attachmentTag.getCompound("Data"), clientPacket, registries);
            toFill[i].label = attachmentTag.contains("Label") ? attachmentTag.getString("Label") : null;
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        PanelAttachment[] attachments = getAttachments();
        for (int i = 0; i < attachments.length; i++) {
            if (attachments[i] == null)
                continue;

            ResourceLocation id = CEERegistries.PANEL_ATTACHMENT_TYPE.getKey(attachments[i].type);
            if (id == null)
                continue;

            CompoundTag attachmentTag = new CompoundTag();
            tag.put("AttachmentSlot" + i, attachmentTag);

            attachmentTag.putString("ID", id.toString());

            CompoundTag dataTag = new CompoundTag();
            attachmentTag.put("Data", dataTag);
            attachments[i].write(dataTag, clientPacket, registries);
            if (attachments[i].label != null)
                attachmentTag.putString("Label", attachments[i].label);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null)
            return false;
        Vec3 clickedPos = mc.hitResult.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition));
        ElectricalPanelSlot slot = getHoveringAttachmentIndex(clickedPos);
        if (slot == null || beAttachments[slot.ordinal()] == null)
            return false;
        return beAttachments[slot.ordinal()].addToGoggleTooltip(this, tooltip, isPlayerSneaking);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null)
            return false;
        Vec3 clickedPos = mc.hitResult.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition));
        ElectricalPanelSlot slot = getHoveringAttachmentIndex(clickedPos);
        if (slot == null || beAttachments[slot.ordinal()] == null)
            return false;
        return beAttachments[slot.ordinal()].addToTooltip(this, tooltip, isPlayerSneaking);
    }

    public void attachmentUpdate() {
        sendData();
    }

    public PanelAttachment[] getAttachments() {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            return device.attachments;
        return beAttachments;
    }

    public void setAttachments(PanelAttachment[] attachments) {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            device.attachments = attachments;
        this.beAttachments = attachments;
    }

    private ElectricalPanelDevice deviceOrNull() {
        if (level instanceof ServerLevel sl) {
            DevicesSavedData dsd = DevicesSavedData.load(sl);

            return dsd.getDevice(worldPosition, ElectricalPanelDevice.class);
        }

        return null;
    }

    /**
     * @return -1 if no attachment
     */
    @Nullable
    public ElectricalPanelSlot getHoveringAttachmentIndex(Vec3 localClickPos) {
        localClickPos = VecHelper.rotateCentered(localClickPos, getBlockState().getValue(ElectricalPanelBlock.FACING).toYRot() + 180, Direction.Axis.Y);

        PanelAttachment[] attachments = this.getAttachments();

        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;

            if (attachment.slot.shape.minX < localClickPos.x && attachment.slot.shape.maxX > localClickPos.x &&
                    attachment.slot.shape.minY < localClickPos.y && attachment.slot.shape.maxY > localClickPos.y)
                return attachment.slot;
        }

        return null;
    }

    public Map<Integer, Vec3> getNodePositions() {
        PanelAttachment[] attachments = getAttachments();
        Map<Integer, Vec3> out = new HashMap<>();
        Direction facing = getBlockState().getValue(ElectricalPanelBlock.FACING);
        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;
            for (InWorldNode node : attachment.nodes) {
                int id = node.id();
                out.put(id, ElectricalPanelNodeState.configurator.getNodePos(facing, id));
            }
        }

        return out;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state) {
        ItemRequirement requiredItems = super.getRequiredItems(state);
        for (PanelAttachment attachment : getAttachments()) {
            if (attachment == null)
                continue;

            requiredItems = requiredItems.union(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, attachment.getDrops()));
        }
        return requiredItems;
    }
}
