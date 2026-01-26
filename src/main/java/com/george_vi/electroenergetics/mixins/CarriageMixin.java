package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEPantographTypes;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElecrtoEnergetics;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Carriage.class)
public class CarriageMixin implements IPantographList {
    @Unique
    public List<TrainPantographEntry> electroEnergetics$pantographs = new ArrayList<>();

    @Unique
    public Set<TrainSoundModifier> electroEnergetics$soundModifyingBlocks = new HashSet<>();

    @Unique
    public boolean electroEnergetics$hasMotor = false;

    @Shadow
    private Train train;

    @Override
    public int getAccumulators() {
        return 0;
    }

    @Override
    public void setAccumulators(int value) {
    }

    @Override
    public boolean hasCreativeElectricalSource() {
        return false;
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lcom/simibubi/create/content/trains/graph/TrackGraph;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Carriage;", at=@At("RETURN"), remap = false)
    private static void electroEnergetics$read(CompoundTag tag, HolderLookup.Provider registries, TrackGraph graph, DimensionPalette dimensions, CallbackInfoReturnable<Carriage> cir) {
        Carriage carriage = cir.getReturnValue();
        List<TrainPantographEntry> pantographs = new ArrayList<>();
        NBTHelper.iterateCompoundList(tag.getList("CEEPantographs", Tag.TAG_COMPOUND), pt -> {
            BlockPos pos = NBTHelper.readBlockPos(pt, "Pos");
            BlockPos originalPos = NBTHelper.readBlockPos(pt, "OriginalPos");
            boolean forward = pt.getBoolean("Forward");
            boolean active = pt.getBoolean("Active");
            ResourceLocation pantographTypeId = ResourceLocation.tryParse(pt.getString("TypeID"));
            PantographType type;

            if (pantographTypeId == null)
                type = CEEPantographTypes.STANDARD.get();
            else {
                type = CEERegistries.PANTOGRAPH_TYPE.get(pantographTypeId);
                if (type == null)
                    type = CEEPantographTypes.STANDARD.get();
            }

            pantographs.add(new TrainPantographEntry(originalPos, pos, type, active, forward));
        });
        ((IPantographList)carriage).setPantographList(pantographs);
        ((IPantographList)carriage).setElectricMotor(tag.getBoolean("CEEHasElectricMotor"));
    }

    @Inject(method = "setContraption", at=@At("TAIL"), remap = false)
    public void electroEnergetics$setContraption(Level level, CarriageContraption contraption, CallbackInfo ci) {
        IPantographList contraptionExtension = (IPantographList) contraption;
        this.electroEnergetics$pantographs = contraptionExtension.getPantographList();
        this.electroEnergetics$hasMotor = contraptionExtension.hasElectricMotor();
        this.electroEnergetics$soundModifyingBlocks = contraptionExtension.getSoundModifyingBlocks();

        if (train == null)
            return;
        ICEETrainExtension trainExtension = (ICEETrainExtension) train;
        ElectricTrainData trainData = trainExtension.getElectricTrainData();
        trainData.accumulators += contraptionExtension.getAccumulators();
        trainData.hasCreativeSource = contraptionExtension.hasCreativeElectricalSource();
        trainData.pantographs.addAll(this.electroEnergetics$pantographs);

        if (electroEnergetics$soundModifyingBlocks.isEmpty())
            return;
        Set<TrainSoundModifier> trainSMBlocks = trainExtension.getSoundModifyingBlocks();
        trainSMBlocks.addAll(electroEnergetics$soundModifyingBlocks);
        TrainSoundModifier choice = null;
        for (TrainSoundModifier sm : trainSMBlocks) {
            if (choice == null || choice.priority() < sm.priority())
                choice = sm;
        }

        if (choice != null)
            trainExtension.setSoundType(choice.soundType());
    }

    @Inject(method = "write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;", at=@At("RETURN"), remap = false)
    public void electroEnergetics$write(DimensionPalette dimensions, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        ListTag pantographTag = new ListTag();
        for (TrainPantographEntry e : List.copyOf(electroEnergetics$pantographs)) {
            CompoundTag pt = new CompoundTag();
            pt.put("Pos", NbtUtils.writeBlockPos(e.rotatedPos));
            pt.put("OriginalPos", NbtUtils.writeBlockPos(e.originalPos));
            pt.putBoolean("Forward", e.facingForward);
            pt.putBoolean("Active", e.active);
            ResourceLocation key = CEERegistries.PANTOGRAPH_TYPE.getKey(e.type);
            if (key != null)
                pt.putString("TypeID", key.toString());
            pantographTag.add(pt);
        }
        tag.put("CEEPantographs", pantographTag);
        tag.putBoolean("CEEHasElectricMotor", electroEnergetics$hasMotor);
    }

    @Inject(method = "setTrain", at=@At("TAIL"), remap = false)
    public void setElectroEnergetics$setTrain(Train train, CallbackInfo ci) {
        ICEETrainExtension trainExtension = (ICEETrainExtension) train;
        ElectricTrainData trainData = trainExtension.getElectricTrainData();
        List<TrainPantographEntry> toAdd = new ArrayList<>();
        for (TrainPantographEntry pantograph : this.electroEnergetics$pantographs) {
            if (!trainData.pantographs.contains(pantograph))
                toAdd.add(pantograph);
        }
        trainData.pantographs.addAll(toAdd);
    }


    @Override
    public void setPantographList(List<TrainPantographEntry> newPantographList) {
        electroEnergetics$pantographs = newPantographList;
    }

    @Override
    public List<TrainPantographEntry> getPantographList() {
        return electroEnergetics$pantographs;
    }

    @Override
    public boolean hasElectricMotor() {
        return electroEnergetics$hasMotor;
    }

    @Override
    public void setElectricMotor(boolean v) {
        electroEnergetics$hasMotor = v;
    }


    @Override
    public Set<TrainSoundModifier> getSoundModifyingBlocks() {
        return electroEnergetics$soundModifyingBlocks;
    }
}
