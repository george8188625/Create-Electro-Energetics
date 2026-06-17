package com.george_vi.electroenergetics.content.electrical_panel.link;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public interface ElectricalPanelLink extends MenuProvider, IRedstoneLinkable {

    ItemStack[] getLinkFrequencies();

    Level getLevel();

    default void writeLinkFrequencies(CompoundTag tag, HolderLookup.Provider registries) {
        
        for (int i = 0; i < 2; i++) {
            ItemStack linkFrequency = getLinkFrequencies()[i];
            if (linkFrequency.isEmpty())
                continue;
            tag.put("LinkFrequency" + i, linkFrequency.save(registries));
        }
    }

    default void readLinkFrequencies(CompoundTag tag, HolderLookup.Provider registries) {

        for (int i = 0; i < 2; i++) {
            if (!tag.contains("LinkFrequency" + i)) {
                getLinkFrequencies()[i] = ItemStack.EMPTY;
                continue;
            }
            Tag itemTag = tag.get("LinkFrequency" + i);
            if (itemTag != null)
                getLinkFrequencies()[i] = ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY);
            else
                getLinkFrequencies()[i] = ItemStack.EMPTY;
        }
    }

    default void updateLinkState() {
        if (isAlive() && getLevel() != null && !getLevel().isClientSide)
            Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(getLevel(), this);

    }

    default void removeLinkState() {
        if (getLevel() != null && !getLevel().isClientSide) {
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(getLevel(), this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    default void renderLinkAntenna(ElectricalPanelBlockEntity be, PoseStack ms,
                                   MultiBufferSource buffer, int light) {
        if (!isAlive())
            return;

        boolean powered = getTransmittedStrength() > 0;


        CachedBuffers.partial(powered ?
                        CEEPartialModels.PANEL_ATTACHMENT_LINK_ANTENNA_POWERED :
                        CEEPartialModels.PANEL_ATTACHMENT_LINK_ANTENNA, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));
    }

    @Override
    default boolean isAlive() {
        return !(getLinkFrequencies()[0].isEmpty() && getLinkFrequencies()[1].isEmpty());
    }

    @Override
    default boolean isListening() {
        return false;
    }

    @Override
    default void setReceivedStrength(int power) {

    }

    @Override
    default Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
        return Couple.create(
                RedstoneLinkNetworkHandler.Frequency.of(getLinkFrequencies()[0]),
                RedstoneLinkNetworkHandler.Frequency.of(getLinkFrequencies()[1]));
    }

    @Override
    default @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return ElectricalPanelLinkMenu.create(containerId, playerInventory, this);
    }

    @Override
    default Component getDisplayName() {
        return CEELang.translateDirect("electrical_panel_linking.title");
    }

    static ElectricalPanelLink createOnClient(RegistryFriendlyByteBuf extraData) {
        return new Simple(extraData);
    }

    class Simple implements ElectricalPanelLink {
        ItemStack[] linkFrequencies = new ItemStack[2];

        public Simple(RegistryFriendlyByteBuf extraData) {
            for (int i = 0; i < 2; i++)
                linkFrequencies[i] = ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData);
        }

        @Override
        public ItemStack[] getLinkFrequencies() {
            return linkFrequencies;
        }

        @Override
        public Level getLevel() {
            return null;
        }

        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return null;
        }

        @Override
        public BlockPos getLocation() {
            return null;
        }
    }
}
