package com.phasetranscrystal.brealib.test;

import com.phasetranscrystal.brealib.mui.IUIHolder;

import net.minecraft.world.item.Item;

public class MuiTestItem extends Item implements IUIHolder {

    public MuiTestItem(Properties properties) {
        super(properties);
    }

    @Override
    public void openUI() {}
}
