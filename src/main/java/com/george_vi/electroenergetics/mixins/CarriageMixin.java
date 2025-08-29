package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Carriage.class)
public class CarriageMixin implements IPantographList {
    @Unique
    public List<Pair<BlockPos, Boolean>> electroEnergetics$pantographs = new ArrayList<>();

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lcom/simibubi/create/content/trains/graph/TrackGraph;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Carriage;", at=@At("RETURN"), remap = false)
    private static void electroEnergetics$read(CompoundTag tag, HolderLookup.Provider registries, TrackGraph graph, DimensionPalette dimensions, CallbackInfoReturnable<Carriage> cir) {
        Carriage carriage = cir.getReturnValue();
        List<Pair<BlockPos, Boolean>> pantographs = new ArrayList<>();
        NBTHelper.iterateCompoundList(tag.getList("CEEPantographs", Tag.TAG_COMPOUND), pt -> {
            BlockPos pos = NBTHelper.readBlockPos(pt, "Pos");
            boolean forward = pt.getBoolean("Forward");
            pantographs.add(Pair.of(pos, forward));
        });
        ((IPantographList)carriage).setPantographList(pantographs);
    }
    @Inject(method = "setContraption", at=@At("TAIL"), remap = false)
    public void electroEnergetics$setContraption(Level level, CarriageContraption contraption, CallbackInfo ci) {
        this.electroEnergetics$pantographs = ((IPantographList)contraption).getPantographList();
    }

    @Inject(method = "write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;", at=@At("RETURN"), remap = false)
    public void electroEnergetics$write(DimensionPalette dimensions, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        ListTag pantographTag = new ListTag();
        for (Pair<BlockPos, Boolean> e : List.copyOf(electroEnergetics$pantographs)) {
            CompoundTag pt = new CompoundTag();
            pt.put("Pos", NbtUtils.writeBlockPos(e.getFirst()));
            pt.putBoolean("Forward", e.getSecond());
        }
        tag.put("CEEPantographs", pantographTag);
    }

    @Override
    public void setPantographList(List<Pair<BlockPos, Boolean>> newPantographList) {
        electroEnergetics$pantographs = newPantographList;
    }

    @Override
    public List<Pair<BlockPos, Boolean>> getPantographList() {
        return electroEnergetics$pantographs;
    }
}
