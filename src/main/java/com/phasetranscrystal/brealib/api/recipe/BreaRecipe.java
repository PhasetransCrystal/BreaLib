package com.phasetranscrystal.brealib.api.recipe;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IRecipeOutputExtension;

import org.jetbrains.annotations.Nullable;

public class BreaRecipe implements IRecipeOutputExtension {

    @Override
    public void accept(ResourceKey<Recipe<?>> resourceKey, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder, ICondition... iConditions) {}
}
