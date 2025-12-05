package com.phasetranscrystal.brealib.test;

import com.phasetranscrystal.brealib.mui.modular.IUIItem;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class MuiTestItem extends Item implements IUIItem {

    public MuiTestItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return tryOpenUI(player, hand);
    }
}
