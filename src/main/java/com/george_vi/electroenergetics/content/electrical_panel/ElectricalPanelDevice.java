package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricalPanelDevice extends SimpleElectricalDevice {
    public ElectricalPanelLayoutType layoutType = ElectricalPanelLayoutType.NONE;
    public PanelAttachment[] attachments = new PanelAttachment[0];
    Direction panelFacing = Direction.NORTH;

    public ElectricalPanelDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        for (PanelAttachment attachment : attachments)
            if (attachment != null)
                attachment.preTick(bridges);
    }

    @Override
    public void postTick(SimulationResults results) {
        for (PanelAttachment attachment : attachments)
            if (attachment != null)
                attachment.postTick(results);
    }

    @Override
    public void read(CompoundTag tag) {
        layoutType = ElectricalPanelLayoutType.byIdOrNone(tag.getString("Layout"));
        Direction facing = Direction.byName(tag.getString("Facing"));
        if (facing == null)
            facing = Direction.NORTH;

        if (attachments.length != layoutType.slots.length)
            attachments = new PanelAttachment[layoutType.slots.length];

        for (int i = 0; i < attachments.length; i++) {
            CompoundTag attachmentTag = tag.getCompound("Attachment" + i);
            if (attachmentTag.isEmpty())
                continue;
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
                attachments[i] = type.createNew(pos, type.mode.getNodesFor(i, pos, layoutType), level, layoutType.slots[i], facing);
            }

            attachments[i].read(attachmentTag.getCompound("Data"), false);
            attachments[i].label = attachmentTag.contains("Label") ? attachmentTag.getString("Label") : null;
        }
    }

    @Override
    public void update() {
        super.update();
        if (!level.isLoaded(pos))
            return;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ElectricalPanelBlock))
            return;

        if (state.getValue(ElectricalPanelBlock.FACING) != panelFacing) {
            panelFacing = state.getValue(ElectricalPanelBlock.FACING);
            for (PanelAttachment attachment : attachments)
                if (attachment != null)
                    attachment.panelFacing = panelFacing;
        }
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putString("Layout", layoutType.getSerializedName());
        tag.putString("Facing", panelFacing.getSerializedName());
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
            attachments[i].write(dataTag, false);
            if (attachments[i].label != null)
                attachmentTag.putString("Label", attachments[i].label);
        }
    }
}
