package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.PanelEnergyMeterScreen;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.simibubi.create.AllItems;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.UsernameCache;

import java.util.List;
import java.util.UUID;

public abstract class BaseEnergyMeterAttachment extends PanelAttachment {
    public double activePower = 0;
    public double totalEnergy = 0;
    public LerpedFloat smoothTotalEnergy = LerpedFloat.linear();
    public UUID owner;
    public int ticks = 0;
    public boolean disconnected;
    boolean inverted = false;

    public BaseEnergyMeterAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        smoothTotalEnergy.tickChaser();
    }

    @Override
    public ItemInteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.displayScreen(player));
        return ItemInteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayScreen(Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new PanelEnergyMeterScreen(this));
    }

    @Override
    public void onInserted(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        UUID owner = stack.get(CEEDataComponents.OWNER);
        Double energy = stack.get(CEEDataComponents.ENERGY);
        if (energy != null && owner != null) {
            this.owner = owner;
            this.totalEnergy = energy;
        } else {
            this.owner = player.getUUID();
        }

        inverted = hitResult.getLocation().y - pos.getY() > 0.5;
    }

    @Override
    public List<ItemStack> getDrops() {
        ItemStack stack = defaultDroppedStack();
        stack.set(CEEDataComponents.ENERGY, totalEnergy);
        if (owner != null) {
            stack.set(CEEDataComponents.OWNER, owner);
            String username = UsernameCache.getLastKnownUsername(owner);
            stack.set(CEEDataComponents.OWNER_NAME, username == null ? "Unknown Player" : username);
        }
        return List.of(stack);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        super.read(tag, clientPacket, registries);
        totalEnergy = tag.getDouble("TotalEnergy");
        activePower = tag.getDouble("ActivePower");
        disconnected = tag.getBoolean("Disconnected");
        inverted = tag.getBoolean("Inverted");
        if (tag.contains("Owner"))
            owner = tag.getUUID("Owner");

        if (clientPacket)
            smoothTotalEnergy.chase(totalEnergy, 0.5, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        super.write(tag, clientPacket, registries);
        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putDouble("ActivePower", activePower);
        tag.putBoolean("Disconnected", disconnected);
        tag.putBoolean("Inverted", inverted);
        if (owner != null)
            tag.putUUID("Owner", owner);
    }
}
