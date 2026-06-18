package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Objects;

public class ElectricalPanelDevice extends SimpleElectricalDevice {
    public PanelAttachment[] attachments = new PanelAttachment[ElectricalPanelSlot.values().length];
    Direction panelFacing = Direction.NORTH;

    public ElectricalPanelDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    private boolean initialized;

    @Override
    public void preTick(BridgeCollector bridges) {
        for (PanelAttachment attachment : attachments)
            if (attachment != null) {
                attachment.preTick(bridges);
                if (!initialized)
                    attachment.initialize();
            }
        initialized = true;
    }

    @Override
    public void postTick(SimulationResults results) {
        for (PanelAttachment attachment : attachments)
            if (attachment != null)
                attachment.postTick(results);
    }

    @Override
    public void read(CompoundTag tag) {
        Direction facing = Direction.byName(tag.getString("Facing"));
        if (facing == null)
            facing = Direction.NORTH;
        ElectricalPanelBlockEntity.readAttachments(tag, attachments, pos, level, level.registryAccess(), false, facing);
        if (Arrays.stream(attachments).allMatch(Objects::isNull)) {
            Arrays.fill(attachments, null);
            ElectricalPanelBlockEntity.readLegacyAttachments(tag, attachments, pos, level, level.registryAccess(), facing);
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
        tag.putString("Facing", panelFacing.getSerializedName());
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
            attachments[i].write(dataTag, false, level.registryAccess());
            if (attachments[i].label != null)
                attachmentTag.putString("Label", attachments[i].label);
        }
    }

    @Override
    public boolean shouldRemove(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() != newState.getBlock().getClass();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (PanelAttachment attachment : attachments)
            if (attachment != null)
                attachment.onRemoved(null);
    }
}
