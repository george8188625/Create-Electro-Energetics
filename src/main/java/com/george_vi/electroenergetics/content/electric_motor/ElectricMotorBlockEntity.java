package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.content.wire_spool.WireRenderer;
import com.george_vi.electroenergetics.simulation.Node;
import com.simibubi.create.content.decoration.steamWhistle.WhistleSoundInstance;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity {
    float voltage = 0;

    List<Float> voltages = new ArrayList<>();
    float avgVoltage = 0;
    float voltageBeforeLastChange = 0;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Voltage", voltage);
        tag.put("Voltages", NBTHelper.writeCompoundList(voltages, (v) -> {
            CompoundTag t = new CompoundTag();
            t.putFloat("V", v);
            return t;
        }));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        voltage = tag.getFloat("Voltage");
        avgVoltage = tag.getFloat("AvgVoltage");
        voltages = NBTHelper.readCompoundList(tag.getList("Voltages", Tag.TAG_COMPOUND), t -> t.getFloat("V"));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(avgVoltage) < 0.1)
            return false;
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(avgVoltage * avgVoltage / 30)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_voltage")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    int tick = 0;
    @Override
    public void tick() {
        super.tick();
        tick++;
        if (voltages.isEmpty())
            avgVoltage = 0;
        else
            avgVoltage = voltages.stream().reduce(Float::sum).orElse(0f) / voltages.size();

        if ((Math.abs(voltageBeforeLastChange - avgVoltage) < 80 && ((Math.abs(avgVoltage) > 40 && Math.abs(voltageBeforeLastChange) > 40) || (Math.abs(avgVoltage) < 40 && Math.abs(voltageBeforeLastChange) < 40))))
            if (!(tick % 40 == 0 && Math.abs(voltageBeforeLastChange - avgVoltage) > 5))
                return;
        voltageBeforeLastChange = avgVoltage;

        reActivateSource = true;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        Float v1 = WireRenderer.getAllVoltages().get(new Node(0, getBlockPos()));
        Float v2 = WireRenderer.getAllVoltages().get(new Node(1, getBlockPos()));
        if (v1 != null && v2 != null)
            setVoltage(v1 - v2);
        if (Math.abs(avgVoltage) > 79) {
            if (soundInstance == null || soundInstance.isStopped())
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            else if (soundInstance != null)
                soundInstance.keepAlive();
        } else if (soundInstance != null);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = 128 / 9.6f;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        float vS = avgVoltage * avgVoltage;
        vS /= (float) (13 * CEEConfigs.server().motorResistance.get());
        return Math.abs(avgVoltage) < 80 ? 0 : Mth.clamp(vS, -AllConfigs.server().kinetics.maxRotationSpeed.get(), AllConfigs.server().kinetics.maxRotationSpeed.get());
    }

    public void setVoltage(float voltage) {
        if (voltages.size() >= 30)
            voltages.remove(0);
        voltages.add(voltage);
        this.voltage = voltage;
    }
}
