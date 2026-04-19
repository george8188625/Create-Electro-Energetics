package com.george_vi.electroenergetics;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CEEMixinPlugin implements IMixinConfigPlugin {


    @Override
    public void onLoad(String s) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if ((mixinClassName.equals("com.george_vi.electroenergetics.mixins.StructureTemplateMixin") ||
                mixinClassName.equals("com.george_vi.electroenergetics.mixins.SchematicPrinterMixin")) &&
                (LoadingModList.get().getModFileById("betterend") != null ||
                 LoadingModList.get().getModFileById("betternether") != null))
            return false;
        if (mixinClassName.equals("com.george_vi.electroenergetics.mixins.SubLevelAssemblyHelper") &&
                LoadingModList.get().getModFileById("sable") == null)
            return false;
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
