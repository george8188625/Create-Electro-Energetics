package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.content.wire_spool.WireRenderer;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class TransformerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public TransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    protected ScrollValueBehaviour ratio;
    protected double power;
    protected double lastSentPower = -1;

    List<Float> voltages = new ArrayList<>();
    float avgVoltage = 0;

    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Float vp1 = WireRenderer.getAllVoltages().get(new Node(0, getBlockPos()));
        Float vp2 = WireRenderer.getAllVoltages().get(new Node(1, getBlockPos()));
        Float vs1 = WireRenderer.getAllVoltages().get(new Node(2, getBlockPos()));
        Float vs2 = WireRenderer.getAllVoltages().get(new Node(3, getBlockPos()));
        if (vp1 == null || vp2 == null || vs1 == null || vs2 == null)
            return false;
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.primary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(vp1 - vp2))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.secondary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(vs1 - vs2))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide)
            if (Math.abs(lastSentPower - power) > 100) {
                lastSentPower = power;
                sendData();
            }

        if (!level.isClientSide)
            return;

        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickAudio);
    }

    @Override
    public void lazyTick() {
        if (level.isClientSide)
            return;
        lastSentPower = power;
        sendData();
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        if (voltages.isEmpty())
            avgVoltage = 0;
        else
            avgVoltage = voltages.stream().reduce(Float::sum).orElse(0f) / voltages.size();

        Float v1 = WireRenderer.getAllVoltages().get(new Node(0, getBlockPos()));
        Float v2 = WireRenderer.getAllVoltages().get(new Node(1, getBlockPos()));
        if (v1 != null && v2 != null) {
            setVoltage(v1 - v2);
            if (avgVoltage > 10) {
                if (soundInstance == null || soundInstance.isStopped())
                    Minecraft.getInstance()
                            .getSoundManager()
                            .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
                else if (soundInstance != null) {
                    soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.7));
                    soundInstance.keepAlive();
                }
            } else if (soundInstance != null);
        }
    }

    private void setVoltage(float voltage) {
        if (voltages.size() >= 3)
            voltages.remove(0);
        voltages.add(voltage);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

        ratio = new ScrollValueBehaviour(Component.translatable("electroenergetics.transformer.ratio"),
                this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, 1, ImmutableList.of(Component.literal("Value")),
                        new ValueSettingsFormatter(valueSettings -> Component.literal(valueSettings.value() > 8 ? "1 / " + (valueSettings.value() - 7) : (valueSettings.value() + 1) + " / 1")));
            }
        };
        ratio.between(0, 16);
        ratio.value = 2;
        ratio.withFormatter(i -> i > 8 ? "1 / " + (i - 7) : (i + 1) + " / 1");
        ratio.withCallback(i -> this.updateGeneratedRatio());
        behaviours.add(ratio);
    }

    private void updateGeneratedRatio() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null) {
            int i = ratio.value;
            deviceInstance.extraData().putFloat("Ratio", i > 8 ? (float) 1 / (i - 7) : (float) (i + 1));
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket)
            tag.putDouble("Power", power);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket)
            power = tag.getDouble("Power");
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 7, 15);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal() && direction.getAxis() != state.getValue(TransformerBlock.FACING).getAxis();
        }
    }
}
