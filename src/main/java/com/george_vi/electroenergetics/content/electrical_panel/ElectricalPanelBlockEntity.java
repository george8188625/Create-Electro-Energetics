package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class ElectricalPanelBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {

    // This exists purely to mirror the device's data, or for rendering on the client side.
    // The device actually owns those.
    private ElectricalPanelLayoutType layoutType = ElectricalPanelLayoutType.NONE;
    private PanelAttachment[] attachments = new PanelAttachment[0];

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
        for (PanelAttachment attachment : attachments)
            if (attachment != null)
                attachment.tickClient(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        setLayoutType(ElectricalPanelLayoutType.byIdOrNone(tag.getString("Layout")));
        PanelAttachment[] attachments = getAttachments();
        ElectricalPanelLayoutType layoutType = getLayoutType();

        if (attachments.length != layoutType.slots.length)
            setAttachments(new PanelAttachment[layoutType.slots.length]);
        attachments = getAttachments();

        for (int i = 0; i < attachments.length; i++) {
            CompoundTag attachmentTag = tag.getCompound("Attachment" + i);
            if (attachmentTag.isEmpty()) {
                attachments[i] = null;
                continue;
            }
            String id = attachmentTag.getString("ID");

            ResourceLocation attachmentId = ResourceLocation.tryParse(id);

            if (attachmentId == null) {
                attachments[i] = null;
                continue;
            }

            PanelAttachmentType type = CEERegistries.PANEL_ATTACHMENT_TYPE.get(attachmentId);

            if (type == null) {
                attachments[i] = null;
                continue;
            }

            if (attachments[i] == null) {
                attachments[i] = type.createNew(worldPosition, type.mode.getNodesFor(i, worldPosition, layoutType),
                        level, layoutType.slots[i], getBlockState().getValue(ElectricalPanelBlock.FACING));
            }

            attachments[i].read(attachmentTag.getCompound("Data"), clientPacket);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        ElectricalPanelLayoutType layoutType = getLayoutType();
        tag.putString("Layout", layoutType.getSerializedName());
        PanelAttachment[] attachments = getAttachments();
        for (int i = 0; i < attachments.length; i++) {
            if (attachments[i] == null)
                continue;

            ResourceLocation id = CEERegistries.PANEL_ATTACHMENT_TYPE.getKey(attachments[i].type);
            if (id == null)
                continue;

            CompoundTag attachmentTag = new CompoundTag();
            tag.put("Attachment" + i, attachmentTag);

            attachmentTag.putString("ID", id.toString());

            CompoundTag dataTag = new CompoundTag();
            attachmentTag.put("Data", dataTag);
            attachments[i].write(dataTag, clientPacket);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null)
            return false;
        Vec3 clickedPos = mc.hitResult.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition));
        int slot = getHoveringAttachmentIndex(clickedPos);
        if (slot == -1 || attachments[slot] == null)
            return false;
        return attachments[slot].addToGoggleTooltip(this, tooltip, isPlayerSneaking);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null)
            return false;
        Vec3 clickedPos = mc.hitResult.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition));
        int slot = getHoveringAttachmentIndex(clickedPos);
        if (slot == -1 || attachments[slot] == null)
            return false;
        return attachments[slot].addToTooltip(this, tooltip, isPlayerSneaking);
    }

    public void attachmentUpdate() {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ElectricalPanelBlock.NODE_STATE, ElectricalPanelNodeState.getState(getAttachments(), getLayoutType())));
        sendData();
    }

    public ElectricalPanelLayoutType getLayoutType() {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            return device.layoutType;
        return layoutType;
    }

    public void setLayoutType(ElectricalPanelLayoutType layoutType) {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            device.layoutType = layoutType;
        this.layoutType = layoutType;
    }

    public PanelAttachment[] getAttachments() {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            return device.attachments;
        return attachments;
    }

    public void setAttachments(PanelAttachment[] attachments) {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            device.attachments = attachments;
        this.attachments = attachments;
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
    public int getHoveringAttachmentIndex(Vec3 localClickPos) {

        ElectricalPanelLayoutType layout = this.getLayoutType();
        PanelAttachment[] attachments = this.getAttachments();

        BlockState state = this.getBlockState();

        Direction facing = state.getValue(ElectricalPanelBlock.FACING);

        ElectricalPanelSlot slot = null;
        if (attachments.length == 0)
            return -1;
        if (attachments.length == 1 && attachments[0] != null) {
            slot = ElectricalPanelSlot.FULL_SLOT;
        } else if (layout == ElectricalPanelLayoutType.HALF_HORIZONTAL) {
            slot = PanelAttachmentMode.HALF_ONLY_HORIZONTAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.HALF_VERTICAL) {
            slot = PanelAttachmentMode.HALF_ONLY_VERTICAL.getSlot(facing, localClickPos);
        } else if (layout == ElectricalPanelLayoutType.THIRD) {
            slot = PanelAttachmentMode.THIRD.getSlot(facing, localClickPos);
        }

        if (slot == null)
            return -1;

        int slotIndex = layout.getIndexOfSlot(slot);
        if (slotIndex == -1 || attachments[slotIndex] == null)
            return -1;

        Vec3 rotatedClickPos = VecHelper.rotateCentered(localClickPos, facing.toYRot() + 180, Direction.Axis.Y);
        double x = rotatedClickPos.x;
        double y = rotatedClickPos.y;
        if (x < 2/16f || x > 14/16f || y < 2/16f || y > 14/16f)
            return -1;

        if (attachments[slotIndex] != null)
            return slotIndex;
        return  -1;
    }
}
