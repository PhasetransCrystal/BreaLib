package com.phasetranscrystal.brealib.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class BreaStringUtil {

    @NotNull
    public static String itemStackToString(@NotNull ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stack.getCount() + "x_" + itemId.getNamespace() + "_" + itemId.getPath();
    }
}
