package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.content.wire.WireRenderer;
import com.george_vi.electroenergetics.simulation.InfrastructureSavedData;
import com.george_vi.electroenergetics.foundation.Node;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
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
                    soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.25));
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
                return new ValueSettingsBoard(label, max, 1, ImmutableList.of(Component.literal("Ratio")),
                        new ValueSettingsFormatter(valueSettings -> Component.literal(Ratios.values()[(valueSettings.value())].string)));
            }
        };
        ratio.between(0, Ratios.values().length - 1);
        ratio.value = 0;
        ratio.withFormatter(i -> Ratios.values()[i].string);
        ratio.withCallback(i -> this.updateRatio());
        behaviours.add(ratio);
    }

    private void updateRatio() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InfrastructureSavedData.SimulatedDeviceInstance deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null) {
            int i = ratio.value;
            deviceInstance.extraData().putFloat("Ratio", (float) Ratios.values()[i].value);
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

    public enum Ratios implements INamedIconOptions {
        ONE_OVER_ONE(1, 1),

        ONE_OVER_TWO(1, 2),
        TWO_OVER_ONE(2, 1),

        ONE_OVER_THREE(1, 3),
        THREE_OVER_ONE(3, 1),
        TWO_OVER_THREE(2, 3),
        THREE_OVER_TWO(3, 2),

        ONE_OVER_FOUR(1, 4),
        FOUR_OVER_ONE(4, 1),
        THREE_OVER_FOUR(3, 4),
        FOUR_OVER_THREE(4, 3),

        ONE_OVER_FIVE(1, 5),
        FIVE_OVER_ONE(5, 1),
        TWO_OVER_FIVE(2, 5),
        FIVE_OVER_TWO(5, 2),
        THREE_OVER_FIVE(3, 5),
        FIVE_OVER_THREE(5, 3),
        FOUR_OVER_FIVE(4, 5),
        FIVE_OVER_FOUR(5, 4),

        ONE_OVER_SIX(1, 6),
        SIX_OVER_ONE(6, 1),
        FIVE_OVER_SIX(5, 6),
        SIX_OVER_FIVE(6, 5),

        ONE_OVER_SEVEN(1, 7),
        SEVEN_OVER_ONE(7, 1),
        TWO_OVER_SEVEN(2, 7),
        SEVEN_OVER_TWO(7, 2),
        THREE_OVER_SEVEN(3, 7),
        SEVEN_OVER_THREE(7, 3),
        FOUR_OVER_SEVEN(4, 7),
        SEVEN_OVER_FOUR(7, 4),
        FIVE_OVER_SEVEN(5, 7),
        SEVEN_OVER_FIVE(7, 5),
        SIX_OVER_SEVEN(6, 7),
        SEVEN_OVER_SIX(7, 6),

        ONE_OVER_EIGHT(1, 8),
        EIGHT_OVER_ONE(8, 1),
        THREE_OVER_EIGHT(3, 8),
        EIGHT_OVER_THREE(8, 3),
        FIVE_OVER_EIGHT(5, 8),
        EIGHT_OVER_FIVE(8, 5),
        SEVEN_OVER_EIGHT(7, 8),
        EIGHT_OVER_SEVEN(8, 7),

        ONE_OVER_TEN(1, 10),
        TEN_OVER_ONE(10, 1),

        ONE_OVER_FIFTEEN(1, 15),
        FIFTEEN_OVER_ONE(15, 1),

        ONE_OVER_TWENTY(1, 20),
        TWENTY_OVER_ONE(20, 1),

        ONE_OVER_FIFTY(1, 50),
        FIFTY_OVER_ONE(50, 1),
        ;


        public final double value;
        public final String string;
        Ratios(int numerator, int denominator) {
            value = (double) numerator / denominator;
            string = numerator + " / " + denominator;
        }

        @Override
        public AllIcons getIcon() {
            return null;
        }

        @Override
        public String getTranslationKey() {
            return null;
        }
    }
}
