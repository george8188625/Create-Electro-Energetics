package com.george_vi.electroenergetics.content.linemans_stick;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;

public class LinemansStickItem extends Item {

    public LinemansStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.NONE;
    }

}
