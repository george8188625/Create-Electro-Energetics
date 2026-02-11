package com.george_vi.electroenergetics.content.transformer;

import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.SimulatedDeviceInstance;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.data.Couple;
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

import java.util.List;

public class TransformerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public TransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    protected ScrollValueBehaviour ratio;
    protected double power;
    protected double lastSentPower = -1;
    protected double primaryVoltage;
    protected double secondaryVoltage;


    @OnlyIn(Dist.CLIENT)
    protected ElectricHumSoundInstance soundInstance;

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.primary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(primaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElecrtoEnergetics.ID)
                .translate("gui.goggles.secondary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElecrtoEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(secondaryVoltage))))
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

        if (power > 100) {
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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

        ratio = new ScrollValueBehaviour(CEELang.translate("transformer.ratio").component(),
                this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, ratios.length / 11, ImmutableList.of(CEELang.translate("transformer.ratio_symbol").component()),
                        new ValueSettingsFormatter(valueSettings -> Component.literal(indexToRatioString(valueSettings.value()))));
            }
        };
        ratio.between(0, ratios.length * 2);
        ratio.value = ratios.length;
        ratio.withFormatter(TransformerBlockEntity::indexToRatioString);
        ratio.withCallback(i -> this.updateRatio());
        behaviours.add(ratio);
    }

    static String indexToRatioString(int i) {
        if (i == ratios.length)
            return "1 / 1";
        if (i > ratios.length) {
            i -= ratios.length + 1;
            return ratios[i].getFirst() + " / " + ratios[i].getSecond();
        } else {
            i = ratios.length - i - 1;
            return ratios[i].getSecond() + " / " + ratios[i].getFirst();
        }
    }

    static double indexToRatio(int i) {
        if (i == ratios.length)
            return 1;
        if (i > ratios.length) {
            i -= ratios.length + 1;
            return (double) ratios[i].getFirst() / ratios[i].getSecond();
        } else {
            i = ratios.length - i - 1;
            return (double) ratios[i].getSecond() / ratios[i].getFirst();
        }
    }

    private void updateRatio() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        SimulatedDeviceInstance<?> deviceInstance = sd.getDevice(getBlockPos());

        if (deviceInstance != null && deviceInstance.extraData() instanceof TransformerDevice.DataHolder dataHolder)
            dataHolder.ratio = indexToRatio(ratio.value);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            tag.putDouble("Power", power);
            tag.putDouble("PV", primaryVoltage);
            tag.putDouble("SV", secondaryVoltage);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            power = tag.getDouble("Power");
            primaryVoltage = tag.getDouble("PV");
            secondaryVoltage = tag.getDouble("SV");
        }
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

    static Couple<Integer>[] ratios = new Couple[]{
            Couple.create(9, 10),
            Couple.create(8, 9),
            Couple.create(7, 8),
            Couple.create(6, 7),
            Couple.create(5, 6),
            Couple.create(4, 5),
            Couple.create(7, 9),
            Couple.create(3, 4),
            Couple.create(5, 7),
            Couple.create(2, 3),
            Couple.create(3, 5),
            Couple.create(4, 7),
            Couple.create(1, 2),
            Couple.create(3, 7),
            Couple.create(2, 5),
            Couple.create(3, 8),
            Couple.create(1, 3),
            Couple.create(2, 7),
            Couple.create(1, 4),
            Couple.create(2, 9),
            Couple.create(1, 5),
            Couple.create(2, 11),
            Couple.create(1, 6),
            Couple.create(1, 7),
            Couple.create(1, 8),
            Couple.create(1, 9),
            Couple.create(1, 10),
            Couple.create(1, 12),
            Couple.create(1, 14),
            Couple.create(1, 16),
            Couple.create(1, 18),
            Couple.create(1, 20),
            Couple.create(1, 22),
            Couple.create(1, 25),
            Couple.create(1, 28),
            Couple.create(1, 30),
            Couple.create(1, 33),
            Couple.create(1, 36),
            Couple.create(1, 40),
            Couple.create(1, 43),
            Couple.create(1, 46),
            Couple.create(1, 50)
    };

}
